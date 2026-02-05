package be.wiserisk.hlabmonitor.monitor.application.port.in.query;

import be.wiserisk.hlabmonitor.monitor.domain.enums.StatisticType;
import be.wiserisk.hlabmonitor.monitor.domain.model.Statistics;

import java.util.List;

public interface GetCheckStatisticsUseCase {
    Statistics getStatistics(List<StatisticType> statisticTypes);
}
