package be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.notification;

import org.springframework.boot.context.properties.bind.ConstructorBinding;

public record NotificationEmail(boolean enabled,
                                String from,
                                String to,
                                NotificationType notificationType,
                                SmtpConfig smtp) {

    @ConstructorBinding
    public NotificationEmail(Boolean enabled, String from, String to, Boolean firing, Boolean resolved, Boolean failed, SmtpConfig smtp) {
        this(!Boolean.FALSE.equals(enabled), from, to == null ? from : to, new NotificationType(firing, resolved, failed), smtp);
    }
}
