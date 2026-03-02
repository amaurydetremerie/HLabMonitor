package be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.notification;

import org.springframework.boot.context.properties.bind.ConstructorBinding;

public record NotificationDiscord(boolean enabled, String webhookUrl, NotificationType notificationType) {

    @ConstructorBinding
     public NotificationDiscord(Boolean enabled, String webhookUrl, Boolean firing, Boolean resolved, Boolean failed) {
         this(!Boolean.FALSE.equals(enabled), webhookUrl, new NotificationType(firing, resolved, failed));
     }
}

