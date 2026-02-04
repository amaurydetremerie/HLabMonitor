package be.wiserisk.hlabmonitor.monitor.infrastructure.config.mapper;

import be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringResult;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetId;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetResult;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.entity.ResultEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResultMapperTest {

    public static final Instant NOW = Instant.now();
    ResultMapper resultMapper = new ResultMapperImpl();

    @Test
    void mapTargetResultToResultEntity() {
        TargetResult targetResult = new TargetResult(new TargetId("targetId"), MonitoringResult.SUCCESS, "message", NOW);
        assertThat(resultMapper.toEntity(targetResult)).isNotNull().hasNoNullFieldsOrPropertiesExcept("id").extracting("targetId", "result", "message", "checkedAt").isEqualTo(List.of("targetId", "SUCCESS", "message", NOW));
    }

    @Test
    void mapTargetResultToResultEntityResultNull() {
        TargetResult targetResult = new TargetResult(new TargetId("targetId"), null, "message", NOW);
        assertThat(resultMapper.toEntity(targetResult)).isNotNull().hasNoNullFieldsOrPropertiesExcept("id", "result").extracting("targetId", "message", "checkedAt").isEqualTo(List.of("targetId", "message", NOW));
    }

    @Test
    void mapResultEntityToTargetResult() {
        ResultEntity resultEntity = new ResultEntity(1L, "targetId", "SUCCESS", "message", NOW);
        assertThat(resultMapper.toDomain(resultEntity)).isNotNull().extracting("id", "result", "message", "checkedAt").isEqualTo(List.of(new TargetId("targetId"), MonitoringResult.SUCCESS, "message", NOW));
    }

    @Test
    void mapResultEntityToTargetResultResultNull() {
        ResultEntity resultEntity = new ResultEntity(1L, "targetId", null, "message", NOW);
        assertThat(resultMapper.toDomain(resultEntity)).isNotNull().extracting("id", "result", "message", "checkedAt").isEqualTo(List.of(new TargetId("targetId"), MonitoringResult.UNKNOWN, "message", NOW));
    }

    @Test
    void mapResultEntityToTargetResultResultRandom() {
        ResultEntity resultEntity = new ResultEntity(1L, "targetId", "RANDOM", "message", NOW);
        assertThat(resultMapper.toDomain(resultEntity)).isNotNull().extracting("id", "result", "message", "checkedAt").isEqualTo(List.of(new TargetId("targetId"), MonitoringResult.UNKNOWN, "message", NOW));
    }

}