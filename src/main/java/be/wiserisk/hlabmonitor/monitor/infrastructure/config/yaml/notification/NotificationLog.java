package be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.notification;

import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.boot.logging.LogLevel;

public record NotificationLog(boolean enabled, LogLevel level, NotificationType notificationType) {

    @ConstructorBinding
    public NotificationLog(Boolean enabled, LogLevel level, Boolean firing, Boolean resolved, Boolean failed) {
        this(!Boolean.FALSE.equals(enabled), level, new NotificationType(firing, resolved, failed));
    }
}
