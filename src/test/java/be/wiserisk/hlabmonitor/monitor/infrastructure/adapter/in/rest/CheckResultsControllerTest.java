package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.in.rest;

import be.wiserisk.hlabmonitor.monitor.application.port.in.query.GetCheckResultsUseCase;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetId;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringResult.SUCCESS;
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

}