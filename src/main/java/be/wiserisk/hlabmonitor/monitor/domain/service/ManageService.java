package be.wiserisk.hlabmonitor.monitor.domain.service;

import be.wiserisk.hlabmonitor.monitor.application.port.in.execution.ExecuteCheckUseCase;
import be.wiserisk.hlabmonitor.monitor.application.port.in.management.ManageMonitoringConfigUseCase;
import be.wiserisk.hlabmonitor.monitor.application.port.out.MonitoringSchedulerPort;
import be.wiserisk.hlabmonitor.monitor.application.port.out.PersistencePort;
import be.wiserisk.hlabmonitor.monitor.application.port.out.ScheduleHandle;
import be.wiserisk.hlabmonitor.monitor.domain.model.Target;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetId;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@AllArgsConstructor
public class ManageService implements ManageMonitoringConfigUseCase {

    private final PersistencePort persistencePort;
    private final MonitoringSchedulerPort schedulerPort;
    private final ExecuteCheckUseCase executeCheckUseCase;

    private final Map<String, ScheduleHandle> activeSchedules = new ConcurrentHashMap<>();

    @Override
    public void syncFullConfiguration(List<Target> targetList) {
        targetList.parallelStream().forEach(this::syncTarget);
        refreshMonitoredTargets(targetList);
    }

    @Override
    public void syncTarget(Target target) {
        if (persistencePort.isTargetIdExist(target.id()))
            updateExistingTarget(target);
        else
            saveNewTarget(target);
    }

    @Override
    public void refreshMonitoredTargets(List<Target> targetList) {
        activeSchedules.values().parallelStream().forEach(ScheduleHandle::cancel);
        activeSchedules.clear();

        targetList.parallelStream().forEach(this::scheduleTargetMonitoring);
    }

    @Override
    public void refreshMonitoredTarget(Target target) {
        unscheduleTarget(target.id());
        scheduleTargetMonitoring(target);
    }

    private void updateExistingTarget(Target target) {
        persistencePort.updateTarget(target);
    }

    private void saveNewTarget(Target target) {
        persistencePort.createTarget(target);
    }

    @Override
    public void updateAndRefreshExistingTarget(Target target) {
        updateExistingTarget(target);
        refreshMonitoredTarget(target);
    }

    @Override
    public void saveNewAndRefreshTarget(Target target) {
        saveNewTarget(target);
        refreshMonitoredTarget(target);
    }

    @Override
    public void stopMonitoring(TargetId targetId) {
        unscheduleTarget(targetId);
    }

    @Override
    public void resumeMonitoring(TargetId targetId) {
        scheduleTargetMonitoring(persistencePort.getTarget(targetId));
    }

    @Override
    public void reloadAllMonitoring() {
        refreshMonitoredTargets(persistencePort.getAllTargets(getAllActiveTargets()));
    }

    private List<TargetId> getAllActiveTargets() {
        return activeSchedules.values()
                .parallelStream()
                .map(ScheduleHandle::getTargetId)
                .map(TargetId::new)
                .toList();
    }

    void scheduleTargetMonitoring(Target target) {
        ScheduleHandle handle = schedulerPort.scheduleTarget(target, executeCheckUseCase::executeCheck);
        activeSchedules.put(target.id().id(), handle);
    }

    private void unscheduleTarget(TargetId targetId) {
        ScheduleHandle handle = activeSchedules.remove(targetId.id());
        schedulerPort.unschedule(handle);
    }
}
