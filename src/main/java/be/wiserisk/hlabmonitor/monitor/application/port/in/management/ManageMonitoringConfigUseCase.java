package be.wiserisk.hlabmonitor.monitor.application.port.in.management;

import be.wiserisk.hlabmonitor.monitor.domain.model.Target;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetId;

import java.util.List;

public interface ManageMonitoringConfigUseCase {

    void syncFullConfiguration(List<Target> targetList);

    void syncTarget(Target target);

    void refreshMonitoredTargets(List<Target> targetList);

    void refreshMonitoredTarget(Target target);

    void updateAndRefreshExistingTarget(Target target);

    void saveNewAndRefreshTarget(Target target);

    void stopMonitoring(TargetId targetId);

    void resumeMonitoring(TargetId targetId);

    void reloadAllMonitoring();
}
