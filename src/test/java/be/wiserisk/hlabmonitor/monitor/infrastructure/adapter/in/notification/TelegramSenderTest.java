package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.in.notification;


import be.wiserisk.hlabmonitor.monitor.domain.enums.NotificationStatus;
import be.wiserisk.hlabmonitor.monitor.domain.exception.NotificationSenderException;
import be.wiserisk.hlabmonitor.monitor.domain.model.Notification;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetId;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.Monitoring;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.notification.NotificationProperties;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.notification.NotificationTelegram;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.notification.NotificationType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;

import javax.net.ssl.HttpsURLConnection;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class TelegramSenderTest {

    @Test
    void getNotificationType() {
        TelegramSender telegramSender = new TelegramSender(null, null, null);
        NotificationType notificationType = mock(NotificationType.class);
        Monitoring monitoring = new Monitoring(null, null, new NotificationProperties(
                null, null, null, new NotificationTelegram(
                true, null, null, notificationType), null));
        assertThat(telegramSender.getNotificationType(monitoring)).isNotNull().isEqualTo(notificationType);
    }

    @Test
    void sendNotification_Error() throws JsonProcessingException {
        try(MockedConstruction<URI> uriMockedConstruction = mockConstruction(URI.class, (mock, ctx) -> {
            URL url = mock(URL.class);
            when(mock.toURL()).thenThrow(new MalformedURLException());
        })) {
            TelegramSender telegramSender = new TelegramSender(null, "", "");
            assertThatThrownBy(() -> telegramSender.sendNotification(null)).isInstanceOf(NotificationSenderException.class);
        }
    }

    @Test
    void sendNotification_SEND() throws JsonProcessingException {
        try(MockedConstruction<URI> uriMockedConstruction = mockConstruction(URI.class, (mock, ctx) -> {
            URL url = mock(URL.class);
            HttpsURLConnection httpsURLConnection = mock(HttpsURLConnection.class);
            OutputStream outputStream = mock(OutputStream.class);
            InputStream inputStream = mock(InputStream.class);
            when(mock.toURL()).thenReturn(url);
            when(url.openConnection()).thenReturn(httpsURLConnection);
            when(httpsURLConnection.getOutputStream()).thenReturn(outputStream);
            when(httpsURLConnection.getInputStream()).thenReturn(inputStream);
        })) {
            ObjectMapper objectMapper = mock(ObjectMapper.class);
            Notification notification = mock(Notification.class);
            when(notification.notificationStatus()).thenReturn(NotificationStatus.SEND);
            when(notification.targetId()).thenReturn(new TargetId("targetId"));
            TelegramSender telegramSender = new TelegramSender(objectMapper, "", "");
            ArgumentCaptor<Map<String, String>> captor = ArgumentCaptor.forClass(Map.class);
            when(objectMapper.writeValueAsBytes(captor.capture())).thenReturn(new byte[0]);
            assertDoesNotThrow(() -> telegramSender.sendNotification(notification));
            assertThat(captor.getValue()).hasSize(3)
                    .extracting("text").isNotNull()
                    .matches(s -> s.toString().contains("[FIRING]"));
        }
    }

    @Test
    void sendNotification_TERMINATED() throws JsonProcessingException {
        try(MockedConstruction<URI> uriMockedConstruction = mockConstruction(URI.class, (mock, ctx) -> {
            URL url = mock(URL.class);
            HttpsURLConnection httpsURLConnection = mock(HttpsURLConnection.class);
            OutputStream outputStream = mock(OutputStream.class);
            InputStream inputStream = mock(InputStream.class);
            when(mock.toURL()).thenReturn(url);
            when(url.openConnection()).thenReturn(httpsURLConnection);
            when(httpsURLConnection.getOutputStream()).thenReturn(outputStream);
            when(httpsURLConnection.getInputStream()).thenReturn(inputStream);
        })) {
            ObjectMapper objectMapper = mock(ObjectMapper.class);
            Notification notification = mock(Notification.class);
            when(notification.notificationStatus()).thenReturn(NotificationStatus.TERMINATED);
            when(notification.targetId()).thenReturn(new TargetId("targetId"));
            TelegramSender telegramSender = new TelegramSender(objectMapper, "", "");
            ArgumentCaptor<Map<String, String>> captor = ArgumentCaptor.forClass(Map.class);
            when(objectMapper.writeValueAsBytes(captor.capture())).thenReturn(new byte[0]);
            assertDoesNotThrow(() -> telegramSender.sendNotification(notification));
            assertThat(captor.getValue()).hasSize(3)
                    .extracting("text").isNotNull()
                    .matches(s -> s.toString().contains("[RESOLVED]"));
        }
    }

    @Test
    void sendNotification_FAILED() throws JsonProcessingException {
        try(MockedConstruction<URI> uriMockedConstruction = mockConstruction(URI.class, (mock, ctx) -> {
            URL url = mock(URL.class);
            HttpsURLConnection httpsURLConnection = mock(HttpsURLConnection.class);
            OutputStream outputStream = mock(OutputStream.class);
            InputStream inputStream = mock(InputStream.class);
            when(mock.toURL()).thenReturn(url);
            when(url.openConnection()).thenReturn(httpsURLConnection);
            when(httpsURLConnection.getOutputStream()).thenReturn(outputStream);
            when(httpsURLConnection.getInputStream()).thenReturn(inputStream);
        })) {
            ObjectMapper objectMapper = mock(ObjectMapper.class);
            Notification notification = mock(Notification.class);
            when(notification.notificationStatus()).thenReturn(NotificationStatus.FAILED);
            when(notification.targetId()).thenReturn(new TargetId("targetId"));
            TelegramSender telegramSender = new TelegramSender(objectMapper, "", "");
            ArgumentCaptor<Map<String, String>> captor = ArgumentCaptor.forClass(Map.class);
            when(objectMapper.writeValueAsBytes(captor.capture())).thenReturn(new byte[0]);
            assertDoesNotThrow(() -> telegramSender.sendNotification(notification));
            assertThat(captor.getValue()).hasSize(3)
                    .extracting("text").isNotNull()
                    .matches(s -> s.toString().contains("[FAILED]"));
        }
    }

}