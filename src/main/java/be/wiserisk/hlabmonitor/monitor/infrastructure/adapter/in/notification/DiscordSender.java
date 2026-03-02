package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.in.notification;

import be.wiserisk.hlabmonitor.monitor.domain.model.Notification;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.Monitoring;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.notification.NotificationType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.messaging.Message;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class DiscordSender implements NotificationSender {

    private final ObjectMapper objectMapper;
    private final String webhookURL;

    @Override
    public void sendNotification(Notification notification) {
        final HttpsURLConnection connection;
        try {
            connection = (HttpsURLConnection) new URI(webhookURL).toURL().openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            connection.getOutputStream().write(getMessage(notification).getBytes());
            connection.getInputStream();
        } catch (URISyntaxException e) {
            //throw new RuntimeException(e);
        } catch (MalformedURLException e) {
            //throw new RuntimeException(e);
        } catch (ProtocolException e) {
            //throw new RuntimeException(e);
        } catch (SecurityException e) {
            //throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            //throw new RuntimeException(e);
        } catch (IOException e) {
            //throw new RuntimeException(e);
        }
    }

    @Override
    public NotificationType getNotificationType(Monitoring monitoring) {
        return monitoring.getNotification().notificationDiscord().notificationType();
    }

    private String getMessage(Notification notification) throws JsonProcessingException {
        Map<String, Object> embeds;
        embeds = getEmbeds(notification);
        return objectMapper.writeValueAsString(
                Map.ofEntries(
                        Map.entry("embeds", List.of(embeds))
                ));
    }

    private Map<String, Object> getEmbeds(Notification notification) {
        Map<String, Object> embeds = new HashMap<>();
        Map<String, String> message = new HashMap<>();
        switch (notification.notificationStatus()){
            case SEND -> {
                embeds.put("author", getAuthor());
                embeds.put("title", "[FIRING]");
                embeds.put("color", 11550002);
                message.put("name", "Notification for target " + notification.targetId().id() + " has been fired");

            }
            case TERMINATED ->  {
                embeds.put("author", getAuthor());
                embeds.put("title", "[RESOLVED]");
                embeds.put("color", 6729778);
                message.put("name", "Notification for target " + notification.targetId().id() + " has been resolved");
            }
            default -> {
                embeds.put("title", "[FAILED]");
                embeds.put("color", 3319216);
                message.put("name", "An error occurred while sending notification for target " + notification.targetId().id());
            }
        }
        message.put("value", "URL is coming");
        embeds.put("fields", List.of(message));
        embeds.put("author", getAuthor());
        embeds.put("footer", getFooter());
        return embeds;
    }

    private Map<String, String> getAuthor() {
        return Map.ofEntries(
                Map.entry("name", "Hlab Monitor"),
                Map.entry("url", "https://github.com/amaurydetremerie/HLabMonitor"),
                Map.entry("icon_url", "https://github.com/amaurydetremerie.png"));
    }

    private Map<String, String> getFooter() {
        return Map.ofEntries(
                Map.entry("text", "Thanks for using Hlab Monitor! (https://github.com/amaurydetremerie/HLabMonitor)"),
                Map.entry("icon_url", "https://github.githubassets.com/favicons/favicon-dark.svg"));
    }
}
