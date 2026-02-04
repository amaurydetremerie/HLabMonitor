package be.wiserisk.hlabmonitor.monitor.domain.service;

import be.wiserisk.hlabmonitor.monitor.application.port.out.PersistencePort;
import be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringType;
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
class GetTargetIdServiceTest {

    private static final TargetId TARGET_ID = new TargetId("targetId");

    @InjectMocks
    private GetTargetIdService getTargetIdService;

    @Mock
    private PersistencePort persistencePort;

    @Test
    void getAllTargetIds() {
        when(persistencePort.getAllTargetIds()).thenReturn(List.of(TARGET_ID));

        assertThat(getTargetIdService.getAllTargetIds()).isNotNull().containsExactly(TARGET_ID);
    }

    @Test
    void getTargetIdByType() {
        when(persistencePort.getAllTargetIdsByMonitoringType(PING)).thenReturn(List.of(TARGET_ID));

        assertThat(getTargetIdService.getTargetIdByType(PING)).isNotNull().containsExactly(TARGET_ID);
    }

}