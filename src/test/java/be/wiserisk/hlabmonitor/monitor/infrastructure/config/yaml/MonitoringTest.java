package be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml;

import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.notification.NotificationEmail;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.notification.NotificationType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MonitoringTest {
    @Nested
    class NotificationTestRequired {
        @Test
        void notificationRequired() {
            assertThatThrownBy(() -> new NotificationEmail(null, null, null, null, null, null, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("from is null");
        }
    }

    @Nested
    @SpringBootTest(classes = MonitoringTest.TestConfig.class)
    @ActiveProfiles({"test", "notification-smtp"})
    class NotificationTestSmtp {
        @Autowired
        private Monitoring monitoring;
        @Test
        void notificationSmtp() {
            assertThat(monitoring.getNotification().notificationEmail().smtp()).extracting("auth", "tls").isEqualTo(List.of(false, false));
        }
    }

    @Nested
    @SpringBootTest(classes = MonitoringTest.TestConfig.class)
    @ActiveProfiles({"test", "notification-full"})
    class NotificationTestFull {
        @Autowired
        private Monitoring monitoring;
        @Test
        void notificationFull() {
            assertThat(monitoring.getNotification()).hasNoNullFieldsOrProperties();
        }
    }

    @Nested
    @SpringBootTest(classes = MonitoringTest.TestConfig.class)
    @ActiveProfiles({"test", "notification-default"})
    class NotificationTestDefault {
        @Autowired
        private Monitoring monitoring;
        @Test
        void notificationDefault() {
            assertThat(monitoring.getNotification()).hasNoNullFieldsOrProperties();
        }
    }

    @Nested
    @SpringBootTest(classes = MonitoringTest.TestConfig.class)
    @ActiveProfiles({"test", "notification-type-false"})
    class NotificationTestTypeFalse {
        @Autowired
        private Monitoring monitoring;
        @Test
        void notificationTypeDisabled() {
            assertThat(monitoring.getNotification().notificationEmail().notificationType()).isEqualTo(new NotificationType(false, false, false));
            assertThat(monitoring.getNotification().notificationTelegram().notificationType()).isEqualTo(new NotificationType(false, false, false));
            assertThat(monitoring.getNotification().notificationDiscord().notificationType()).isEqualTo(new NotificationType(false, false, false));
            assertThat(monitoring.getNotification().notificationLog().notificationType()).isEqualTo(new NotificationType(false, false, false));
        }
    }

    @Nested
    @SpringBootTest(classes = MonitoringTest.TestConfig.class)
    @ActiveProfiles({"test", "notification-disabled"})
    class NotificationTestDisabled {
        @Autowired
        private Monitoring monitoring;
        @Test
        void notificationDisabled() {
            assertThat(monitoring.getNotification().enabled()).isFalse();
            assertThat(monitoring.getNotification().notificationEmail().enabled()).isFalse();
            assertThat(monitoring.getNotification().notificationDiscord().enabled()).isFalse();
            assertThat(monitoring.getNotification().notificationTelegram().enabled()).isFalse();
            assertThat(monitoring.getNotification().notificationLog().enabled()).isFalse();
        }
    }

    @Nested
    @SpringBootTest(classes = MonitoringTest.TestConfig.class)
    @ActiveProfiles({"test", "monitoring-full"})
    class MonitoringTestFull {
        @Autowired
        private Monitoring monitoring;
        @Test
        void monitoringFull() {
            assertThat(monitoring).isNotNull().hasNoNullFieldsOrPropertiesExcept("notification");
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