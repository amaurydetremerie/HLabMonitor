package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.in.notification;


import be.wiserisk.hlabmonitor.monitor.domain.enums.NotificationStatus;
import be.wiserisk.hlabmonitor.monitor.domain.model.Notification;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.Monitoring;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.notification.NotificationType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class NotificationHandlerDelegateTest {

    private NotificationHandlerDelegate notificationHandlerDelegate = new NotificationHandlerDelegate();

    public static Stream<Arguments> provideNotifications() {
        return Stream.of(
                Arguments.of(NotificationStatus.TO_SEND, new NotificationType(), 0),
                Arguments.of(NotificationStatus.SEND, new NotificationType(), 1),
                Arguments.of(NotificationStatus.TO_TERMINATE, new NotificationType(), 0),
                Arguments.of(NotificationStatus.TERMINATED, new NotificationType(), 1),
                Arguments.of(NotificationStatus.FAILED, new NotificationType(), 1),
                Arguments.of(NotificationStatus.TO_SEND, new NotificationType(false, false, false), 0),
                Arguments.of(NotificationStatus.SEND, new NotificationType(false, false, false), 0),
                Arguments.of(NotificationStatus.TO_TERMINATE, new NotificationType(false, false, false), 0),
                Arguments.of(NotificationStatus.TERMINATED, new NotificationType(false, false, false), 0),
                Arguments.of(NotificationStatus.FAILED, new NotificationType(false, false, false), 0)
        );
    }

    @MethodSource("provideNotifications")
    @ParameterizedTest
    void handle(NotificationStatus status, NotificationType notificationType, int times) {
        Notification notification = mock(Notification.class);
        NotificationSender sender = mock(NotificationSender.class);
        Monitoring monitoring = mock(Monitoring.class);
        when(notification.notificationStatus()).thenReturn(status);
        when(sender.getNotificationType(monitoring)).thenReturn(notificationType);
        assertDoesNotThrow(() -> notificationHandlerDelegate.handle(notification, List.of(sender), monitoring));
        verify(sender, times(times)).sendNotification(notification);
    }
}