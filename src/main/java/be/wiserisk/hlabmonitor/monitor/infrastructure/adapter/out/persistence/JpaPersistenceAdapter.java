package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence;

import be.wiserisk.hlabmonitor.monitor.application.port.out.PersistencePort;
import be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringResult;
import be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringType;
import be.wiserisk.hlabmonitor.monitor.domain.exception.ResultNotFoundException;
import be.wiserisk.hlabmonitor.monitor.domain.model.*;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.entity.*;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.repository.NotificationEntityRepository;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.repository.ResultEntityRepository;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.repository.TargetEntityRepository;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.mapper.NotificationMapper;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.mapper.ResultMapper;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.mapper.TargetMapper;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static be.wiserisk.hlabmonitor.monitor.domain.enums.NotificationStatus.SEND;

@AllArgsConstructor
public class JpaPersistenceAdapter implements PersistencePort {

    NotificationEntityRepository notificationEntityRepository;
    ResultEntityRepository resultEntityRepository;
    TargetEntityRepository targetEntityRepository;
    NotificationMapper notificationMapper;
    TargetMapper targetMapper;
    ResultMapper resultMapper;

    @Override
    public TargetResult saveResult(TargetResult targetResult) {
        return resultMapper.toDomain(resultEntityRepository.save(resultMapper.toEntity(targetResult)));
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

            if (filter.from() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get(ResultEntity_.checkedAt), filter.from()));
            }
            if (filter.to() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get(ResultEntity_.checkedAt), filter.to()));
            }
            if(filter.targetIdList() != null && !filter.targetIdList().isEmpty()) {
                predicates.add(root.get(ResultEntity_.targetId).in(filter.targetIdList().stream().map(TargetId::id).toList()));
            }
            if(filter.monitoringResultList() != null && !filter.monitoringResultList().isEmpty()) {
                predicates.add(root.get(ResultEntity_.result).in(filter.monitoringResultList().stream().map(Enum::name).toList()));
            }
            if (filter.monitoringTypeList() != null && !filter.monitoringTypeList().isEmpty()) {
                Subquery<Integer> subquery = query.subquery(Integer.class);
                Root<TargetEntity> targetEntityRoot = subquery.from(TargetEntity.class);
                subquery.select(cb.literal(1))
                    .where(
                        cb.equal(targetEntityRoot.get(TargetEntity_.targetId), root.get(ResultEntity_.targetId)),
                        targetEntityRoot.get(TargetEntity_.type).in(filter.monitoringTypeList().stream().map(Enum::name).toList())
                    );
                predicates.add(cb.exists(subquery));
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
        targetEntity.setAcceptableStatusCode(target.acceptableStatusCode());
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

    @Override
    public List<TargetId> getAllTargetIds() {
        return toTargetIdList(targetEntityRepository.findAll());
    }

    @Override
    public List<TargetId> getAllTargetIdsByMonitoringType(MonitoringType monitoringType) {
        return toTargetIdList(targetEntityRepository.findAllByType(monitoringType.name()));
    }

    @Override
    public Long countTarget() {
        return targetEntityRepository.count();
    }

    @Override
    public Long countTarget(MonitoringType monitoringType) {
        return targetEntityRepository.countByType(monitoringType.name());
    }

    @Override
    public Long countLast24hResults() {
        return resultEntityRepository.countByCheckedAtGreaterThanEqual(Instant.now().minus(Duration.ofHours(24)));
    }

    @Override
    public Long countLast24hResults(MonitoringResult monitoringResult) {
        return resultEntityRepository.countByResultAndCheckedAtGreaterThanEqual(monitoringResult.name(), Instant.now().minus(Duration.ofHours(24)));
    }

    @Override
    public Optional<Notification> getSendNotification(TargetId targetId) {
        return Optional.ofNullable(notificationMapper.toDomain(notificationEntityRepository.findLastByTargetIdAndNotificationStatus(targetId.id(), SEND.name())));
    }

    @Override
    public TargetResult getLastTargetResult(TargetId targetId) {
        return resultMapper.toDomain(resultEntityRepository.findLastByTargetId(targetId.id()));
    }

    @Override
    public boolean isResultChanged(TargetId targetId) {
        NotificationEntity notificationEntity = notificationEntityRepository.findTopByTargetId(targetId.id());
        ResultEntity resultEntity = resultEntityRepository.findTopByTargetId(targetId.id());
        if(resultEntity == null) {
            throw new ResultNotFoundException(targetId);
        }
        MonitoringResult monitoringResult = MonitoringResult.valueOf(resultEntity.getResult());
        if(notificationEntity == null) {
            return !monitoringResult.getFamily().equals(MonitoringResult.Family.SUCCESS);
        }
        if(!notificationEntity.getNotificationStatus().equals(SEND.name())) {
            return !monitoringResult.getFamily().equals(MonitoringResult.Family.SUCCESS);
        }
        MonitoringResult notificationResult = MonitoringResult.valueOf(notificationEntity.getOldResult());
        return monitoringResult.getFamily().equals(notificationResult.getFamily());
    }

    @Override
    public Notification saveNotification(Notification notification) {
        return notificationMapper.toDomain(notificationEntityRepository.save(notificationMapper.toEntity(notification)));
    }

    @Override
    public List<Notification> getActiveNotifications() {
        return toNotificationList(notificationEntityRepository.findAllByNotificationStatus(SEND.name()));
    }

    @Override
    public Integer countActiveNotifications() {
        return notificationEntityRepository.countByNotificationStatus(SEND.name());
    }

    private List<Notification> toNotificationList(List<NotificationEntity> notificationEntityList) {
        return notificationEntityList.stream().map(notificationMapper::toDomain).toList();
    }

    private List<TargetId> toTargetIdList(List<TargetEntity> targetEntityList) {
        return targetEntityList.stream().map(t -> new TargetId(t.getTargetId())).toList();
    }

    private List<TargetResult> toTargetResultList(List<ResultEntity> resultEntityList) {
        return resultEntityList
                .stream()
                .map(resultMapper::toDomain)
                .toList();
    }

    private List<Target> toTargetList(List<TargetEntity> targetEntityList) {
        return targetEntityList
                .stream()
                .map(targetMapper::toDomain)
                .toList();
    }
}
