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
            if (shouldSendNotification(notification.notificationStatus(), sender, sender.getNotificationType(monitoring))) {
                sender.sendNotification(notification);
            }
        }
    }

    private boolean shouldSendNotification(
            NotificationStatus status,
            NotificationSender sender,
            NotificationType notificationType) {

        return switch (status) {
            case SEND -> sender.isFiring(notificationType);
            case TERMINATED -> sender.isResolved(notificationType);
            case FAILED -> sender.isFailed(notificationType);
            default -> false;
        };
    }
}
