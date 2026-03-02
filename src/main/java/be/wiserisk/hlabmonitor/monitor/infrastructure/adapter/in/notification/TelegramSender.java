package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.in.notification;

import be.wiserisk.hlabmonitor.monitor.domain.model.Notification;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.Monitoring;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.notification.NotificationType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

import javax.net.ssl.HttpsURLConnection;
import java.net.URI;
import java.util.Map;

@AllArgsConstructor
public class TelegramSender implements NotificationSender{

    private final ObjectMapper objectMapper;
    private final String token;
    private final String chatId;

    @Override
    public void sendNotification(Notification notification) {
        try {
            HttpsURLConnection connection = (HttpsURLConnection)
                    new URI("https://api.telegram.org/bot" + token + "/sendMessage")
                            .toURL().openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            connection.getOutputStream().write(
                    objectMapper.writeValueAsBytes(getMessage(notification))
            );
            connection.getInputStream().close();

        } catch (Exception e) {
            // log ici
        }
    }

    @Override
    public NotificationType getNotificationType(Monitoring monitoring) {
        return monitoring.getNotification().notificationTelegram().notificationType();
    }

    private Map<String, Object> getMessage(Notification notification) {
        return Map.of(
                "chat_id", chatId,
                "parse_mode", "HTML",
                "text", buildText(notification)
        );
    }

    private String buildText(Notification notification) {
        String targetId = notification.targetId().id();
        return switch (notification.notificationStatus()) {
            case SEND -> """
                🔴 <b>[FIRING]</b> — HLab Monitor
                Notification for target <code>%s</code> has been fired.
                """.formatted(targetId);

            case TERMINATED -> """
                🟢 <b>[RESOLVED]</b> — HLab Monitor
                Notification for target <code>%s</code> has been resolved.
                """.formatted(targetId);

            default -> """
                ⚠️ <b>[FAILED]</b> — HLab Monitor
                An error occurred while sending notification for target <code>%s</code>.
                """.formatted(targetId);
        };
    }
}
