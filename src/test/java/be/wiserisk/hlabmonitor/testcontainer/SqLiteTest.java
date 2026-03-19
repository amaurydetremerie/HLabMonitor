package be.wiserisk.hlabmonitor.testcontainer;

import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.repository.NotificationEntityRepository;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.repository.ResultEntityRepository;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.repository.TargetEntityRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

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
@Tag("testcontainer")
class SqLiteTest {

    @Autowired
    private TargetEntityRepository targetEntityRepository;
    @Autowired
    private ResultEntityRepository resultEntityRepository;
    @Autowired
    private NotificationEntityRepository notificationEntityRepository;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("database.type", () -> "sqlite");

        registry.add("monitoring.http.local.target", () -> "http://127.0.0.1:59813/actuator/health");
    }

    @Test
    void should_run_full_startup_process() {
        await().atMost(Duration.ofMinutes(2)).untilAsserted(() -> {
            assertThat(targetEntityRepository.findAll()).hasSize(5);
            System.out.println("notif : " + targetEntityRepository.findAll().size() + " -> " + targetEntityRepository.findAll());
        });
        await().atMost(Duration.ofMinutes(2)).untilAsserted(() -> {
            assertThat(resultEntityRepository.findAll()).hasSize(5);
            System.out.println("notif : " + resultEntityRepository.findAll().size() + " -> " + resultEntityRepository.findAll());
        });
        await().atMost(Duration.ofMinutes(2)).untilAsserted(() -> {
            assertThat(notificationEntityRepository.findAll()).hasSize(1);
            System.out.println("notif : " + notificationEntityRepository.findAll().size() + " -> " + notificationEntityRepository.findAll());
        });
    }
}
