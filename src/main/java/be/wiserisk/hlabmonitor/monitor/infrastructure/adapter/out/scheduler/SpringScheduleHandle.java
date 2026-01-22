package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.scheduler;

import be.wiserisk.hlabmonitor.monitor.application.port.out.ScheduleHandle;

import java.util.concurrent.ScheduledFuture;

public class SpringScheduleHandle implements ScheduleHandle {

    private final String targetId;
    private final ScheduledFuture<?> future;

    public SpringScheduleHandle(String targetId, ScheduledFuture<?> future) {
        this.targetId = targetId;
        this.future = future;
    }

    @Override
    public String getTargetId() {
        return targetId;
    }

    @Override
    public void cancel() {
        if (isActive()) {
            future.cancel(false);
        }
    }

    @Override
    public boolean isActive() {
        return !future.isCancelled() && !future.isDone();
    }
}
