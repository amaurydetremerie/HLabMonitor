package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "TARGET")
@Data
public class TargetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String targetId;
    @Column(nullable = false)
    private String target;
    @Column(nullable = false)
    private String type;
}
