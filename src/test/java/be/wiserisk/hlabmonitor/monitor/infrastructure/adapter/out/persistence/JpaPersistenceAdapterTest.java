package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence;

import be.wiserisk.hlabmonitor.monitor.domain.model.*;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.entity.ResultEntity;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.entity.ResultEntity_;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.entity.TargetEntity;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.entity.TargetEntity_;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.repository.ResultEntityRepository;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.repository.TargetEntityRepository;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.mapper.ResultMapper;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.mapper.TargetMapper;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringResult.SUCCESS;
import static be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringType.HTTP;
import static be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringType.PING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JpaPersistenceAdapterTest {
    public static final String MESSAGE = "message";
    public static final String TARGET_ID_STRING = "targetId";
    public static final TargetId TARGET_ID = new TargetId(TARGET_ID_STRING);
    public static final TargetResult TARGET_RESULT = new TargetResult(TARGET_ID, SUCCESS, MESSAGE);
    public static final List<TargetResult> TARGET_RESULTS = List.of(TARGET_RESULT);
    public static final ResultEntity RESULT_ENTITY = new ResultEntity();
    public static final Target TARGET = new Target(TARGET_ID, PING, "target", Duration.ofMinutes(1));

    @InjectMocks
    private JpaPersistenceAdapter jpaPersistenceAdapter;

    @Mock
    private ResultEntityRepository resultEntityRepository;
    @Mock
    private TargetEntityRepository targetEntityRepository;
    @Mock
    private TargetMapper targetMapper;
    @Mock
    private ResultMapper resultMapper;

    @Test
    void getAllTargetIds() {
        TargetEntity targetEntity = new TargetEntity();
        targetEntity.setTargetId(TARGET_ID_STRING);
        when(targetEntityRepository.findAll()).thenReturn(List.of(targetEntity));

        assertThat(jpaPersistenceAdapter.getAllTargetIds()).isNotNull().containsExactly(TARGET_ID);
    }

    @Test
    void getAllTargetIdsByMonitoringType() {
        TargetEntity targetEntity = new TargetEntity();
        targetEntity.setTargetId(TARGET_ID_STRING);
        when(targetEntityRepository.findAllByType(PING.name())).thenReturn(List.of(targetEntity));

        assertThat(jpaPersistenceAdapter.getAllTargetIdsByMonitoringType(PING)).isNotNull().containsExactly(TARGET_ID);
    }

    @Test
    void saveResult() {
        when(resultMapper.toEntity(TARGET_RESULT)).thenReturn(RESULT_ENTITY);

        assertDoesNotThrow(() -> jpaPersistenceAdapter.saveResult(TARGET_RESULT));
        verify(resultEntityRepository, times(1)).save(RESULT_ENTITY);
    }

    @Test
    void getTarget() {
        Target target = new Target(TARGET_ID, HTTP, MESSAGE, Duration.ofMinutes(1));
        TargetEntity targetEntity = new TargetEntity();

        when(targetEntityRepository.findByTargetId(TARGET_ID_STRING)).thenReturn(targetEntity);
        when(targetMapper.toDomain(targetEntity)).thenReturn(target);

        assertThat(jpaPersistenceAdapter.getTarget(TARGET_ID)).isEqualTo(target);
    }

    @Test
    void getAllTargetResults() {

        when(resultMapper.toDomain(RESULT_ENTITY)).thenReturn(TARGET_RESULT);
        when(resultEntityRepository.findAll()).thenReturn(List.of(RESULT_ENTITY));

        assertThat(jpaPersistenceAdapter.getAllTargetResults()).isNotNull().isNotEmpty().isEqualTo(TARGET_RESULTS);
    }

    @Test
    void getAllTargetResultsByTargetId() {
        when(resultMapper.toDomain(RESULT_ENTITY)).thenReturn(TARGET_RESULT);
        when(resultEntityRepository.findAllByTargetId(TARGET_ID_STRING)).thenReturn(List.of(RESULT_ENTITY));

        assertThat(jpaPersistenceAdapter.getAllTargetResultsByTargetId(TARGET_ID)).isNotNull().isNotEmpty().isEqualTo(TARGET_RESULTS);
    }

    @Test
    void isTargetIdExist() {
        when(targetEntityRepository.existsByTargetId(TARGET_ID_STRING)).thenReturn(true);

        assertThat(jpaPersistenceAdapter.isTargetIdExist(TARGET_ID)).isTrue();
    }

    @Test
    void notIsTargetIdExist() {
        when(targetEntityRepository.existsByTargetId(TARGET_ID_STRING)).thenReturn(false);

        assertThat(jpaPersistenceAdapter.isTargetIdExist(TARGET_ID)).isFalse();
    }

    @Test
    void getAllResultsFilteredBy() {
        PageImpl<ResultEntity> page = new PageImpl<>(List.of(RESULT_ENTITY), Pageable.ofSize(10), 1);
        PageResponse<TargetResult> pageResponse = new PageResponse<>(TARGET_RESULTS, 0, 10, 1, false);
        CheckResultsFilter filter = new CheckResultsFilter(Instant.MIN, Instant.MAX, List.of(TARGET_ID), List.of(SUCCESS), List.of(HTTP));
        PageRequest pageRequest = new PageRequest(0, 10);

        when(resultMapper.toDomain(RESULT_ENTITY)).thenReturn(TARGET_RESULT);
        when(resultEntityRepository.findAll(argThat(new ResultEntitySpecificationMatcher(filter)), any(org.springframework.data.domain.PageRequest.class))).thenReturn(page);

        assertThat(jpaPersistenceAdapter.getAllResultsFilteredBy(filter, pageRequest)).isNotNull().isEqualTo(pageResponse);
    }

    @Test
    void getAllResultsFilteredByFiltersEmpty() {
        PageImpl<ResultEntity> page = new PageImpl<>(List.of(RESULT_ENTITY), Pageable.ofSize(10), 1);
        PageResponse<TargetResult> pageResponse = new PageResponse<>(TARGET_RESULTS, 0, 10, 1, false);
        CheckResultsFilter filter = new CheckResultsFilter(null, null, List.of(), List.of(), List.of());
        PageRequest pageRequest = new PageRequest(0, 10);

        when(resultMapper.toDomain(RESULT_ENTITY)).thenReturn(TARGET_RESULT);
        when(resultEntityRepository.findAll(argThat(new ResultEntitySpecificationMatcher(filter)), any(org.springframework.data.domain.PageRequest.class))).thenReturn(page);

        assertThat(jpaPersistenceAdapter.getAllResultsFilteredBy(filter, pageRequest)).isNotNull().isEqualTo(pageResponse);
    }

    @Test
    void getAllResultsFilteredByNoFilters() {
        PageImpl<ResultEntity> page = new PageImpl<>(List.of(RESULT_ENTITY), Pageable.ofSize(10), 1);
        PageResponse<TargetResult> pageResponse = new PageResponse<>(TARGET_RESULTS, 0, 10, 1, false);
        CheckResultsFilter filter = new CheckResultsFilter(null, null, null, null, null);
        PageRequest pageRequest = new PageRequest(0, 10);

        when(resultMapper.toDomain(RESULT_ENTITY)).thenReturn(TARGET_RESULT);
        when(resultEntityRepository.findAll(argThat(new ResultEntitySpecificationMatcher(filter)), any(org.springframework.data.domain.PageRequest.class))).thenReturn(page);

        assertThat(jpaPersistenceAdapter.getAllResultsFilteredBy(filter, pageRequest)).isNotNull().isEqualTo(pageResponse);
    }

    @Test
    void updateTarget() {
        TargetEntity targetEntity = mock(TargetEntity.class);
        when(targetEntityRepository.findByTargetId(TARGET_ID_STRING)).thenReturn(targetEntity);
        when(targetEntityRepository.save(targetEntity)).thenReturn(targetEntity);
        assertDoesNotThrow(() -> jpaPersistenceAdapter.updateTarget(TARGET));
        verify(targetEntity, times(1)).setTarget(TARGET.target());
        verify(targetEntity, times(1)).setType(TARGET.type().name());
    }

    @Test
    void createTarget() {
        TargetEntity targetEntity = new TargetEntity();
        when(targetMapper.toEntity(TARGET)).thenReturn(targetEntity);
        assertDoesNotThrow(() -> jpaPersistenceAdapter.createTarget(TARGET));
        verify(targetEntityRepository, times(1)).save(targetEntity);
    }

    @Test
    void getAllTargets() {
        TargetEntity targetEntity = new TargetEntity();
        when(targetEntityRepository.findByTargetIdIn(List.of(TARGET_ID_STRING))).thenReturn(List.of(targetEntity));
        when(targetMapper.toDomain(targetEntity)).thenReturn(TARGET);
        assertThat(jpaPersistenceAdapter.getAllTargets(List.of(TARGET_ID))).isNotNull().isEqualTo(List.of(TARGET));
    }

    private static class ResultEntitySpecificationMatcher implements ArgumentMatcher<Specification<ResultEntity>> {
        private final CheckResultsFilter filter;

        public ResultEntitySpecificationMatcher(CheckResultsFilter filter) {
            this.filter = filter;
        }

        @Override
        public boolean matches(Specification<ResultEntity> resultEntitySpecification) {
            List<Predicate> predicates = new ArrayList<>();

            Root<ResultEntity> root = mock(Root.class);
            CriteriaQuery<ResultEntity> criteriaQuery = mock(CriteriaQuery.class);
            CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);

            if(filter.from() != null || filter.to() != null) {
                Path pathCheckedAt = mock(Path.class);
                when(root.get(ResultEntity_.checkedAt)).thenReturn(pathCheckedAt);
                if(filter.from() != null) {
                    Predicate predicateFrom = mock(Predicate.class);
                    when(criteriaBuilder.greaterThanOrEqualTo(pathCheckedAt, filter.from())).thenReturn(predicateFrom);
                    predicates.add(predicateFrom);
                }

                if(filter.to() != null) {
                    Predicate predicateTo = mock(Predicate.class);
                    when(criteriaBuilder.lessThanOrEqualTo(pathCheckedAt, filter.to())).thenReturn(predicateTo);
                    predicates.add(predicateTo);
                }
            }

            if(filter.targetIdList() != null && !filter.targetIdList().isEmpty()) {
                Path pathTargetIdList = mock(Path.class);
                Predicate predicateTargetIdList = mock(Predicate.class);
                when(root.get(ResultEntity_.targetId)).thenReturn(pathTargetIdList);
                when(pathTargetIdList.in(filter.targetIdList())).thenReturn(predicateTargetIdList);
                predicates.add(predicateTargetIdList);
            }

            if (filter.monitoringTypeList() != null && !filter.monitoringTypeList().isEmpty()) {
                Subquery<Integer> subquery = mock(Subquery.class);
                Root<TargetEntity> targetEntityRoot = mock(Root.class);
                Expression<Integer> expression = mock(Expression.class);
                Path pathTargetTargetId = mock(Path.class);
                Path pathResultTargetId = mock(Path.class);
                Path pathMonitoringTypeList = mock(Path.class);
                Predicate equalsTargetIdPredicate = mock(Predicate.class);
                Predicate predicateMonitoringTypeList = mock(Predicate.class);
                Predicate existsPredicate = mock(Predicate.class);
                when(criteriaQuery.subquery(Integer.class)).thenReturn(subquery);
                when(subquery.from(TargetEntity.class)).thenReturn(targetEntityRoot);
                when(criteriaBuilder.literal(1)).thenReturn(expression);
                when(subquery.select(expression)).thenReturn(subquery);
                when(targetEntityRoot.get(TargetEntity_.targetId)).thenReturn(pathTargetTargetId);
                when(root.get(ResultEntity_.targetId)).thenReturn(pathResultTargetId);
                when(criteriaBuilder.equal(pathTargetTargetId, pathResultTargetId)).thenReturn(equalsTargetIdPredicate);
                when(targetEntityRoot.get(TargetEntity_.type)).thenReturn(pathMonitoringTypeList);
                when(pathMonitoringTypeList.in(filter.monitoringTypeList())).thenReturn(predicateMonitoringTypeList);
                when(subquery.where(equalsTargetIdPredicate, predicateMonitoringTypeList)).thenReturn(subquery);
                when(criteriaBuilder.exists(subquery)).thenReturn(existsPredicate);
                predicates.add(existsPredicate);
            }

            Predicate totalPredicate = mock(Predicate.class);
            when(criteriaBuilder.and(predicates.toArray(Predicate[]::new))).thenReturn(totalPredicate);

            Predicate predicate = resultEntitySpecification.toPredicate(root, criteriaQuery, criteriaBuilder);
            return predicate != null && predicate.equals(totalPredicate);
        }
    }

}