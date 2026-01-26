package be.wiserisk.hlabmonitor.monitor.application.port.out;

import java.util.concurrent.ScheduledFuture;

public interface ScheduleHandle {
    String getTargetId();
    ScheduledFuture<?> getFuture();
    void cancel();
    boolean isActive();
}