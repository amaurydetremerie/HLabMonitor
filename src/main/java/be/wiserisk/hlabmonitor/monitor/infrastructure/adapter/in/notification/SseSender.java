package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.in.notification;

import be.wiserisk.hlabmonitor.monitor.domain.model.Notification;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.in.rest.CheckNotificationController;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.Monitoring;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.notification.NotificationType;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SseSender implements NotificationSender {

    private final CheckNotificationController checkNotificationController;

    public static final NotificationType NOTIFICATION_TYPE = new NotificationType();

    @Override
    public void sendNotification(Notification notification) {
        checkNotificationController.broadcastDbCount();
    }

    @Override
    public NotificationType getNotificationType(Monitoring monitoring) {
        return NOTIFICATION_TYPE;
    }
}
