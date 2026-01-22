package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.scheduler;

import be.wiserisk.hlabmonitor.monitor.application.port.out.CheckTriggerCallback;
import be.wiserisk.hlabmonitor.monitor.application.port.out.MonitoringSchedulerPort;
import be.wiserisk.hlabmonitor.monitor.application.port.out.ScheduleHandle;
import be.wiserisk.hlabmonitor.monitor.domain.model.Target;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;

import java.util.concurrent.ScheduledFuture;

@Slf4j
@AllArgsConstructor
public class SchedulerAdapter implements MonitoringSchedulerPort {
    private final ThreadPoolTaskScheduler scheduler;
    private final TaskExecutor checkExecutor;

    @Override
    public ScheduleHandle scheduleTarget(Target target, CheckTriggerCallback callback) {
        Runnable scheduledTask = () -> checkExecutor.execute(() -> {
            try {
                callback.onTrigger(target.id());
            } catch (Exception e) {
                log.error("Check execution failed: {}", e.getMessage(), e);
            }
        });

        PeriodicTrigger trigger = new PeriodicTrigger(target.interval());
        trigger.setFixedRate(false);

        ScheduledFuture<?> future = scheduler.schedule(scheduledTask, trigger);
        return new SpringScheduleHandle(target.id().id(), future);
    }

    @Override
    public void unschedule(ScheduleHandle handle) {
        if (handle != null && handle.isActive()) {
            handle.cancel();
        }
    }
}
