package be.wiserisk.hlabmonitor.monitor.domain.model;

import be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringType;
import be.wiserisk.hlabmonitor.monitor.domain.enums.SpeedtestType;

import java.time.Duration;

public record Target(
    TargetId id,
    MonitoringType type,
    String target,
    Duration interval,
    Integer acceptableStatusCode,
    SpeedtestType speedtestType,
    Double warningThresholdMbps,
    Double failureThresholdMbps
) {
    public Target(TargetId id, MonitoringType type, String target, Duration interval, Integer acceptableStatusCode) {
        this(id, type, target, interval, acceptableStatusCode, null, null, null);
    }

    public Target(TargetId id, MonitoringType type, String target, Duration interval) {
        this(id, type, target, interval, 999, null, null, null);
    }

    public Target(TargetId id, String target, Duration interval, SpeedtestType speedtestType, Double warningThresholdMbps, Double failureThresholdMbps) {
        this(id, MonitoringType.SPEEDTEST, target, interval, 999, speedtestType, warningThresholdMbps, failureThresholdMbps);
    }
}
