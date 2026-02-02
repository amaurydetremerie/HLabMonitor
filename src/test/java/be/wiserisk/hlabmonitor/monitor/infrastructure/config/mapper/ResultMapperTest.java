package be.wiserisk.hlabmonitor.monitor.infrastructure.config.mapper;

import be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringResult;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetId;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetResult;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.entity.ResultEntity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResultMapperTest {

    ResultMapper resultMapper = new ResultMapperImpl();

    @Test
    void mapTargetResultToResultEntity() {
        TargetResult targetResult = new TargetResult(new TargetId("targetId"), MonitoringResult.SUCCESS, "message");
        assertThat(resultMapper.toEntity(targetResult)).isNotNull().hasNoNullFieldsOrPropertiesExcept("id").extracting("targetId", "result", "message").isEqualTo(List.of("targetId", "SUCCESS", "message"));
    }

    @Test
    void mapTargetResultToResultEntityResultNull() {
        TargetResult targetResult = new TargetResult(new TargetId("targetId"), null, "message");
        assertThat(resultMapper.toEntity(targetResult)).isNotNull().hasNoNullFieldsOrPropertiesExcept("id", "result").extracting("targetId", "message").isEqualTo(List.of("targetId", "message"));
    }

    @Test
    void mapResultEntityToTargetResult() {
        ResultEntity resultEntity = new ResultEntity(1L, "targetId", "SUCCESS", "message");
        assertThat(resultMapper.toDomain(resultEntity)).isNotNull().extracting("id", "result", "message").isEqualTo(List.of(new TargetId("targetId"), MonitoringResult.SUCCESS, "message"));
    }

    @Test
    void mapResultEntityToTargetResultResultNull() {
        ResultEntity resultEntity = new ResultEntity(1L, "targetId", null, "message");
        assertThat(resultMapper.toDomain(resultEntity)).isNotNull().extracting("id", "result", "message").isEqualTo(List.of(new TargetId("targetId"), MonitoringResult.UNKNOWN, "message"));
    }

}