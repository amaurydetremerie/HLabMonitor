package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.in.notification;

import be.wiserisk.hlabmonitor.monitor.domain.model.Notification;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.Monitoring;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.notification.NotificationType;

public interface NotificationSender {

    void sendNotification(Notification notification);

    default boolean isFiring(NotificationType notificationType) {
        if(notificationType == null)
            return false;
        return notificationType.firing();
    }

    default boolean isResolved(NotificationType notificationType) {
        if(notificationType == null)
            return false;
        return notificationType.resolved();
    }

    default boolean isFailed(NotificationType notificationType) {
        if(notificationType == null)
            return false;
        return notificationType.failed();
    }

    NotificationType getNotificationType(Monitoring monitoring);
}
