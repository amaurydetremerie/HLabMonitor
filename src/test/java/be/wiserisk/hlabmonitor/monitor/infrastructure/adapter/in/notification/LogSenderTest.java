package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.in.notification;


import be.wiserisk.hlabmonitor.monitor.domain.enums.NotificationStatus;
import be.wiserisk.hlabmonitor.monitor.domain.model.Notification;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetId;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.Monitoring;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.notification.NotificationLog;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.notification.NotificationProperties;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.notification.NotificationType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.logging.LogLevel;

import java.util.stream.Stream;

import static be.wiserisk.hlabmonitor.monitor.domain.enums.NotificationStatus.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;
import static org.springframework.boot.logging.LogLevel.*;
import static org.assertj.core.api.Assertions.assertThat;

class LogSenderTest {

    public static final String TARGET_ID = "targetId";

    @Test
    void getNotificationType() {
        LogSender logSender = new LogSender(null);
        NotificationType notificationType = mock(NotificationType.class);
        Monitoring monitoring = new Monitoring(null, null, new NotificationProperties(
                null, null, null, null, new NotificationLog(
                        true, null, notificationType)));
        assertThat(logSender.getNotificationType(monitoring)).isNotNull().isEqualTo(notificationType);
    }

    @Test
    void sendNotificationFatal() {
        Notification notification = mock(Notification.class);
        when(notification.notificationStatus()).thenReturn(SEND);
        LogSender logSender = new LogSender(FATAL);
        assertThatThrownBy(() -> logSender.sendNotification(notification)).isInstanceOf(IllegalStateException.class);
    }

    public static Stream<Arguments> provider() {
        return Stream.of(
                Arguments.of(ERROR, SEND),
                Arguments.of(WARN, SEND),
                Arguments.of(INFO, SEND),
                Arguments.of(DEBUG, SEND),
                Arguments.of(TRACE, SEND),
                Arguments.of(ERROR, TERMINATED),
                Arguments.of(WARN, TERMINATED),
                Arguments.of(INFO, TERMINATED),
                Arguments.of(DEBUG, TERMINATED),
                Arguments.of(TRACE, TERMINATED),
                Arguments.of(ERROR, FAILED),
                Arguments.of(WARN, FAILED),
                Arguments.of(INFO, FAILED),
                Arguments.of(DEBUG, FAILED),
                Arguments.of(TRACE, FAILED)
        );
    }

    @MethodSource("provider")
    @ParameterizedTest
    void sendNotification(LogLevel level, NotificationStatus status) {
        try (MockedStatic<LoggerFactory> lf = Mockito.mockStatic(LoggerFactory.class)) {
            Logger logger = Mockito.mock(Logger.class);
            lf.when(() -> LoggerFactory.getLogger(LogSender.class)).thenReturn(logger);

            LogSender logSender = new LogSender(level);
            Notification notification = mock(Notification.class);
            TargetId targetId = new TargetId(TARGET_ID);
            when(notification.targetId()).thenReturn(targetId);
            when(notification.notificationStatus()).thenReturn(status);
            assertDoesNotThrow(() -> logSender.sendNotification(notification));
            verifyLog(logger, level, status);
        }

    }

    private void verifyLog(Logger logger, LogLevel level, NotificationStatus status) {
        switch (status) {
            case SEND -> verifyLog(logger, level,"Notification for target {} has been fired");
            case TERMINATED -> verifyLog(logger, level,"Notification for target {} has been cleared");
            default -> verifyLog(logger, level,"An error occurred while sending notification for target {}");
        }
    }

    private void verifyLog(Logger logger, LogLevel level, String s) {
        switch (level) {
            case ERROR -> verify(logger, times(1)).error(s, TARGET_ID);
            case WARN -> verify(logger, times(1)).warn(s, TARGET_ID);
            case INFO -> verify(logger, times(1)).info(s, TARGET_ID);
            case DEBUG -> verify(logger, times(1)).debug(s, TARGET_ID);
            case TRACE -> verify(logger, times(1)).trace(s, TARGET_ID);
            default -> verifyNoInteractions(logger);
        }
    }

}