package be.wiserisk.hlabmonitor.monitor.domain.exception;

import be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringType;
import be.wiserisk.hlabmonitor.monitor.domain.enums.StatisticType;

public class UnsupportedEnumException extends RuntimeException {
    public UnsupportedEnumException(StatisticType statisticType) {
        super("Statistic type " + statisticType + " not supported");
    }

    public UnsupportedEnumException(MonitoringType type) {
        super("Monitoring type " + type + " not supported");
    }
}
