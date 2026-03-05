package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.entity;

import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.converter.InstantEpochMillisConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "NOTIFICATION")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "target_id", nullable = false)
    private String targetId;
    @Column(name = "notification_status", nullable = false)
    private String notificationStatus;
    @Column(name = "fire_at", nullable = false)
    @Convert(converter = InstantEpochMillisConverter.class)
    private Instant fireAt;
    @Column(name = "resolved_at")
    @Convert(converter = InstantEpochMillisConverter.class)
    private Instant resolvedAt;
    @Column(name = "old_result", nullable = false)
    private String oldResult;
    @Column(name = "new_result")
    private String newResult;
}
