package be.wiserisk.hlabmonitor.monitor.application.port.out;

import java.util.concurrent.ScheduledFuture;

public interface ScheduleHandle {
    String targetId();
    ScheduledFuture<?> future();
    void cancel();
    boolean isActive();
}