package be.wiserisk.hlabmonitor.monitor.infrastructure.config.mapper;

import be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringResult;
import be.wiserisk.hlabmonitor.monitor.domain.enums.NotificationStatus;
import be.wiserisk.hlabmonitor.monitor.domain.model.Notification;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetId;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.entity.NotificationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import static be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringResult.UNKNOWN;
import static be.wiserisk.hlabmonitor.monitor.domain.enums.NotificationStatus.FAILED;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(source = "notificationId", target = "id")
    @Mapping(source = "targetId", target = "targetId")
    @Mapping(source = "notificationStatus", target = "notificationStatus")
    @Mapping(source = "oldMonitoringResult", target = "oldResult")
    @Mapping(source = "newMonitoringResult", target = "newResult")
    @Mapping(source = "fireAt", target = "fireAt")
    @Mapping(source = "resolvedAt", target = "resolvedAt")
    NotificationEntity toEntity(Notification t);

    @Mapping(target = "notificationId", source = "id")
    @Mapping(target = "targetId", source = "targetId")
    @Mapping(target = "notificationStatus", source = "notificationStatus")
    @Mapping(target = "oldMonitoringResult", source = "oldResult")
    @Mapping(target = "newMonitoringResult", source = "newResult")
    @Mapping(target = "fireAt", source = "fireAt")
    @Mapping(target = "resolvedAt", source = "resolvedAt")
    Notification toDomain(NotificationEntity e);

    default String map(TargetId id) {
        return id.id();
    }
    default TargetId map(String id) {
        return new TargetId(id);
    }

    default String map(MonitoringResult result) {
        return result == null ? null : result.name();
    }
    default MonitoringResult mapMonitoringResult(String result) {
        if (result == null) return UNKNOWN;
        try {
            return MonitoringResult.valueOf(result);
        } catch (IllegalArgumentException ex) {
            return UNKNOWN;
        }
    }

    default String map(NotificationStatus status) {
        return status == null ? FAILED.name() : status.name();
    }
    default NotificationStatus mapNotificationStatus(String status) {
        if (status == null) return FAILED;
        try {
            return NotificationStatus.valueOf(status);
        } catch (IllegalArgumentException ex) {
            return FAILED;
        }
    }
}
