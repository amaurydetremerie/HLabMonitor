package be.wiserisk.hlabmonitor.monitor.domain.model;

import be.wiserisk.hlabmonitor.monitor.domain.enums.StatisticType;

import java.util.Map;

public record Statistics(StatisticType type, Map<StatisticType, Long> statistics) {
}
