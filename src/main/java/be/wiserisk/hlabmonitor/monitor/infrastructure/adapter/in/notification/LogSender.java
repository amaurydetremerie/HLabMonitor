package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.in.notification;

import be.wiserisk.hlabmonitor.monitor.domain.model.Notification;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.Monitoring;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.notification.NotificationType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.logging.LogLevel;

@AllArgsConstructor
@Slf4j
public class LogSender implements NotificationSender {

    private final LogLevel level;

    @Override
    public void sendNotification(Notification notification) {
        String logMessage = getMessage(notification);
        switch (level) {
            case ERROR -> log.error(logMessage, notification.targetId().id());
            case WARN -> log.warn(logMessage, notification.targetId().id());
            case INFO -> log.info(logMessage, notification.targetId().id());
            case DEBUG -> log.debug(logMessage, notification.targetId().id());
            case TRACE -> log.trace(logMessage, notification.targetId().id());
            default -> throw new IllegalStateException("Unexpected value: " + level);
        }
    }

    private String getMessage(Notification notification) {
        return switch (notification.notificationStatus()){
            case SEND -> "Notification for target {} has been fired";
            case TERMINATED -> "Notification for target {} has been cleared";
            default -> "An error occurred while sending notification for target {}";
        };
    }

    @Override
    public NotificationType getNotificationType(Monitoring monitoring) {
        return monitoring.getNotification().notificationDiscord().notificationType();
    }
}
