package be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

class MonitoringTest {
    @Nested
    @SpringBootTest(classes = MonitoringTest.TestConfig.class)
    @ActiveProfiles({"test", "monitoring-full"})
    class MonitoringTestFull {
        @Autowired
        private Monitoring monitoring;
        @Test
        void monitoringFull() {
            assertThat(monitoring).isNotNull().hasNoNullFieldsOrProperties();
        }
    }

    @Nested
    @SpringBootTest(classes = MonitoringTest.TestConfig.class)
    @ActiveProfiles({"test", "monitoring-empty"})
    class MonitoringTestEmpty {
        @Autowired
        private Monitoring monitoring;
        @Test
        void monitoringEmpty() {
            assertThat(monitoring).isNotNull().hasAllNullFieldsOrProperties();
        }
    }

    @Nested
    @SpringBootTest(classes = MonitoringTest.TestConfig.class)
    @ActiveProfiles({"test", "monitoring-ping"})
    class MonitoringTestPing {
        @Autowired
        private Monitoring monitoring;
        @Test
        void monitoringPing() {
            assertThat(monitoring).isNotNull().hasAllNullFieldsOrPropertiesExcept("ping");
        }
    }

    @Nested
    @SpringBootTest(classes = MonitoringTest.TestConfig.class)
    @ActiveProfiles({"test", "monitoring-http"})
    class MonitoringTestHttp {
        @Autowired
        private Monitoring monitoring;
        @Test
        void monitoringHttp() {
            assertThat(monitoring).isNotNull().hasAllNullFieldsOrPropertiesExcept("http");
        }
    }

    @EnableConfigurationProperties(Monitoring.class)
    public static class TestConfig {
        @Bean
        Monitoring monitoring() {
            return new Monitoring();
        }
    }
}