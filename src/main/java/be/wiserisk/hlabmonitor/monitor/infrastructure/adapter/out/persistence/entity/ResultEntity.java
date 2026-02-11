package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.entity;

import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.converter.InstantEpochMillisConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

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
    @Column(name = "message")
    private String message;
    @Column(name = "checked_at", nullable = false)
    @Convert(converter = InstantEpochMillisConverter.class)
    private Instant checkedAt;
}
