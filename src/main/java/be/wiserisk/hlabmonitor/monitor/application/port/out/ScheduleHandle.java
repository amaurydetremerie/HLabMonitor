package be.wiserisk.hlabmonitor.monitor.application.port.out;

public interface ScheduleHandle {
    String getTargetId();
    void cancel();
    boolean isActive();
}