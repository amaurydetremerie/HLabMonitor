package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "TARGET")
@Data
public class TargetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "target_id", unique = true, nullable = false)
    private String targetId;
    @Column(name = "target", nullable = false)
    private String target;
    @Column(name = "type", nullable = false)
    private String type;
}
