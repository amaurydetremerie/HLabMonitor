package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.in.notification;

import be.wiserisk.hlabmonitor.monitor.application.port.out.NotificationPort;
import be.wiserisk.hlabmonitor.monitor.domain.model.Notification;
import lombok.AllArgsConstructor;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

@AllArgsConstructor
public class NotificationAdapter implements NotificationPort {

    private final MessageChannel notificationChannel;

    @Override
    public void sendNotification(Notification notification) {
        Message<Notification> message = MessageBuilder
                .withPayload(notification)
                .build();
        notificationChannel.send(message);
    }
}
