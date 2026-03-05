package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.in.rest;

import be.wiserisk.hlabmonitor.monitor.application.port.in.query.GetCheckNotificationsUseCase;
import be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringResult;
import be.wiserisk.hlabmonitor.monitor.domain.model.Notification;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetId;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckNotificationControllerTest {

    public static final Notification NOTIFICATION = new Notification(new TargetResult(new TargetId("targetId"), MonitoringResult.FAILURE, "message"));
    @InjectMocks
    private CheckNotificationController checkNotificationController;

    @Mock
    private GetCheckNotificationsUseCase getCheckNotificationsUseCase;

    @Test
    void deadEmitter() {
        RuntimeException exception = new RuntimeException("exception");
        when(getCheckNotificationsUseCase.countActiveNotifications()).thenThrow(exception);

        assertDoesNotThrow(() -> checkNotificationController.stream());
    }

    @Test
    void getActiveNotifications() {
        when(getCheckNotificationsUseCase.getActiveNotifications()).thenReturn(List.of(NOTIFICATION));

        assertThat(checkNotificationController.getActiveNotifications()).isNotNull().containsExactly(NOTIFICATION);
    }

    @Test
    void countActiveNotifications() {
        when(getCheckNotificationsUseCase.countActiveNotifications()).thenReturn(1);

        assertThat(checkNotificationController.countActiveNotifications()).isNotNull().isEqualTo(1);
    }

    @Test
    void stream() {
        when(getCheckNotificationsUseCase.countActiveNotifications()).thenReturn(1);

        assertThat(checkNotificationController.stream()).isNotNull().isInstanceOf(SseEmitter.class);
    }

    @Test
    void broadcastDbCount() {
        when(getCheckNotificationsUseCase.countActiveNotifications()).thenReturn(1);

        checkNotificationController.stream();
        assertDoesNotThrow(() -> checkNotificationController.broadcastDbCount());
    }
}