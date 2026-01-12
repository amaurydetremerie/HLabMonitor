package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence;

import be.wiserisk.hlabmonitor.monitor.application.port.out.PersistencePort;
import be.wiserisk.hlabmonitor.monitor.domain.model.Target;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetId;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetResult;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.entity.ResultEntity;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.repository.ResultEntityRepository;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.repository.TargetEntityRepository;
import lombok.AllArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@AllArgsConstructor
public class JpaPersistenceAdapter implements PersistencePort {

    ResultEntityRepository resultEntityRepository;
    TargetEntityRepository targetEntityRepository;
    ObjectMapper objectMapper;

    @Override
    public void saveResult(TargetResult targetResult) {
        resultEntityRepository.save(objectMapper.convertValue(targetResult, ResultEntity.class));
    }

    @Override
    public Target getTarget(TargetId targetId) {
        return objectMapper.convertValue(targetEntityRepository.findByTargetId(targetId.id()), Target.class);
    }
}
