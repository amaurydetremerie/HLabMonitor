package be.wiserisk.hlabmonitor.monitor.application.port.out;

import be.wiserisk.hlabmonitor.monitor.domain.model.Target;

public interface MonitoringSchedulerPort {
    ScheduleHandle scheduleTarget(Target target, CheckTriggerCallback callback);
    void unschedule(ScheduleHandle handle);
}
