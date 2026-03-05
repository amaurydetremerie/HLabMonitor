package be.wiserisk.hlabmonitor.monitor.domain.enums;

import lombok.Getter;

@Getter
public enum MonitoringResult {
    SUCCESS(Family.SUCCESS),
    FAILURE(Family.FAILURE),
    WARNING(Family.FAILURE),
    ERROR(Family.FAILURE),
    UNKNOWN(Family.UNKNOWN);

    private final Family family;

    MonitoringResult(Family family) {
        this.family = family;
    }

    public enum Family {
        SUCCESS,
        FAILURE,
        UNKNOWN
    }
}
