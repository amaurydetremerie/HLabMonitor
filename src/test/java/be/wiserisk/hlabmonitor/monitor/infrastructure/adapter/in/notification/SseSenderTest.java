package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.in.notification;

import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.in.rest.CheckNotificationController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
class SseSenderTest {

    @InjectMocks
    private SseSender sseSender;

    @Mock
    private CheckNotificationController checkNotificationController;

    @Test
    void sendNotification() {
        doNothing().when(checkNotificationController).broadcastDbCount();
        assertDoesNotThrow(() -> sseSender.sendNotification(null));
    }

    @Test
    void getNotificationType() {
        assertThat(sseSender.getNotificationType(null)).isEqualTo(SseSender.NOTIFICATION_TYPE);
    }

}