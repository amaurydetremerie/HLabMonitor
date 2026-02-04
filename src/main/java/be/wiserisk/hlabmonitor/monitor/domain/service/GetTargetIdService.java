package be.wiserisk.hlabmonitor.monitor.domain.service;

import be.wiserisk.hlabmonitor.monitor.application.port.in.query.GetCheckResultsUseCase;
import be.wiserisk.hlabmonitor.monitor.application.port.in.query.GetCheckTargetIdsUseCase;
import be.wiserisk.hlabmonitor.monitor.application.port.out.PersistencePort;
import be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringType;
import be.wiserisk.hlabmonitor.monitor.domain.model.*;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class GetTargetIdService implements GetCheckTargetIdsUseCase {

    PersistencePort persistencePort;

    @Override
    public List<TargetId> getAllTargetIds() {
        return persistencePort.getAllTargetIds();
    }

    @Override
    public List<TargetId> getTargetIdByType(MonitoringType monitoringType) {
        return persistencePort.getAllTargetIdsByMonitoringType(monitoringType);
    }
}
