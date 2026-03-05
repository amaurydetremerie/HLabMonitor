package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.in.notification;

import be.wiserisk.hlabmonitor.monitor.domain.model.Notification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationAdapterTest {

    @InjectMocks
    private NotificationAdapter notificationAdapter;

    @Mock
    private MessageChannel notificationChannel;

    @Test
    void sendNotification() {
        Notification notification = mock(Notification.class);
        Message<Notification> message = mock(Message.class);
        MessageBuilder<Notification> messageBuilder = mock(MessageBuilder.class);

        when(messageBuilder.build()).thenReturn(message);

        try (MockedStatic<MessageBuilder> messageBuilderMockedStatic = Mockito.mockStatic(MessageBuilder.class)) {
            messageBuilderMockedStatic.when(() -> MessageBuilder.withPayload(notification)).thenReturn(messageBuilder);

            assertDoesNotThrow(() -> notificationAdapter.sendNotification(notification));
        }
        verify(notificationChannel, times(1)).send(message);
    }

}