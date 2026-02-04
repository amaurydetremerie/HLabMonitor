package be.wiserisk.hlabmonitor.monitor.domain.service;

import be.wiserisk.hlabmonitor.monitor.application.port.out.PersistencePort;
import be.wiserisk.hlabmonitor.monitor.domain.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;

import static be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringResult.FAILURE;
import static be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringResult.SUCCESS;
import static be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetResultServiceTest {

    public static final String TARGET_ID_STRING = "targetId";
    public static final TargetId TARGET_ID = new TargetId(TARGET_ID_STRING);
    public static final int SIZE = 10;
    public static final int PAGE = 0;

    @InjectMocks
    private GetResultService getResultService;

    @Mock
    private PersistencePort persistencePort;

    @Test
    void getAllResults() {
        TargetResult targetResult = new TargetResult(TARGET_ID, SUCCESS, "");
        List<TargetResult> targetResults = List.of(targetResult);

        when(persistencePort.getAllTargetResults()).thenReturn(targetResults);

        assertThat(getResultService.getAllResults()).isNotNull().isNotEmpty().isEqualTo(targetResults);
    }

    @Test
    void getTargetIdResults() {
        TargetResult targetResult = new TargetResult(TARGET_ID, SUCCESS, "");
        List<TargetResult> targetResults = List.of(targetResult);

        when(persistencePort.isTargetIdExist(TARGET_ID)).thenReturn(true);
        when(persistencePort.getAllTargetResultsByTargetId(TARGET_ID)).thenReturn(targetResults);

        assertThat(getResultService.getTargetIdResults(TARGET_ID)).isNotNull().isNotEmpty().isEqualTo(targetResults);
    }

    @Test
    void getTargetIdResults_TargetIdNotFound() {
        when(persistencePort.isTargetIdExist(TARGET_ID)).thenReturn(false);

        assertThatThrownBy(() -> getResultService.getTargetIdResults(TARGET_ID)).isInstanceOf(RuntimeException.class);
    }

    @Test
    void getFilteredResults() {
        TargetResult targetResult = new TargetResult(TARGET_ID, SUCCESS, "");
        List<TargetResult> targetResults = List.of(targetResult);

        CheckResultsFilter filter = new CheckResultsFilter(
                LocalDateTime.of(LocalDate.of(2026, 1, 1), LocalTime.MIDNIGHT).toInstant(ZoneOffset.UTC),
                LocalDateTime.of(LocalDate.of(2026, 12, 31), LocalTime.MIDNIGHT).toInstant(ZoneOffset.UTC),
                List.of(TARGET_ID),
                List.of(SUCCESS, FAILURE),
                List.of(HTTP, PING, CERTIFICATE, SPEEDTEST));
        PageRequest pageRequest = new PageRequest(PAGE, SIZE);
        PageResponse<TargetResult> targetResultsPage = new PageResponse<>(targetResults, PAGE, SIZE, targetResults.size(), false);

        when(persistencePort.getAllResultsFilteredBy(filter, pageRequest)).thenReturn(targetResultsPage);

        assertThat(getResultService.getFilteredResults(filter, pageRequest)).isNotNull().isEqualTo(targetResultsPage);
    }

}