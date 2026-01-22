package be.wiserisk.hlabmonitor.monitor.domain.service;

import be.wiserisk.hlabmonitor.monitor.application.port.out.CheckTargetPort;
import be.wiserisk.hlabmonitor.monitor.application.port.out.PersistencePort;
import be.wiserisk.hlabmonitor.monitor.domain.model.Target;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringType.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MonitoringServiceTest {

    public static final TargetId TARGET_ID = new TargetId("TargetId");
    public static final String TARGET = "target";
    @InjectMocks
    private MonitoringService monitoringService;

    @Mock
    private CheckTargetPort checkPort;
    @Mock
    private PersistencePort persistencePort;

    @Test
    void executeCheckHttp() {
        Target target = new Target(TARGET_ID, HTTP, TARGET, Duration.ofMinutes(1));

        when(persistencePort.getTarget(TARGET_ID)).thenReturn(target);

        assertDoesNotThrow(() -> monitoringService.executeCheck(TARGET_ID));
        verify(checkPort, times(1)).httpCheck(target);
    }

    @Test
    void executeCheckPing() {
        Target target = new Target(TARGET_ID, PING, TARGET, Duration.ofMinutes(1));

        when(persistencePort.getTarget(TARGET_ID)).thenReturn(target);

        assertDoesNotThrow(() -> monitoringService.executeCheck(TARGET_ID));
        verify(checkPort, times(1)).ping(target);
    }

    @Test
    void executeCheckCertificate() {
        Target target = new Target(TARGET_ID, CERTIFICATE, TARGET, Duration.ofMinutes(1));

        when(persistencePort.getTarget(TARGET_ID)).thenReturn(target);

        assertDoesNotThrow(() -> monitoringService.executeCheck(TARGET_ID));
        verify(checkPort, times(1)).certCheck(target);
    }

    @Test
    void executeCheckSpeedTest() {
        Target target = new Target(TARGET_ID, SPEEDTEST, TARGET, Duration.ofMinutes(1));

        when(persistencePort.getTarget(TARGET_ID)).thenReturn(target);

        assertThrows(UnsupportedOperationException.class, () -> monitoringService.executeCheck(TARGET_ID));
    }

    @Test
    void executeCheckUnknown() {
        Target target = new Target(TARGET_ID, UNKNOWN, TARGET, Duration.ofMinutes(1));

        when(persistencePort.getTarget(TARGET_ID)).thenReturn(target);

        assertThrows(RuntimeException.class, () -> monitoringService.executeCheck(TARGET_ID));
    }
}