package be.wiserisk.hlabmonitor.monitor.application.port.in.query;

import be.wiserisk.hlabmonitor.monitor.domain.model.*;

import java.util.List;

public interface GetCheckResultsUseCase {
    List<TargetResult> getAllResults();
    PageResponse<TargetResult> getFilteredResults(CheckResultsFilter filter, PageRequest pageRequest);
    List<TargetResult> getTargetIdResults(TargetId targetId);
}
