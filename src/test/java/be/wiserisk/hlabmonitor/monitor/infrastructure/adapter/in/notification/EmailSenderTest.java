package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.in.notification;


import be.wiserisk.hlabmonitor.monitor.domain.enums.NotificationStatus;
import be.wiserisk.hlabmonitor.monitor.domain.exception.NotificationSenderException;
import be.wiserisk.hlabmonitor.monitor.domain.model.Notification;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetId;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.Monitoring;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.notification.NotificationEmail;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.notification.NotificationProperties;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.notification.NotificationType;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailSenderTest {

    @InjectMocks
    private EmailSender emailSender;

    @Mock
    private JavaMailSender mailSender;

    @Test
    void getNotificationType() {
        NotificationType notificationType = mock(NotificationType.class);
        Monitoring monitoring = new Monitoring(null, null, null, new NotificationProperties(
                null, new NotificationEmail(
                true, "null", null, notificationType, null), null, null, null));
        assertThat(emailSender.getNotificationType(monitoring)).isNotNull().isEqualTo(notificationType);
    }

    @Test
    void sendNotification_Error() {
        try(MockedConstruction<MimeMessageHelper> mimeMessageHelperMockedConstruction = Mockito.mockConstruction(MimeMessageHelper.class, (mock, context) -> {
            doThrow(MessagingException.class).when(mock).setFrom((String) null);
        })) {
            MimeMessage mimeMessage = mock(MimeMessage.class);
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            assertThatThrownBy(() -> emailSender.sendNotification(null)).isInstanceOf(NotificationSenderException.class);
        }
    }

    @Test
    void sendNotification_SEND() {
        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        try(MockedConstruction<MimeMessageHelper> mimeMessageHelperMockedConstruction = Mockito.mockConstruction(MimeMessageHelper.class, (mock, context) -> {
            doNothing().when(mock).setSubject(subjectCaptor.capture());
            doNothing().when(mock).setText(bodyCaptor.capture(), eq(true));
        })) {
            MimeMessage mimeMessage = mock(MimeMessage.class);
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            Notification notification = mock(Notification.class);
            when(notification.notificationStatus()).thenReturn(NotificationStatus.SEND);
            when(notification.targetId()).thenReturn(new TargetId("targetId"));

            assertDoesNotThrow(() -> emailSender.sendNotification(notification));

            verify(mailSender, times(1)).send(mimeMessage);
            assertThat(subjectCaptor.getValue()).isNotNull().contains("[FIRING]");
            assertThat(bodyCaptor.getValue()).isNotNull().contains("[FIRING]");
        }
    }

    @Test
    void sendNotification_TERMINATED() {
        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        try(MockedConstruction<MimeMessageHelper> mimeMessageHelperMockedConstruction = Mockito.mockConstruction(MimeMessageHelper.class, (mock, context) -> {
            doNothing().when(mock).setSubject(subjectCaptor.capture());
            doNothing().when(mock).setText(bodyCaptor.capture(), eq(true));
        })) {
            MimeMessage mimeMessage = mock(MimeMessage.class);
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            Notification notification = mock(Notification.class);
            when(notification.notificationStatus()).thenReturn(NotificationStatus.TERMINATED);
            when(notification.targetId()).thenReturn(new TargetId("targetId"));

            assertDoesNotThrow(() -> emailSender.sendNotification(notification));

            verify(mailSender, times(1)).send(mimeMessage);
            assertThat(subjectCaptor.getValue()).isNotNull().contains("[RESOLVED]");
            assertThat(bodyCaptor.getValue()).isNotNull().contains("[RESOLVED]");
        }
    }

    @Test
    void sendNotification_FAILED() {
        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        try(MockedConstruction<MimeMessageHelper> mimeMessageHelperMockedConstruction = Mockito.mockConstruction(MimeMessageHelper.class, (mock, context) -> {
            doNothing().when(mock).setSubject(subjectCaptor.capture());
            doNothing().when(mock).setText(bodyCaptor.capture(), eq(true));
        })) {
            MimeMessage mimeMessage = mock(MimeMessage.class);
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            Notification notification = mock(Notification.class);
            when(notification.notificationStatus()).thenReturn(NotificationStatus.FAILED);
            when(notification.targetId()).thenReturn(new TargetId("targetId"));

            assertDoesNotThrow(() -> emailSender.sendNotification(notification));

            verify(mailSender, times(1)).send(mimeMessage);
            assertThat(subjectCaptor.getValue()).isNotNull().contains("[FAILED]");
            assertThat(bodyCaptor.getValue()).isNotNull().contains("[FAILED]");
        }
    }
}