package be.wiserisk.hlabmonitor.monitor.domain.service;

import be.wiserisk.hlabmonitor.monitor.application.port.in.execution.ExecuteNotificationUseCase;
import be.wiserisk.hlabmonitor.monitor.application.port.in.query.GetCheckNotificationsUseCase;
import be.wiserisk.hlabmonitor.monitor.application.port.out.NotificationPort;
import be.wiserisk.hlabmonitor.monitor.application.port.out.PersistencePort;
import be.wiserisk.hlabmonitor.monitor.domain.enums.NotificationStatus;
import be.wiserisk.hlabmonitor.monitor.domain.model.Notification;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetId;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetResult;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class NotificationService implements ExecuteNotificationUseCase, GetCheckNotificationsUseCase {

    private final PersistencePort persistencePort;
    private final NotificationPort notificationPort;

    @Override
    public void handleNotification(TargetResult targetResult) {
        Optional<Notification> optionalNotification = persistencePort.getSendNotification(targetResult.id());
        optionalNotification.ifPresentOrElse(notification -> resolveNotificationIfResultChanged(targetResult, notification), () -> sendNotificationIfResultChanged(targetResult));
    }

    @Override
    public void resolveNotificationIfResultChanged(TargetResult targetResult, Notification notification) {
        if(targetResult.result().getFamily() != notification.oldMonitoringResult().getFamily()) {
            saveNotification(new Notification(notification,targetResult.result()));
        }
    }

    @Override
    public void sendNotificationIfResultChanged(TargetResult targetResult) {
        if(persistencePort.isResultChanged(targetResult.id())) {
            saveNotification(new Notification(targetResult));
        }
    }

    @Override
    public void saveNotification(Notification notification) {
        Notification savedNotification;
        switch (notification.notificationStatus()) {
            case TO_SEND -> savedNotification = persistencePort.saveNotification(new Notification(notification, NotificationStatus.SEND));
            case TO_TERMINATE -> savedNotification = persistencePort.saveNotification(new Notification(notification, NotificationStatus.TERMINATED));
            default -> savedNotification = persistencePort.saveNotification(new Notification(notification, NotificationStatus.FAILED));
        }
        notificationPort.sendNotification(savedNotification);
    }

    @Override
    public void resendNotification(TargetId targetId) {
        persistencePort.getSendNotification(targetId).ifPresent(notificationPort::sendNotification);
    }

    @Override
    public void deleteByNotificationId(Long notificationId) {
        persistencePort.deleteByNotificationId(notificationId);
    }

    @Override
    public List<Notification> getActiveNotifications() {
        return persistencePort.getActiveNotifications();
    }

    @Override
    public Integer countActiveNotifications() {
        return persistencePort.countActiveNotifications();
    }
}
