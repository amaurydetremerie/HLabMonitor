package be.wiserisk.hlabmonitor.monitor.application.port.in.query;

import be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringType;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetId;

import java.util.List;

public interface GetCheckTargetIdsUseCase {
    List<TargetId> getAllTargetIds();

    List<TargetId> getTargetIdByType(MonitoringType monitoringType);
}
