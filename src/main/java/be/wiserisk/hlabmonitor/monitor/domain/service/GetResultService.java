package be.wiserisk.hlabmonitor.monitor.domain.service;

import be.wiserisk.hlabmonitor.monitor.application.port.in.query.GetCheckResultsUseCase;
import be.wiserisk.hlabmonitor.monitor.application.port.out.PersistencePort;
import be.wiserisk.hlabmonitor.monitor.domain.model.*;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class GetResultService implements GetCheckResultsUseCase {

    PersistencePort persistencePort;

    @Override
    public List<TargetResult> getAllResults() {
        return persistencePort.getAllTargetResults();
    }

    @Override
    public PageResponse<TargetResult> getFilteredResults(CheckResultsFilter filter, PageRequest pageRequest) {
        return persistencePort.getAllResultsFilteredBy(filter, pageRequest);
    }

    @Override
    public List<TargetResult> getTargetIdResults(TargetId targetId) {
        if(!persistencePort.isTargetIdExist(targetId)) {
            //Use dedicated exception
            throw new RuntimeException("Target id not found");
        }
        return persistencePort.getAllTargetResultsByTargetId(targetId);
    }
}
