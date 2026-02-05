package be.wiserisk.hlabmonitor.monitor.domain.service;

import be.wiserisk.hlabmonitor.monitor.application.port.in.query.GetCheckStatisticsUseCase;
import be.wiserisk.hlabmonitor.monitor.application.port.out.PersistencePort;
import be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringResult;
import be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringType;
import be.wiserisk.hlabmonitor.monitor.domain.enums.StatisticType;
import be.wiserisk.hlabmonitor.monitor.domain.model.*;
import lombok.AllArgsConstructor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static be.wiserisk.hlabmonitor.monitor.domain.enums.StatisticType.*;

@AllArgsConstructor
public class GetStatisticsService implements GetCheckStatisticsUseCase {

    PersistencePort persistencePort;

    @Override
    public Statistics getStatistics(List<StatisticType> statisticTypes) {
        Map<StatisticType, Long> statistics;
        if(statisticTypes == null || statisticTypes.isEmpty() || statisticTypes.contains(GENERAL))
            statistics = getAllStatistics();
        else {
            statistics = new HashMap<>();
            statisticTypes.forEach(statisticType -> statistics.put(statisticType, getStatisticsFor(statisticType)));
        }
        return new Statistics(GENERAL, statistics);
    }

    private Map<StatisticType, Long> getAllStatistics() {
        return Arrays.stream(values()).filter(s -> !s.equals(GENERAL)).collect(Collectors.toMap(Function.identity(), this::getStatisticsFor));
    }

    private Long getStatisticsFor(StatisticType statisticType) {
        return switch (statisticType) {
            case TARGET -> persistencePort.countTarget();
            case RESULT -> persistencePort.countLast24hResults();
            case NOTIFICATION -> -1L;
            case RESULT_SUCCESS -> persistencePort.countLast24hResults(MonitoringResult.SUCCESS);
            case RESULT_FAILURE -> persistencePort.countLast24hResults(MonitoringResult.FAILURE);
            case RESULT_WARNING -> persistencePort.countLast24hResults(MonitoringResult.WARNING);
            case RESULT_ERROR -> persistencePort.countLast24hResults(MonitoringResult.ERROR);
            case TARGET_PING -> persistencePort.countTarget(MonitoringType.PING);
            case TARGET_CERTIFICATE -> persistencePort.countTarget(MonitoringType.CERTIFICATE);
            case TARGET_HTTP -> persistencePort.countTarget(MonitoringType.HTTP);
            case TARGET_SPEEDTEST -> persistencePort.countTarget(MonitoringType.SPEEDTEST);
            case NOTIFICATION_SEND -> -1L;
            case NOTIFICATION_TRIGGER -> -1L;
            default -> throw new UnsupportedOperationException("Unsupported statistic type: " + statisticType);
        };
    }
}
