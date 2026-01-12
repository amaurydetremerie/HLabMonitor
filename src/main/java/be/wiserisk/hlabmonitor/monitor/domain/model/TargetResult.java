package be.wiserisk.hlabmonitor.monitor.domain.model;

import be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringResult;

public record TargetResult(TargetId id, MonitoringResult result, String message) {
}
