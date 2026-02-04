package be.wiserisk.hlabmonitor.monitor.domain.model;

import be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringResult;

import java.time.Instant;

public record TargetResult(TargetId id, MonitoringResult result, String message, Instant checkedAt) {

    public TargetResult(TargetId id, MonitoringResult result, String message) {
        this(id, result, message, Instant.now());
    }
}
