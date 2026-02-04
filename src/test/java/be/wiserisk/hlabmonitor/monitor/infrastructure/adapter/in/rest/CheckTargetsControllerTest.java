package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.in.rest;

import be.wiserisk.hlabmonitor.monitor.application.port.in.query.GetCheckTargetIdsUseCase;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringType.PING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckTargetsControllerTest {

    private static final TargetId TARGET_ID = new TargetId("targetId");

    @InjectMocks
    private CheckTargetsController checkTargetsController;

    @Mock
    private GetCheckTargetIdsUseCase getCheckTargetIdsUseCase;

    @Test
    void getAll() {
        when(getCheckTargetIdsUseCase.getAllTargetIds()).thenReturn(List.of(TARGET_ID));

        assertThat(checkTargetsController.getAll()).isNotNull().containsExactly(TARGET_ID);
    }

    @Test
    void getAllByType() {
        when(getCheckTargetIdsUseCase.getTargetIdByType(PING)).thenReturn(List.of(TARGET_ID));

        assertThat(checkTargetsController.getAllByType(PING)).isNotNull().containsExactly(TARGET_ID);
    }

}