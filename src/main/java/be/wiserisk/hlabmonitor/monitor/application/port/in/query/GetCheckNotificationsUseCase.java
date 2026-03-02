package be.wiserisk.hlabmonitor.monitor.application.port.in.query;

import be.wiserisk.hlabmonitor.monitor.domain.model.Notification;

import java.util.List;

public interface GetCheckNotificationsUseCase {
    List<Notification> getActiveNotifications();

    Integer countActiveNotifications();
}
