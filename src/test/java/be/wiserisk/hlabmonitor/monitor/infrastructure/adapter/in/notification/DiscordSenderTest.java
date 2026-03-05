package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.in.notification;


import be.wiserisk.hlabmonitor.monitor.domain.enums.NotificationStatus;
import be.wiserisk.hlabmonitor.monitor.domain.exception.NotificationSenderException;
import be.wiserisk.hlabmonitor.monitor.domain.model.Notification;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetId;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.Monitoring;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.notification.NotificationDiscord;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.notification.NotificationProperties;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.notification.NotificationType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscordSenderTest {

    @InjectMocks
    DiscordSender discordSender;

    @Mock
    ObjectMapper objectMapper;

    @Test
    void getNotificationType() {
        NotificationType notificationType = mock(NotificationType.class);
        Monitoring monitoring = new Monitoring(null, null, new NotificationProperties(
                null, null, new NotificationDiscord(
                true, null, notificationType), null, null));
        assertThat(discordSender.getNotificationType(monitoring)).isNotNull().isEqualTo(notificationType);
    }

    @Test
    void sendNotification_URISyntaxException() {
        DiscordSender sender = new DiscordSender(null, "http:");
        assertThatThrownBy(() -> sender.sendNotification(null)).isInstanceOf(NotificationSenderException.class);
    }

    @Test
    void sendNotification_SecurityException() {
        try(MockedConstruction<URI> uriMockedConstruction = Mockito.mockConstruction(URI.class, (mock, context) -> {
            when(mock.toURL()).thenThrow(new SecurityException());
        })) {
            assertThatThrownBy(() -> discordSender.sendNotification(null)).isInstanceOf(NotificationSenderException.class);
        }
    }

    @Test
    void sendNotification_IOException() {
        try(MockedConstruction<URI> uriMockedConstruction = Mockito.mockConstruction(URI.class, (mock, context) -> {
            URL url = mock(URL.class);
            when(mock.toURL()).thenReturn(url);
            when(url.openConnection()).thenThrow(new IOException());
        })) {
            assertThatThrownBy(() -> discordSender.sendNotification(null)).isInstanceOf(NotificationSenderException.class);
        }
    }

    @Test
    void sendNotification_SEND() throws IOException {
        URL url = mock(URL.class);
        try(MockedConstruction<URI> uriMockedConstruction = Mockito.mockConstruction(URI.class,(mock, context)-> {
            when(mock.toURL()).thenReturn(url);
        })){
            HttpsURLConnection httpsURLConnection = mock(HttpsURLConnection.class);
            OutputStream outputStream = mock(OutputStream.class);
            when(url.openConnection()).thenReturn(httpsURLConnection);
            when(httpsURLConnection.getOutputStream()).thenReturn(outputStream);

            Notification notification = mock(Notification.class);
            when(notification.notificationStatus()).thenReturn(NotificationStatus.SEND);
            when(notification.targetId()).thenReturn(new TargetId("targetId"));

            ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);

            when(objectMapper.writeValueAsString(captor.capture())).thenReturn("");

            assertDoesNotThrow(() -> discordSender.sendNotification(notification));

            assertThat(captor.getValue()).hasSize(1)
                    .extracting("embeds").isNotNull()
                    .extracting(s -> ((List) s).getFirst())
                    .extracting("title").isNotNull()
                    .matches(s -> s.toString().contains("[FIRING]"));
        }
    }

    @Test
    void sendNotification_TERMINATED() throws IOException {
        URL url = mock(URL.class);
        try(MockedConstruction<URI> uriMockedConstruction = Mockito.mockConstruction(URI.class,(mock, context)-> {
            when(mock.toURL()).thenReturn(url);
        })){
            HttpsURLConnection httpsURLConnection = mock(HttpsURLConnection.class);
            OutputStream outputStream = mock(OutputStream.class);
            when(url.openConnection()).thenReturn(httpsURLConnection);
            when(httpsURLConnection.getOutputStream()).thenReturn(outputStream);

            Notification notification = mock(Notification.class);
            when(notification.notificationStatus()).thenReturn(NotificationStatus.TERMINATED);
            when(notification.targetId()).thenReturn(new TargetId("targetId"));

            ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);

            when(objectMapper.writeValueAsString(captor.capture())).thenReturn("");

            assertDoesNotThrow(() -> discordSender.sendNotification(notification));

            assertThat(captor.getValue()).hasSize(1)
                    .extracting("embeds").isNotNull()
                    .extracting(s -> ((List) s).getFirst())
                    .extracting("title").isNotNull()
                    .matches(s -> s.toString().contains("[RESOLVED]"));
        }
    }

    @Test
    void sendNotification_FAILED() throws IOException {
        URL url = mock(URL.class);
        try(MockedConstruction<URI> uriMockedConstruction = Mockito.mockConstruction(URI.class,(mock, context)-> {
            when(mock.toURL()).thenReturn(url);
        })){
            HttpsURLConnection httpsURLConnection = mock(HttpsURLConnection.class);
            OutputStream outputStream = mock(OutputStream.class);
            when(url.openConnection()).thenReturn(httpsURLConnection);
            when(httpsURLConnection.getOutputStream()).thenReturn(outputStream);

            Notification notification = mock(Notification.class);
            when(notification.notificationStatus()).thenReturn(NotificationStatus.FAILED);
            when(notification.targetId()).thenReturn(new TargetId("targetId"));

            ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);

            when(objectMapper.writeValueAsString(captor.capture())).thenReturn("");

            assertDoesNotThrow(() -> discordSender.sendNotification(notification));

            assertThat(captor.getValue()).hasSize(1)
                    .extracting("embeds").isNotNull()
                    .extracting(s -> ((List) s).getFirst())
                    .extracting("title").isNotNull()
                    .matches(s -> s.toString().contains("[FAILED]"));
        }
    }

}