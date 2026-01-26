package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.in.rest;

import be.wiserisk.hlabmonitor.monitor.application.port.in.management.ManageMonitoringConfigUseCase;
import be.wiserisk.hlabmonitor.monitor.domain.model.Target;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Duration;

import static be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringType.PING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MonitoringManagementController Tests")
class MonitoringManagementControllerTest {

    @Mock
    private ManageMonitoringConfigUseCase manageConfigUseCase;

    @InjectMocks
    private MonitoringManagementController controller;

    public static final String TARGET_STRING = "target";
    public static final Duration INTERVAL = Duration.ofSeconds(30);
    public static final String TARGET_ID_STRING = "ping-1";
    public static final TargetId TARGET_ID = new TargetId(TARGET_ID_STRING);
    public static final Target TARGET = new Target(TARGET_ID, PING, TARGET_STRING, INTERVAL);

    @Test
    void addTarget_shouldCallUseCaseAndReturnAccepted() {
        doNothing().when(manageConfigUseCase).saveNewAndRefreshTarget(TARGET);

        ResponseEntity<Void> response = controller.addTarget(TARGET);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody()).isNull();
        verify(manageConfigUseCase, times(1)).saveNewAndRefreshTarget(TARGET);
    }

    @Test
    void updateTarget_shouldCallUseCaseAndReturnOk() {
        doNothing().when(manageConfigUseCase).updateAndRefreshExistingTarget(TARGET);

        ResponseEntity<Void> response = controller.updateTarget(TARGET);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();
        verify(manageConfigUseCase, times(1)).updateAndRefreshExistingTarget(TARGET);
    }

    @Test
    void stopMonitoring_shouldCallUseCaseAndReturnOk() {
        doNothing().when(manageConfigUseCase).stopMonitoring(TARGET_ID);

        ResponseEntity<Void> response = controller.stopMonitoring(TARGET_ID_STRING);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();
        verify(manageConfigUseCase, times(1)).stopMonitoring(TARGET_ID);
    }

    @Test
    void resumeMonitoring_shouldCallUseCaseAndReturnOk() {
        doNothing().when(manageConfigUseCase).resumeMonitoring(TARGET_ID);

        ResponseEntity<Void> response = controller.resumeMonitoring(TARGET_ID_STRING);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();
        verify(manageConfigUseCase, times(1)).resumeMonitoring(TARGET_ID);
    }

    @Test
    void reloadConfiguration_shouldCallUseCaseAndReturnOk() {
        doNothing().when(manageConfigUseCase).reloadAllMonitoring();

        ResponseEntity<Void> response = controller.reloadConfiguration();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();
        verify(manageConfigUseCase, times(1)).reloadAllMonitoring();
    }
}