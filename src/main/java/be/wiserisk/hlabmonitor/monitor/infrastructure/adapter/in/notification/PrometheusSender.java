package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.in.notification;

import be.wiserisk.hlabmonitor.monitor.domain.model.Notification;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.Monitoring;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.notification.NotificationType;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import lombok.AllArgsConstructor;

import java.time.Duration;
import java.time.Instant;

import static be.wiserisk.hlabmonitor.monitor.domain.enums.NotificationStatus.SEND;
import static be.wiserisk.hlabmonitor.monitor.domain.enums.NotificationStatus.TERMINATED;

@AllArgsConstructor
public class PrometheusSender implements NotificationSender {
    private final MeterRegistry meterRegistry;

    @Override
    public void sendNotification(Notification notification) {
        Tag statusTag = Tag.of("status", notification.notificationStatus().name());
        Tag targetIdTag = Tag.of("target_id", notification.targetId().id());
        Tag notificationIdTag = Tag.of("id", notification.notificationId().toString());

        Counter.builder("notifications_total")
                .tags(Tags.of(statusTag, targetIdTag))
                .description("Total notifications by status")
                .register(meterRegistry).increment();

        meterRegistry.gauge("notifications_active",
                Tags.of(notificationIdTag, statusTag, targetIdTag),
                SEND.equals(notification.notificationStatus()) ? 1.0 : 0.0);

        if (SEND.equals(notification.notificationStatus()) && notification.fireAt() != null) {
            meterRegistry.gauge("notifications_age_seconds",
                    Tags.of(notificationIdTag, targetIdTag),
                    Duration.between(notification.fireAt(), Instant.now()).getSeconds());
        }

        if (TERMINATED.equals(notification.notificationStatus()) && notification.fireAt() != null && notification.resolvedAt() != null) {
            meterRegistry.gauge("notifications_resolution_seconds",
                    Tags.of(notificationIdTag, targetIdTag),
                    Duration.between(notification.fireAt(), notification.resolvedAt()).getSeconds());
        }
    }

    @Override
    public NotificationType getNotificationType(Monitoring monitoring) {
        return new NotificationType();
    }
}
