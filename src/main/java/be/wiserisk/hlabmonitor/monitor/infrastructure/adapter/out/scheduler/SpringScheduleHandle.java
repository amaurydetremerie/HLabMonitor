package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.scheduler;

import be.wiserisk.hlabmonitor.monitor.application.port.out.ScheduleHandle;
import lombok.Getter;

import java.util.concurrent.ScheduledFuture;


public record SpringScheduleHandle(String targetId, ScheduledFuture<?> future) implements ScheduleHandle {

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
