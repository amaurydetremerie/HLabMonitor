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
    @Column(name = "acceptable_status_code")
    private Integer acceptableStatusCode;

    @Column(name = "speedtest_type")
    private String speedtestType;
    @Column(name = "warning_threshold_mbps")
    private Double warningThresholdMbps;
    @Column(name = "failure_threshold_mbps")
    private Double failureThresholdMbps;
}
