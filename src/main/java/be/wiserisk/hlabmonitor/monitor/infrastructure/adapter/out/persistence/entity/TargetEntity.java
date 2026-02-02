package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "TARGET")
@Data
@NoArgsConstructor
@AllArgsConstructor
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
