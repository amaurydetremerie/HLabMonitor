package be.wiserisk.hlabmonitor.monitor.application.port.in.execution;

import be.wiserisk.hlabmonitor.monitor.domain.model.Notification;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetId;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetResult;

public interface ExecuteNotificationUseCase {
    void handleNotification(TargetResult targetResult);

    void resolveNotificationIfResultChanged(TargetResult targetResult, Notification notification);

    void sendNotificationIfResultChanged(TargetResult targetResult);

    void saveNotification(Notification notification);

    void resendNotification(TargetId targetId);

    void deleteByNotificationId(Long notificationId);
}
