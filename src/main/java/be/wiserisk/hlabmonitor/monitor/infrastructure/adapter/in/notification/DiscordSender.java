package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.in.notification;

import be.wiserisk.hlabmonitor.monitor.domain.exception.NotificationSenderException;
import be.wiserisk.hlabmonitor.monitor.domain.model.Notification;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.Monitoring;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.notification.NotificationType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class DiscordSender implements NotificationSender {

    public static final String AUTHOR = "author";
    public static final String TITLE = "title";
    public static final String COLOR = "color";
    public static final String NAME = "name";
    public static final String FIELDS = "fields";
    public static final String FOOTER = "footer";
    public static final String VALUE = "value";
    public static final String EMBEDS = "embeds";
    public static final String URL = "url";
    public static final String ICON_URL = "icon_url";
    public static final String TEXT = "text";
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
        } catch (URISyntaxException | SecurityException | IOException e) {
            throw new NotificationSenderException(e);
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
                        Map.entry(EMBEDS, List.of(embeds))
                ));
    }

    private Map<String, Object> getEmbeds(Notification notification) {
        Map<String, Object> embeds = new HashMap<>();
        Map<String, String> message = new HashMap<>();
        switch (notification.notificationStatus()){
            case SEND -> {
                embeds.put(AUTHOR, getAuthor());
                embeds.put(TITLE, "[FIRING]");
                embeds.put(COLOR, 11550002);
                message.put(NAME, "Notification for target " + notification.targetId().id() + " has been fired");

            }
            case TERMINATED ->  {
                embeds.put(AUTHOR, getAuthor());
                embeds.put(TITLE, "[RESOLVED]");
                embeds.put(COLOR, 6729778);
                message.put(NAME, "Notification for target " + notification.targetId().id() + " has been resolved");
            }
            default -> {
                embeds.put(TITLE, "[FAILED]");
                embeds.put(COLOR, 3319216);
                message.put(NAME, "An error occurred while sending notification for target " + notification.targetId().id());
            }
        }
        message.put(VALUE, "URL is coming");
        embeds.put(FIELDS, List.of(message));
        embeds.put(AUTHOR, getAuthor());
        embeds.put(FOOTER, getFooter());
        return embeds;
    }

    private Map<String, String> getAuthor() {
        return Map.ofEntries(
                Map.entry(NAME, "Hlab Monitor"),
                Map.entry(URL, "https://github.com/amaurydetremerie/HLabMonitor"),
                Map.entry(ICON_URL, "https://github.com/amaurydetremerie.png"));
    }

    private Map<String, String> getFooter() {
        return Map.ofEntries(
                Map.entry(TEXT, "Thanks for using Hlab Monitor! (https://github.com/amaurydetremerie/HLabMonitor)"),
                Map.entry(ICON_URL, "https://github.githubassets.com/favicons/favicon-dark.svg"));
    }
}
