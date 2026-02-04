package be.wiserisk.hlabmonitor.monitor.infrastructure.config.mapper;

import be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringResult;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetId;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetResult;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.entity.ResultEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ResultMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "targetId", source = "id")
    @Mapping(target = "result", source = "result")
    @Mapping(target = "checkedAt", source = "checkedAt")
    ResultEntity toEntity(TargetResult t);

    @Mapping(target = "id", source = "targetId")
    @Mapping(target = "result", source = "result")
    @Mapping(target = "checkedAt", source = "checkedAt")
    TargetResult toDomain(ResultEntity e);

    default String map(TargetId id) {
        return id.id();
    }
    default TargetId map(String id) {
        return new TargetId(id);
    }

    default String map(MonitoringResult result) {
        return result == null ? null : result.name();
    }
    default MonitoringResult mapMonitoringType(String result) {
        if (result == null) return MonitoringResult.UNKNOWN;
        try {
            return MonitoringResult.valueOf(result);
        } catch (IllegalArgumentException ex) {
            return MonitoringResult.UNKNOWN;
        }
    }
}
