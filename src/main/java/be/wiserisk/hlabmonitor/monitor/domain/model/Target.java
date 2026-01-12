package be.wiserisk.hlabmonitor.monitor.domain.model;

import be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringType;

public record Target(TargetId id, MonitoringType type, String target) {
}