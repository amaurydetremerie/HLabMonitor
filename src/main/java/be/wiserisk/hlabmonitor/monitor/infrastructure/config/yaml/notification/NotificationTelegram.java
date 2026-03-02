package be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.notification;

import org.springframework.boot.context.properties.bind.ConstructorBinding;

public record NotificationTelegram(boolean enabled, String token, String chatId, NotificationType notificationType) {

    @ConstructorBinding
    public NotificationTelegram(Boolean enabled, String token, String chatId, Boolean firing, Boolean resolved, Boolean failed) {
        this(!Boolean.FALSE.equals(enabled), token, chatId, new NotificationType(firing, resolved, failed));
    }
}
