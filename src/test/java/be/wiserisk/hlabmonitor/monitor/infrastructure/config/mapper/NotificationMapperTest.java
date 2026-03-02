package be.wiserisk.hlabmonitor.monitor.infrastructure.config.mapper;

import be.wiserisk.hlabmonitor.monitor.domain.model.Notification;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetId;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.entity.NotificationEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringResult.*;
import static be.wiserisk.hlabmonitor.monitor.domain.enums.NotificationStatus.FAILED;
import static be.wiserisk.hlabmonitor.monitor.domain.enums.NotificationStatus.TO_SEND;
import static org.assertj.core.api.Assertions.assertThat;

class NotificationMapperTest {

    private final NotificationMapper notificationMapper = new NotificationMapperImpl();

    private static final Long NOTIFICATION_ID = 1L;
    private static final String TARGET_ID_STRING = "targetId";
    private static final TargetId TARGET_ID = new TargetId(TARGET_ID_STRING);
    private static final Instant NOW = Instant.now();

    @Test
    void toDomain() {
        Notification notification = new Notification(NOTIFICATION_ID, TARGET_ID, TO_SEND, NOW, NOW, FAILURE, SUCCESS);
        NotificationEntity notificationEntity = new NotificationEntity();
        notificationEntity.setId(NOTIFICATION_ID);
        notificationEntity.setTargetId(TARGET_ID_STRING);
        notificationEntity.setNotificationStatus(TO_SEND.name());
        notificationEntity.setFireAt(NOW);
        notificationEntity.setResolvedAt(NOW);
        notificationEntity.setOldResult(FAILURE.name());
        notificationEntity.setNewResult(SUCCESS.name());
        assertThat(notificationMapper.toDomain(notificationEntity)).isNotNull().isEqualTo(notification);
    }

    @Test
    void toDomain_resultAndStatusNull() {
        Notification notification = new Notification(NOTIFICATION_ID, TARGET_ID, FAILED, NOW, NOW, null, null);
        NotificationEntity notificationEntity = new NotificationEntity();
        notificationEntity.setId(NOTIFICATION_ID);
        notificationEntity.setTargetId(TARGET_ID_STRING);
        notificationEntity.setNotificationStatus(null);
        notificationEntity.setFireAt(NOW);
        notificationEntity.setResolvedAt(NOW);
        notificationEntity.setOldResult(null);
        notificationEntity.setNewResult(null);
        assertThat(notificationMapper.toDomain(notificationEntity)).isNotNull().isEqualTo(notification);
    }

    @Test
    void toDomain_notEnum() {
        Notification notification = new Notification(NOTIFICATION_ID, TARGET_ID, FAILED, NOW, NOW, UNKNOWN, UNKNOWN);
        NotificationEntity notificationEntity = new NotificationEntity();
        notificationEntity.setId(NOTIFICATION_ID);
        notificationEntity.setTargetId(TARGET_ID_STRING);
        notificationEntity.setNotificationStatus("NOT_VALID");
        notificationEntity.setFireAt(NOW);
        notificationEntity.setResolvedAt(NOW);
        notificationEntity.setOldResult("NOT_VALID");
        notificationEntity.setNewResult("NOT_VALID");
        assertThat(notificationMapper.toDomain(notificationEntity)).isNotNull().isEqualTo(notification);
    }

    @Test
    void toEntity() {
        Notification notification = new Notification(NOTIFICATION_ID, TARGET_ID, TO_SEND, NOW, NOW, FAILURE, SUCCESS);
        NotificationEntity notificationEntity = new NotificationEntity();
        notificationEntity.setId(NOTIFICATION_ID);
        notificationEntity.setTargetId(TARGET_ID_STRING);
        notificationEntity.setNotificationStatus(TO_SEND.name());
        notificationEntity.setFireAt(NOW);
        notificationEntity.setResolvedAt(NOW);
        notificationEntity.setOldResult(FAILURE.name());
        notificationEntity.setNewResult(SUCCESS.name());
        assertThat(notificationMapper.toEntity(notification)).isNotNull().isEqualTo(notificationEntity);
    }

    @Test
    void toEntity_resultAndStatusNull() {
        Notification notification = new Notification(NOTIFICATION_ID, TARGET_ID, null, NOW, NOW, null, null);
        NotificationEntity notificationEntity = new NotificationEntity();
        notificationEntity.setId(NOTIFICATION_ID);
        notificationEntity.setTargetId(TARGET_ID_STRING);
        notificationEntity.setNotificationStatus(FAILED.name());
        notificationEntity.setFireAt(NOW);
        notificationEntity.setResolvedAt(NOW);
        notificationEntity.setOldResult(null);
        notificationEntity.setNewResult(null);
        assertThat(notificationMapper.toEntity(notification)).isNotNull().isEqualTo(notificationEntity);
    }
}