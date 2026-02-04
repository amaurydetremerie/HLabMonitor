package be.wiserisk.hlabmonitor.monitor.domain.model;

import be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringResult;
import be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public record CheckResultsFilter(Instant from,
                                 Instant to,
                                 List<TargetId> targetIdList,
                                 List<MonitoringResult> monitoringResultList,
                                 List<MonitoringType> monitoringTypeList) {
}
