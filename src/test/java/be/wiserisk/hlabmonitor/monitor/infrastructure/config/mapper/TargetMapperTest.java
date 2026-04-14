package be.wiserisk.hlabmonitor.monitor.infrastructure.config.mapper;

import be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringType;
import be.wiserisk.hlabmonitor.monitor.domain.model.Target;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetId;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.entity.TargetEntity;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TargetMapperTest {

    final TargetMapper targetMapper = new TargetMapperImpl();

    @Test
    void mapTargetToTargetEntity() {
        Target target = new Target(new TargetId("targetId"), MonitoringType.HTTP, "target", Duration.ofMinutes(1));
        assertThat(targetMapper.toEntity(target)).isNotNull()
                .hasNoNullFieldsOrPropertiesExcept("id", "speedtestType", "warningThresholdMbps", "failureThresholdMbps")
                .extracting("targetId", "type", "target")
                .isEqualTo(List.of("targetId", "HTTP", "target"));
    }

    @Test
    void mapTargetToTargetEntityTypeNull() {
        Target target = new Target(new TargetId("targetId"), null, "target", Duration.ofMinutes(1));
        assertThat(targetMapper.toEntity(target)).isNotNull()
                .hasNoNullFieldsOrPropertiesExcept("id", "type", "speedtestType", "warningThresholdMbps", "failureThresholdMbps")
                .extracting("targetId", "target")
                .isEqualTo(List.of("targetId", "target"));
    }

    @Test
    void mapTargetEntityToTarget() {
        TargetEntity targetEntity = new TargetEntity(1L, "targetId", "target", "HTTP", 302, null, null, null);
        assertThat(targetMapper.toDomain(targetEntity)).isNotNull()
                .extracting("id", "type", "target", "acceptableStatusCode")
                .isEqualTo(List.of(new TargetId("targetId"), MonitoringType.HTTP, "target", 302));
    }

    @Test
    void mapTargetEntityToTargetTypeNull() {
        TargetEntity targetEntity = new TargetEntity(1L, "targetId", "target", null, null, null, null, null);
        assertThat(targetMapper.toDomain(targetEntity)).isNotNull()
                .extracting("id", "type", "target")
                .isEqualTo(List.of(new TargetId("targetId"), MonitoringType.UNKNOWN, "target"));
    }

    @Test
    void mapTargetEntityToTargetTypeRandom() {
        TargetEntity targetEntity = new TargetEntity(1L, "targetId", "target", "RANDOM", null, null, null, null);
        assertThat(targetMapper.toDomain(targetEntity)).isNotNull()
                .extracting("id", "type", "target")
                .isEqualTo(List.of(new TargetId("targetId"), MonitoringType.UNKNOWN, "target"));
    }

}
