package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.repository;

import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.entity.NotificationEntity;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.entity.TargetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface NotificationEntityRepository extends JpaRepository<NotificationEntity, Long> {
    NotificationEntity findLastByTargetIdAndNotificationStatus(String targetId, String notificationStatus);

    NotificationEntity findTopByTargetId(String id);

    List<NotificationEntity> findAllByNotificationStatus(String notificationStatus);

    Integer countByNotificationStatus(String name);
}
