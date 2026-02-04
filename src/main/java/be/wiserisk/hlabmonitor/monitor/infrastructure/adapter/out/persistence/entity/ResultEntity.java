package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "RESULT")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResultEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "target_id", nullable = false)
    private String targetId;
    @Column(name = "result", nullable = false)
    private String result;
    @Column(name = "message", nullable = true)
    private String message;
    @Column(name = "checked_at", nullable = false)
    private Instant checkedAt;
}
