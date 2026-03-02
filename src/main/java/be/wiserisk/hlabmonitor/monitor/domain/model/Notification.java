package be.wiserisk.hlabmonitor.monitor.domain.model;

import be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringResult;
import be.wiserisk.hlabmonitor.monitor.domain.enums.NotificationStatus;

import java.time.Instant;

import static be.wiserisk.hlabmonitor.monitor.domain.enums.NotificationStatus.TO_TERMINATE;

public record Notification(Long notificationId,
                           TargetId targetId,
                           NotificationStatus notificationStatus,
                           Instant fireAt,
                           Instant resolvedAt,
                           MonitoringResult oldMonitoringResult,
                           MonitoringResult newMonitoringResult) {

    public Notification(Notification notification, MonitoringResult result) {
        this(notification.notificationId(),
                notification.targetId(),
                TO_TERMINATE,
                notification.fireAt(),
                Instant.now(),
                notification.oldMonitoringResult(),
                result);
    }

    public Notification(Notification notification, NotificationStatus notificationStatus) {
        this(notification.notificationId(),
                notification.targetId(),
                notificationStatus,
                notification.fireAt(),
                notification.resolvedAt(),
                notification.oldMonitoringResult(),
                notification.newMonitoringResult());
    }

    public Notification(TargetResult targetResult) {
        this(null,
                targetResult.id(),
                NotificationStatus.TO_SEND,
                targetResult.checkedAt(),
                null,
                targetResult.result(),
                null);
    }
}