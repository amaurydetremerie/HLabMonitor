package be.wiserisk.hlabmonitor.monitor.domain.service;

import be.wiserisk.hlabmonitor.monitor.application.port.in.execution.ExecuteCheckUseCase;
import be.wiserisk.hlabmonitor.monitor.application.port.out.CheckTriggerCallback;
import be.wiserisk.hlabmonitor.monitor.application.port.out.MonitoringSchedulerPort;
import be.wiserisk.hlabmonitor.monitor.application.port.out.PersistencePort;
import be.wiserisk.hlabmonitor.monitor.application.port.out.ScheduleHandle;
import be.wiserisk.hlabmonitor.monitor.domain.model.Target;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;

import static be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringType.PING;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManageServiceTest {

    public static final String TARGET_STRING = "target";
    public static final Duration INTERVAL = Duration.ofSeconds(30);
    public static final String TARGET_ID_STRING = "ping-1";
    public static final TargetId TARGET_ID = new TargetId(TARGET_ID_STRING);
    public static final Target TARGET = new Target(TARGET_ID, PING, TARGET_STRING, INTERVAL);

    @Mock
    private PersistencePort persistencePort;

    @Mock
    private MonitoringSchedulerPort schedulerPort;

    @Mock
    private ExecuteCheckUseCase executeCheckUseCase;

    @Spy
    @InjectMocks
    private ManageService service;

    @Test
    void syncFullConfiguration_shouldThrowErrorIfDuplicate() {
        List<Target> targetList = List.of(TARGET, TARGET);
        assertThrows(IllegalArgumentException.class, () -> service.syncFullConfiguration(targetList));
    }

    @Test
    void syncFullConfiguration_shouldSyncEachTargetAndRefreshMonitoring() {
        doNothing().when(service).syncTarget(TARGET);
        doNothing().when(service).refreshMonitoredTargets(List.of(TARGET));

        List<Target> targetList = List.of(TARGET);
        service.syncFullConfiguration(targetList);

        verify(service).syncTarget(TARGET);
        verify(service).refreshMonitoredTargets(targetList);
    }

    @Test
    void syncFullConfiguration_shouldHandleMultipleTargets() {
        Target target2 = new Target(new TargetId("ping-2"), PING, "target2", INTERVAL);
        List<Target> targetList = List.of(TARGET, target2);

        doNothing().when(service).syncTarget(TARGET);
        doNothing().when(service).syncTarget(target2);
        doNothing().when(service).refreshMonitoredTargets(targetList);

        service.syncFullConfiguration(targetList);

        verify(service).syncTarget(TARGET);
        verify(service).syncTarget(target2);
        verify(service).refreshMonitoredTargets(targetList);
    }

    @Test
    void syncTarget_shouldUpdateExistingTarget_whenTargetExists() {
        when(persistencePort.isTargetIdExist(TARGET_ID)).thenReturn(true);

        doCallRealMethod().when(service).syncTarget(TARGET);

        service.syncTarget(TARGET);

        verify(persistencePort).isTargetIdExist(TARGET_ID);
        verify(persistencePort).updateTarget(TARGET);
        verify(persistencePort, never()).createTarget(TARGET);
    }

    @Test
    void syncTarget_shouldSaveNewTarget_whenTargetDoesNotExist() {
        when(persistencePort.isTargetIdExist(TARGET_ID)).thenReturn(false);

        doCallRealMethod().when(service).syncTarget(TARGET);

        service.syncTarget(TARGET);

        verify(persistencePort).isTargetIdExist(TARGET_ID);
        verify(persistencePort).createTarget(TARGET);
        verify(persistencePort, never()).updateTarget(TARGET);
    }

    @Test
    void updateAndRefreshExistingTarget_shouldCallUpdateAndRefresh() {
        doNothing().when(service).refreshMonitoredTarget(TARGET);

        service.updateAndRefreshExistingTarget(TARGET);

        verify(persistencePort).updateTarget(TARGET);
        verify(service).refreshMonitoredTarget(TARGET);
    }

    @Test
    void saveNewAndRefreshTarget_shouldCallCreateAndRefresh() {
        doNothing().when(service).refreshMonitoredTarget(TARGET);

        service.saveNewAndRefreshTarget(TARGET);

        verify(persistencePort).createTarget(TARGET);
        verify(service).refreshMonitoredTarget(TARGET);
    }

    @Test
    void refreshMonitoredTarget_shouldUnscheduleAndRescheduleTarget() {
        ScheduleHandle oldHandle = mock(ScheduleHandle.class);
        ScheduleHandle newHandle = mock(ScheduleHandle.class);

        when(schedulerPort.scheduleTarget(eq(TARGET), any(CheckTriggerCallback.class)))
                .thenReturn(oldHandle)
                .thenReturn(newHandle);

        service.scheduleTargetMonitoring(TARGET);

        doCallRealMethod().when(service).refreshMonitoredTarget(TARGET);

        service.refreshMonitoredTarget(TARGET);

        verify(schedulerPort).unschedule(oldHandle);
        verify(schedulerPort, times(2)).scheduleTarget(eq(TARGET), any(CheckTriggerCallback.class));
    }

    @Test
    void refreshMonitoredTargets_shouldCancelExistingSchedulesAndCreateNewOnes() {
        ScheduleHandle oldHandle = mock(ScheduleHandle.class);
        ScheduleHandle newHandle = mock(ScheduleHandle.class);

        when(schedulerPort.scheduleTarget(eq(TARGET), any(CheckTriggerCallback.class)))
                .thenReturn(oldHandle)
                .thenReturn(newHandle);

        service.scheduleTargetMonitoring(TARGET);
        List<Target> targetList = List.of(TARGET);

        doCallRealMethod().when(service).refreshMonitoredTargets(targetList);

        service.refreshMonitoredTargets(targetList);

        verify(oldHandle).cancel();
        verify(schedulerPort, times(2)).scheduleTarget(eq(TARGET), any(CheckTriggerCallback.class));
    }

    @Test
    void refreshMonitoredTargets_shouldClearAllActiveSchedules() {
        ScheduleHandle handle1 = mock(ScheduleHandle.class);
        ScheduleHandle handle2 = mock(ScheduleHandle.class);

        Target target2 = new Target(new TargetId("ping-2"), PING, "target2", INTERVAL);

        when(schedulerPort.scheduleTarget(eq(TARGET), any(CheckTriggerCallback.class))).thenReturn(handle1);
        service.scheduleTargetMonitoring(TARGET);
        when(schedulerPort.scheduleTarget(eq(target2), any(CheckTriggerCallback.class))).thenReturn(handle2);
        service.scheduleTargetMonitoring(target2);

        doCallRealMethod().when(service).refreshMonitoredTargets(List.of());

        service.refreshMonitoredTargets(List.of());

        verify(handle1).cancel();
        verify(handle2).cancel();
    }

    @Test
    void stopMonitoring_shouldUnscheduleTarget() {
        ScheduleHandle scheduleHandle = mock(ScheduleHandle.class);

        when(schedulerPort.scheduleTarget(eq(TARGET), any(CheckTriggerCallback.class))).thenReturn(scheduleHandle);

        service.scheduleTargetMonitoring(TARGET);

        doCallRealMethod().when(service).stopMonitoring(TARGET_ID);

        service.stopMonitoring(TARGET_ID);

        verify(schedulerPort).unschedule(scheduleHandle);
    }

    @Test
    void resumeMonitoring_shouldRetrieveTargetAndScheduleIt() {
        ScheduleHandle scheduleHandle = mock(ScheduleHandle.class);

        when(persistencePort.getTarget(TARGET_ID)).thenReturn(TARGET);
        when(schedulerPort.scheduleTarget(eq(TARGET), any(CheckTriggerCallback.class))).thenReturn(scheduleHandle);

        doCallRealMethod().when(service).resumeMonitoring(TARGET_ID);

        service.resumeMonitoring(TARGET_ID);

        verify(persistencePort).getTarget(TARGET_ID);
        verify(schedulerPort).scheduleTarget(eq(TARGET), any(CheckTriggerCallback.class));
    }

    @Test
    void reloadAllMonitoring_shouldCancelAllAndReloadFromPersistence() {
        ScheduleHandle oldHandle = mock(ScheduleHandle.class);
        ScheduleHandle newHandle = mock(ScheduleHandle.class);

        when(oldHandle.getTargetId()).thenReturn(TARGET_ID_STRING);
        when(schedulerPort.scheduleTarget(eq(TARGET), any(CheckTriggerCallback.class)))
                .thenReturn(oldHandle)
                .thenReturn(newHandle);
        when(persistencePort.getAllTargets(List.of(TARGET_ID))).thenReturn(List.of(TARGET));

        service.scheduleTargetMonitoring(TARGET);

        doNothing().when(service).refreshMonitoredTargets(List.of(TARGET));

        service.reloadAllMonitoring();

        verify(persistencePort).getAllTargets(List.of(TARGET_ID));
        verify(service).refreshMonitoredTargets(List.of(TARGET));
    }
}