package be.wiserisk.hlabmonitor.monitor.domain.service;

import be.wiserisk.hlabmonitor.monitor.application.port.out.NotificationPort;
import be.wiserisk.hlabmonitor.monitor.application.port.out.PersistencePort;
import be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringResult;
import be.wiserisk.hlabmonitor.monitor.domain.enums.NotificationStatus;
import be.wiserisk.hlabmonitor.monitor.domain.model.Notification;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetId;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringResult.FAILURE;
import static be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringResult.SUCCESS;
import static be.wiserisk.hlabmonitor.monitor.domain.enums.NotificationStatus.FAILED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    private final static TargetId TARGET_ID = new TargetId("targetId");

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private PersistencePort persistencePort;
    @Mock
    private NotificationPort notificationPort;

    @Test
    void handleNotification_Empty_ResultChanged() {
        Notification notification = mock(Notification.class);
        TargetResult targetResult = mock(TargetResult.class);
        when(targetResult.id()).thenReturn(TARGET_ID);

        when(persistencePort.getSendNotification(TARGET_ID)).thenReturn(Optional.empty());
        when(persistencePort.isResultChanged(TARGET_ID)).thenReturn(true);
        when(persistencePort.saveNotification(any(Notification.class))).thenReturn(notification);
        doNothing().when(notificationPort).sendNotification(notification);

        assertDoesNotThrow(() -> notificationService.handleNotification(targetResult));
    }

    @Test
    void handleNotification_Empty_ResultNotChanged() {
        TargetResult targetResult = mock(TargetResult.class);
        when(targetResult.id()).thenReturn(TARGET_ID);

        when(persistencePort.getSendNotification(TARGET_ID)).thenReturn(Optional.empty());
        when(persistencePort.isResultChanged(TARGET_ID)).thenReturn(false);

        assertDoesNotThrow(() -> notificationService.handleNotification(targetResult));
    }

    @Test
    void handleNotification_NotEmpty_ResultNotChanged() {
        TargetResult targetResult = mock(TargetResult.class);
        when(targetResult.id()).thenReturn(TARGET_ID);
        when(targetResult.result()).thenReturn(FAILURE);
        Notification notification = mock(Notification.class);
        when(notification.oldMonitoringResult()).thenReturn(FAILURE);

        when(persistencePort.getSendNotification(TARGET_ID)).thenReturn(Optional.of(notification));

        assertDoesNotThrow(() -> notificationService.handleNotification(targetResult));
    }

    @Test
    void handleNotification_NotEmpty_ResultChanged() {
        TargetResult targetResult = mock(TargetResult.class);
        when(targetResult.id()).thenReturn(TARGET_ID);
        when(targetResult.result()).thenReturn(SUCCESS);
        Notification notification = mock(Notification.class);
        when(notification.oldMonitoringResult()).thenReturn(FAILURE);

        when(persistencePort.getSendNotification(TARGET_ID)).thenReturn(Optional.of(notification));
        when(persistencePort.saveNotification(any(Notification.class))).thenReturn(notification);
        doNothing().when(notificationPort).sendNotification(notification);

        assertDoesNotThrow(() -> notificationService.handleNotification(targetResult));
    }

    @Test
    void saveNotification() {
        Notification notification = mock(Notification.class);
        when(notification.notificationStatus()).thenReturn(FAILED);
        when(persistencePort.saveNotification(any(Notification.class))).thenReturn(notification);
        doNothing().when(notificationPort).sendNotification(notification);
        assertDoesNotThrow(() -> notificationService.saveNotification(notification));
    }

    @Test
    void resendNotification_empty() {
        when(persistencePort.getSendNotification(TARGET_ID)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> notificationService.resendNotification(TARGET_ID));
    }

    @Test
    void resendNotification_present() {
        Notification notification = mock(Notification.class);

        when(persistencePort.getSendNotification(TARGET_ID)).thenReturn(Optional.of(notification));
        doNothing().when(notificationPort).sendNotification(notification);

        assertDoesNotThrow(() -> notificationService.resendNotification(TARGET_ID));
    }

    @Test
    void getActiveNotifications() {
        Notification notification = mock(Notification.class);

        when(persistencePort.getActiveNotifications()).thenReturn(List.of(notification));

        assertThat(notificationService.getActiveNotifications()).isNotNull().containsExactly(notification);
    }

    @Test
    void countActiveNotifications() {
        when(persistencePort.countActiveNotifications()).thenReturn(2);

        assertThat(notificationService.countActiveNotifications()).isEqualTo(2);
    }
}