package be.wiserisk.hlabmonitor.monitor.domain.model;

import be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringType;

import java.time.Duration;

public record Target(TargetId id, MonitoringType type, String target, Duration interval) {}