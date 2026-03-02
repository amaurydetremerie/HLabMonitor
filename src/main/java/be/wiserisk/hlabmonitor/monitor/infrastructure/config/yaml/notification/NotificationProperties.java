package be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.notification;

import org.springframework.boot.context.properties.bind.ConstructorBinding;

public record NotificationProperties(boolean enabled,
                                     NotificationEmail notificationEmail,
                                     NotificationDiscord notificationDiscord,
                                     NotificationTelegram notificationTelegram,
                                     NotificationLog notificationLog) {

    @ConstructorBinding
    public NotificationProperties(Boolean enabled,
                                  NotificationEmail email,
                                  NotificationDiscord discord,
                                  NotificationTelegram telegram,
                                  NotificationLog log) {
        this(!Boolean.FALSE.equals(enabled), email, discord, telegram, log);
    }
}