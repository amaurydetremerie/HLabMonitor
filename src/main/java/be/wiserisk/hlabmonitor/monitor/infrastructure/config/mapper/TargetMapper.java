package be.wiserisk.hlabmonitor.monitor.infrastructure.config.mapper;

import be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringType;
import be.wiserisk.hlabmonitor.monitor.domain.enums.SpeedtestType;
import be.wiserisk.hlabmonitor.monitor.domain.model.Target;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetId;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.entity.TargetEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TargetMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "targetId", source = "id")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "acceptableStatusCode", source = "acceptableStatusCode")
    @Mapping(target = "speedtestType", source = "speedtestType")
    @Mapping(target = "warningThresholdMbps", source = "warningThresholdMbps")
    @Mapping(target = "failureThresholdMbps", source = "failureThresholdMbps")
    TargetEntity toEntity(Target t);

    @Mapping(target = "id", source = "targetId")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "acceptableStatusCode", source = "acceptableStatusCode")
    @Mapping(target = "speedtestType", source = "speedtestType")
    @Mapping(target = "warningThresholdMbps", source = "warningThresholdMbps")
    @Mapping(target = "failureThresholdMbps", source = "failureThresholdMbps")
    @Mapping(target = "interval", ignore = true)
    Target toDomain(TargetEntity e);

    default String map(TargetId id) {
        return id.id();
    }
    default TargetId map(String id) {
        return new TargetId(id);
    }

    default String map(MonitoringType type) {
        return type == null ? null : type.name();
    }
    default MonitoringType mapMonitoringType(String type) {
        if (type == null) return MonitoringType.UNKNOWN;
        try {
            return MonitoringType.valueOf(type);
        } catch (IllegalArgumentException ex) {
            return MonitoringType.UNKNOWN;
        }
    }

    default String map(SpeedtestType type) {
        return type == null ? null : type.name();
    }
    default SpeedtestType mapSpeedtestType(String type) {
        if (type == null) return null;
        try {
            return SpeedtestType.valueOf(type);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
