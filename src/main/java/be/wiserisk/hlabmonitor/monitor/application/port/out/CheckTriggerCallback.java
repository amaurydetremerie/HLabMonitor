package be.wiserisk.hlabmonitor.monitor.application.port.out;

import be.wiserisk.hlabmonitor.monitor.domain.model.TargetId;

@FunctionalInterface
public interface CheckTriggerCallback {
    void onTrigger(TargetId targetId);
}
