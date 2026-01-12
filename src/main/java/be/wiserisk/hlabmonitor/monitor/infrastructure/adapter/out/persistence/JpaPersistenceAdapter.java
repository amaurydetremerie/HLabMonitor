package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence;

import be.wiserisk.hlabmonitor.monitor.application.port.out.PersistencePort;
import be.wiserisk.hlabmonitor.monitor.domain.model.*;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.entity.ResultEntity;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.repository.ResultEntityRepository;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.repository.TargetEntityRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

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
                predicates.add(cb.in(root.get("targetId")).in(filter.targetIdList()));
            }
            if(filter.monitoringResultList() != null && !filter.monitoringResultList().isEmpty()) {
                predicates.add(cb.in(root.get("result")).in(filter.monitoringResultList()));
            }
            //Remonter sur la target
            if(filter.monitoringTypeList() != null && !filter.monitoringTypeList().isEmpty()) {
                predicates.add(cb.in(root.get("type")).in(filter.monitoringTypeList()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Override
    public List<TargetResult> getAllTargetResultsByTargetId(TargetId targetId) {
        return toTargetResultList(resultEntityRepository.findAllByTargetId(targetId.id()));
    }

    private List<TargetResult> toTargetResultList(List<ResultEntity> resultEntityList) {
        return resultEntityList
                .stream()
                .map(result -> objectMapper.convertValue(result, TargetResult.class))
                .toList();
    }
}
