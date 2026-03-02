package be.wiserisk.hlabmonitor.monitor.application.port.out;

import be.wiserisk.hlabmonitor.monitor.domain.model.Notification;

public interface NotificationPort {
    void sendNotification(Notification notification);
}
