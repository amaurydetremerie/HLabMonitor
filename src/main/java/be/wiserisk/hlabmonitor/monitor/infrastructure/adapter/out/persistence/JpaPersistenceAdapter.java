package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence;

import be.wiserisk.hlabmonitor.monitor.application.port.out.PersistencePort;
import be.wiserisk.hlabmonitor.monitor.domain.model.*;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.entity.ResultEntity;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.entity.TargetEntity;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.repository.ResultEntityRepository;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.repository.TargetEntityRepository;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.mapper.ResultMapper;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.mapper.TargetMapper;
import jakarta.persistence.criteria.Predicate;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class JpaPersistenceAdapter implements PersistencePort {

    ResultEntityRepository resultEntityRepository;
    TargetEntityRepository targetEntityRepository;
    TargetMapper targetMapper;
    ResultMapper resultMapper;

    @Override
    public void saveResult(TargetResult targetResult) {
        resultEntityRepository.save(resultMapper.toEntity(targetResult));
    }

    @Override
    public Target getTarget(TargetId targetId) {
        return targetMapper.toDomain(targetEntityRepository.findByTargetId(targetId.id()));
    }

    @Override
    public List<TargetResult> getAllTargetResults() {
        return toTargetResultList(resultEntityRepository.findAll());
    }

    @Override
    public PageResponse<TargetResult> getAllResultsFilteredBy(CheckResultsFilter filter, PageRequest pageRequest) {
        Specification<ResultEntity> specification = getResultsEntitySpecification(filter);
        Page<ResultEntity> page = resultEntityRepository.findAll(specification, org.springframework.data.domain.PageRequest.of(pageRequest.page(), pageRequest.size(), Sort.unsorted()));

        return new PageResponse<>(toTargetResultList(page.getContent()), page.getNumber(), page.getSize(), page.getTotalElements(), page.hasNext());
    }

    private Specification<ResultEntity> getResultsEntitySpecification(CheckResultsFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            //Manquant dans l'entity
            if(filter.from() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("from"), filter.from()));
            }
            //Manquant dans l'entity
            if(filter.to() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("to"), filter.to()));
            }
            if(filter.targetIdList() != null && !filter.targetIdList().isEmpty()) {
                predicates.add(root.get("targetId").in(filter.targetIdList()));
            }
            if(filter.monitoringResultList() != null && !filter.monitoringResultList().isEmpty()) {
                predicates.add(root.get("result").in(filter.monitoringResultList()));
            }
            //Remonter sur la target
            if(filter.monitoringTypeList() != null && !filter.monitoringTypeList().isEmpty()) {
                predicates.add(root.get("type").in(filter.monitoringTypeList()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Override
    public List<TargetResult> getAllTargetResultsByTargetId(TargetId targetId) {
        return toTargetResultList(resultEntityRepository.findAllByTargetId(targetId.id()));
    }

    @Override
    public boolean isTargetIdExist(TargetId targetId) {
        return targetEntityRepository.existsByTargetId(targetId.id());
    }

    @Override
    public void updateTarget(Target target) {
        TargetEntity targetEntity = targetEntityRepository.findByTargetId(target.id().id());
        targetEntity.setTarget(target.target());
        targetEntity.setType(target.type().name());
        targetEntityRepository.save(targetEntity);
    }

    @Override
    public void createTarget(Target target) {
        targetEntityRepository.save(targetMapper.toEntity(target));
    }

    @Override
    public List<Target> getAllTargets(List<TargetId> targetIds) {
        return toTargetList(targetEntityRepository.findByTargetIdIn(targetIds.parallelStream().map(TargetId::id).toList()));
    }

    private List<TargetResult> toTargetResultList(List<ResultEntity> resultEntityList) {
        return resultEntityList
                .stream()
                .map(result -> resultMapper.toDomain(result))
                .toList();
    }

    private List<Target> toTargetList(List<TargetEntity> targetEntityList) {
        return targetEntityList
                .stream()
                .map(targetEntity -> targetMapper.toDomain(targetEntity))
                .toList();
    }
}
