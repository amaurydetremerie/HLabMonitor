package be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.notification;

public record NotificationType(boolean firing, boolean resolved, boolean failed) {
    public NotificationType() {
        this(true, true, true);
    }

    public NotificationType(Boolean firing, Boolean resolved, Boolean failed) {
        this(!Boolean.FALSE.equals(firing),
                !Boolean.FALSE.equals(resolved),
                !Boolean.FALSE.equals(failed));
    }
}