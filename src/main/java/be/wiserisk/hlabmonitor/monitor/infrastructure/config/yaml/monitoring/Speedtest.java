package be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.monitoring;

import be.wiserisk.hlabmonitor.monitor.domain.enums.SpeedtestType;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.time.Duration;

import static be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.monitoring.Common.calculateInterval;

public record Speedtest(String target, Duration interval, SpeedtestType type, Double warningThresholdMbps, Double failureThresholdMbps) implements Common {
    public static final Duration DEFAULT_INTERVAL = Duration.ofHours(1L);
    public static final double DEFAULT_WARNING_THRESHOLD = 500.0;
    public static final double DEFAULT_FAILURE_THRESHOLD = 200.0;

    @ConstructorBinding
    public Speedtest(String target, String interval, String type, Double warningThresholdMbps, Double failureThresholdMbps) {
        this(
            target != null ? target : "",
            calculateInterval(interval, DEFAULT_INTERVAL),
            parseType(type),
            warningThresholdMbps != null ? warningThresholdMbps : DEFAULT_WARNING_THRESHOLD,
            failureThresholdMbps != null ? failureThresholdMbps : DEFAULT_FAILURE_THRESHOLD
        );
    }

    private static SpeedtestType parseType(String type) {
        if (type == null || type.isBlank()) {
            return SpeedtestType.OOKLA;
        }
        try {
            return SpeedtestType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid speedtest type: '" + type + "'. Valid values: ookla, librespeed, openspeedtest, iperf");
        }
    }
}
