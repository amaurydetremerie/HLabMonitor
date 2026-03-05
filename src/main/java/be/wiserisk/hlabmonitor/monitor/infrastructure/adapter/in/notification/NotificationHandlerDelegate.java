package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.in.notification;

import be.wiserisk.hlabmonitor.monitor.domain.enums.NotificationStatus;
import be.wiserisk.hlabmonitor.monitor.domain.model.Notification;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.Monitoring;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.notification.NotificationType;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.handler.annotation.Header;

import java.util.List;

public class NotificationHandlerDelegate {

    @ServiceActivator
    public void handle(
            Notification payload,
            @Header("senders") List<NotificationSender> senders,
            @Header("monitoring") Monitoring monitoring) {
        sendMessage(payload, senders, monitoring);
    }

    private void sendMessage(Notification notification,
                             List<NotificationSender> senders,
                             Monitoring monitoring) {
        for (NotificationSender sender : senders) {
            if (shouldSendNotification(notification.notificationStatus(), sender.getNotificationType(monitoring))) {
                sender.sendNotification(notification);
            }
        }
    }

    private boolean shouldSendNotification(
            NotificationStatus status,
            NotificationType notificationType) {

        return switch (status) {
            case SEND -> notificationType.firing();
            case TERMINATED -> notificationType.resolved();
            case FAILED -> notificationType.failed();
            default -> false;
        };
    }
}
