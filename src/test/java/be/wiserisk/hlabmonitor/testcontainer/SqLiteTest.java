package be.wiserisk.hlabmonitor.testcontainer;

import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.repository.NotificationEntityRepository;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.repository.ResultEntityRepository;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.repository.TargetEntityRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@Testcontainers
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = {
                "server.port=59813"
        })
@ActiveProfiles("testcontainer")
@DisabledIfSystemProperty(named = "skip.testcontainer.tests", matches = "true")
class SqLiteTest {

    @Autowired
    private TargetEntityRepository targetEntityRepository;
    @Autowired
    private ResultEntityRepository resultEntityRepository;
    @Autowired
    private NotificationEntityRepository notificationEntityRepository;

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"));

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("database.type", () -> "sqlite");

        registry.add("monitoring.http.local.target", () -> "http://127.0.0.1:59813/actuator/health");
    }

    @Test
    void should_run_full_startup_process() {
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            assertThat(targetEntityRepository.findAll()).hasSize(5);
        });
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            assertThat(resultEntityRepository.findAll()).hasSize(5);
        });
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            assertThat(notificationEntityRepository.findAll()).hasSize(1);
        });
    }
}
