package be.wiserisk.hlabmonitor.monitor.domain.service;

import be.wiserisk.hlabmonitor.monitor.application.port.out.PersistencePort;
import be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringResult;
import be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringType;
import be.wiserisk.hlabmonitor.monitor.domain.enums.StatisticType;
import be.wiserisk.hlabmonitor.monitor.domain.exception.UnsupportedEnumException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetStatisticsServiceTest {

    @InjectMocks
    private GetStatisticsService getStatisticsService;

    @Mock
    private PersistencePort persistencePort;

    @Test
    void getAllStatistics() {
        when(persistencePort.countTarget()).thenReturn(1L);
        when(persistencePort.countTarget(any(MonitoringType.class))).thenReturn(1L);
        when(persistencePort.countLast24hResults()).thenReturn(1L);
        when(persistencePort.countLast24hResults(any(MonitoringResult.class))).thenReturn(1L);

        assertThat(getStatisticsService.getStatistics(List.of(StatisticType.GENERAL))).isNotNull();
    }

    @Test
    void getTARGETStatistics() {
        when(persistencePort.countTarget()).thenReturn(1L);

        assertThat(getStatisticsService.getStatistics(List.of(StatisticType.TARGET))).isNotNull();
    }

    @Test
    void getUNKNOWNStatistics() {
        List<StatisticType> statisticTypes = List.of(StatisticType.TARGET);
        assertThatThrownBy(() -> getStatisticsService.getStatistics(statisticTypes)).isInstanceOf(UnsupportedEnumException.class);
    }

}