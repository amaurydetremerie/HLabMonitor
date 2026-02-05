package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.in.rest;

import be.wiserisk.hlabmonitor.monitor.application.port.in.query.GetCheckStatisticsUseCase;
import be.wiserisk.hlabmonitor.monitor.domain.enums.StatisticType;
import be.wiserisk.hlabmonitor.monitor.domain.model.Statistics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static be.wiserisk.hlabmonitor.monitor.domain.enums.StatisticType.GENERAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckStatisticsControllerTest {

    @InjectMocks
    private CheckStatisticsController checkStatisticsController;

    @Mock
    private GetCheckStatisticsUseCase getCheckStatisticsUseCase;

    @Test
    void getStatistics() {
        List<StatisticType> statisticTypes = List.of(GENERAL);

        when(getCheckStatisticsUseCase.getStatistics(statisticTypes)).thenReturn(new Statistics(GENERAL, Collections.emptyMap()));

        assertThat(checkStatisticsController.getStatistics(statisticTypes)).isNotNull();
    }

}