package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.in.notification;

import be.wiserisk.hlabmonitor.monitor.domain.model.Notification;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.Monitoring;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.notification.NotificationType;

public interface NotificationSender {

    void sendNotification(Notification notification);

    NotificationType getNotificationType(Monitoring monitoring);
}
