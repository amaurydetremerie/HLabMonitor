package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.in.rest;

import be.wiserisk.hlabmonitor.monitor.application.port.in.query.GetCheckResultsUseCase;
import be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringResult;
import be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringType;
import be.wiserisk.hlabmonitor.monitor.domain.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringResult.SUCCESS;
import static be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringType.HTTP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckResultsControllerTest {

    public static final String TARGET_ID_STRING = "targetId";
    public static final TargetId TARGET_ID = new TargetId(TARGET_ID_STRING);

    @InjectMocks
    private CheckResultsController checkResultsController;

    @Mock
    private GetCheckResultsUseCase getCheckResultsUseCase;

    @Test
    void getAll() {
        TargetResult targetResult = new TargetResult(TARGET_ID, SUCCESS, "");
        List<TargetResult> targetResults = List.of(targetResult);

        when(getCheckResultsUseCase.getAllResults()).thenReturn(targetResults);

        assertThat(checkResultsController.getAll()).isNotNull().isNotEmpty().isEqualTo(targetResults);
    }

    @Test
    void getAllByTargetId() {
        TargetResult targetResult = new TargetResult(TARGET_ID, SUCCESS, "");
        List<TargetResult> targetResults = List.of(targetResult);

        when(getCheckResultsUseCase.getTargetIdResults(TARGET_ID)).thenReturn(targetResults);

        assertThat(checkResultsController.getAllByTargetId(TARGET_ID_STRING)).isNotNull().isNotEmpty().isEqualTo(targetResults);
    }

    @Test
    void getAllFiltered() {
        String targetId = "targetId";
        List<String> targetIdStringList = List.of(targetId);
        List<TargetId> targetIdList = List.of(new TargetId(targetId));
        Instant now = Instant.now();
        List<MonitoringResult> monitoringResultList = List.of(SUCCESS);
        List<MonitoringType> monitoringTypeList = List.of(HTTP);
        int size = 20;
        int page = 0;
        List<TargetResult> targetResultList = List.of();

        CheckResultsFilter filter = new CheckResultsFilter(now, now, targetIdList, monitoringResultList, monitoringTypeList);
        PageRequest pageRequest = new PageRequest(page, size);
        PageResponse<TargetResult> pageResponse = new PageResponse<>(targetResultList, page, size, 0, false);

        when(getCheckResultsUseCase.getFilteredResults(filter, pageRequest)).thenReturn(pageResponse);

        assertThat(checkResultsController.getAllFiltered(now, now, targetIdStringList, monitoringResultList, monitoringTypeList, size, page)).isNotNull().isEqualTo(pageResponse);
    }

}