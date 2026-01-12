package be.wiserisk.hlabmonitor.monitor.application.port.out;

import be.wiserisk.hlabmonitor.monitor.domain.model.*;

import java.util.List;

public interface PersistencePort {
    void saveResult(TargetResult targetResult);

    Target getTarget(TargetId targetId);

    List<TargetResult> getAllTargetResults();

    PageResponse<TargetResult> getAllResultsFilteredBy(CheckResultsFilter filter, PageRequest pageRequest);

    List<TargetResult> getAllTargetResultsByTargetId(TargetId targetId);

    boolean exist(TargetId targetId);
}
