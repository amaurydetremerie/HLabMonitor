package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.in.notification;

import be.wiserisk.hlabmonitor.monitor.domain.model.Notification;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetId;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.notification.NotificationType;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static be.wiserisk.hlabmonitor.monitor.domain.enums.NotificationStatus.SEND;
import static be.wiserisk.hlabmonitor.monitor.domain.enums.NotificationStatus.TERMINATED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class PrometheusSenderTest {

    private SimpleMeterRegistry simpleRegistry;
    private PrometheusSender prometheusSender;

    @BeforeEach
    void setup() {
        simpleRegistry = new SimpleMeterRegistry();
        prometheusSender = new PrometheusSender(simpleRegistry);
    }

    @AfterEach
    void teardown() {
        simpleRegistry.clear();
    }

    public static final NotificationType NOTIFICATION_TYPE = new NotificationType();

    @Test
    void sendNotification_SEND() {
        Notification notification = mock(Notification.class);
        when(notification.notificationStatus()).thenReturn(SEND);
        when(notification.targetId()).thenReturn(new TargetId("id"));
        when(notification.notificationId()).thenReturn(1L);
        assertDoesNotThrow(() -> prometheusSender.sendNotification(notification));
        assertThat(simpleRegistry.getMetersAsString()).isNotNull().contains("notifications_active(GAUGE)[id='1', status='SEND', target_id='id']; value=1.0\n" +
                "notifications_total(COUNTER)[status='SEND', target_id='id']; count=1.0");
    }

    @Test
    void sendNotification_TERMINATED() {
        Notification notification = mock(Notification.class);
        when(notification.notificationStatus()).thenReturn(TERMINATED);
        when(notification.targetId()).thenReturn(new TargetId("id"));
        when(notification.notificationId()).thenReturn(1L);
        assertDoesNotThrow(() -> prometheusSender.sendNotification(notification));
        assertThat(simpleRegistry.getMetersAsString()).isNotNull().contains("notifications_active(GAUGE)[id='1', status='TERMINATED', target_id='id']; value=0.0\n" +
                "notifications_total(COUNTER)[status='TERMINATED', target_id='id']; count=1.0");
    }

    @Test
    void getNotificationType() {
        assertThat(prometheusSender.getNotificationType(null)).isEqualTo(NOTIFICATION_TYPE);
    }
}