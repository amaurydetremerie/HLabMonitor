package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.in.notification;

import be.wiserisk.hlabmonitor.monitor.domain.model.Notification;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.Monitoring;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.notification.NotificationType;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

@AllArgsConstructor
public class EmailSender implements NotificationSender {

    private final JavaMailSender mailSender;
    private final String from;
    private final String to;

    @Override
    public void sendNotification(Notification notification) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(getSubject(notification));
            helper.setText(getBody(notification), true);

            mailSender.send(message);
        } catch (MessagingException e) {
            // log ici
        }
    }

    @Override
    public NotificationType getNotificationType(Monitoring monitoring) {
        return monitoring.getNotification().notificationEmail().notificationType();
    }

    private String getSubject(Notification notification) {
        return switch (notification.notificationStatus()) {
            case SEND       -> "[FIRING] HLab Monitor — Target " + notification.targetId().id();
            case TERMINATED -> "[RESOLVED] HLab Monitor — Target " + notification.targetId().id();
            default         -> "[FAILED] HLab Monitor — Target " + notification.targetId().id();
        };
    }

    private String getBody(Notification notification) {
        String targetId = notification.targetId().id();
        return switch (notification.notificationStatus()) {
            case SEND -> """
                <h2>🔴 [FIRING]</h2>
                <p>Notification for target <b>%s</b> has been fired.</p>
                <hr/>
                <small>HLab Monitor — <a href="https://github.com/amaurydetremerie/HLabMonitor">GitHub</a></small>
                """.formatted(targetId);

            case TERMINATED -> """
                <h2>🟢 [RESOLVED]</h2>
                <p>Notification for target <b>%s</b> has been resolved.</p>
                <hr/>
                <small>HLab Monitor — <a href="https://github.com/amaurydetremerie/HLabMonitor">GitHub</a></small>
                """.formatted(targetId);

            default -> """
                <h2>⚠️ [FAILED]</h2>
                <p>An error occurred while sending notification for target <b>%s</b>.</p>
                <hr/>
                <small>HLab Monitor — <a href="https://github.com/amaurydetremerie/HLabMonitor">GitHub</a></small>
                """.formatted(targetId);
        };
    }
}
