package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence;

import be.wiserisk.hlabmonitor.monitor.domain.model.*;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.entity.ResultEntity;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.entity.TargetEntity;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.repository.ResultEntityRepository;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.repository.TargetEntityRepository;
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
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringResult.SUCCESS;
import static be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringType.HTTP;
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
    @InjectMocks
    private JpaPersistenceAdapter jpaPersistenceAdapter;

    @Mock
    private ResultEntityRepository resultEntityRepository;
    @Mock
    private TargetEntityRepository targetEntityRepository;
    @Mock
    private ObjectMapper objectMapper;


    @Test
    void saveResult() {
        when(objectMapper.convertValue(TARGET_RESULT, ResultEntity.class)).thenReturn(RESULT_ENTITY);

        assertDoesNotThrow(() -> jpaPersistenceAdapter.saveResult(TARGET_RESULT));
        verify(resultEntityRepository, times(1)).save(RESULT_ENTITY);
    }

    @Test
    void getTarget() {
        Target target = new Target(TARGET_ID, HTTP, MESSAGE);
        TargetEntity targetEntity = new TargetEntity();

        when(targetEntityRepository.findByTargetId(TARGET_ID_STRING)).thenReturn(targetEntity);
        when(objectMapper.convertValue(targetEntity, Target.class)).thenReturn(target);

        assertThat(jpaPersistenceAdapter.getTarget(TARGET_ID)).isEqualTo(target);
    }

    @Test
    void getAllTargetResults() {

        when(objectMapper.convertValue(RESULT_ENTITY, TargetResult.class)).thenReturn(TARGET_RESULT);
        when(resultEntityRepository.findAll()).thenReturn(List.of(RESULT_ENTITY));

        assertThat(jpaPersistenceAdapter.getAllTargetResults()).isNotNull().isNotEmpty().isEqualTo(TARGET_RESULTS);
    }

    @Test
    void getAllTargetResultsByTargetId() {
        when(objectMapper.convertValue(RESULT_ENTITY, TargetResult.class)).thenReturn(TARGET_RESULT);
        when(resultEntityRepository.findAllByTargetId(TARGET_ID_STRING)).thenReturn(List.of(RESULT_ENTITY));

        assertThat(jpaPersistenceAdapter.getAllTargetResultsByTargetId(TARGET_ID)).isNotNull().isNotEmpty().isEqualTo(TARGET_RESULTS);
    }

    @Test
    void exist() {
        when(targetEntityRepository.existsByTargetId(TARGET_ID_STRING)).thenReturn(true);

        assertThat(jpaPersistenceAdapter.exist(TARGET_ID)).isTrue();
    }

    @Test
    void notExist() {
        when(targetEntityRepository.existsByTargetId(TARGET_ID_STRING)).thenReturn(false);

        assertThat(jpaPersistenceAdapter.exist(TARGET_ID)).isFalse();
    }

    @Test
    void getAllResultsFilteredBy() {
        PageImpl<ResultEntity> page = new PageImpl<>(List.of(RESULT_ENTITY), Pageable.ofSize(10), 1);
        PageResponse<TargetResult> pageResponse = new PageResponse<>(TARGET_RESULTS, 0, 10, 1, false);
        CheckResultsFilter filter = new CheckResultsFilter(LocalDateTime.MIN, LocalDateTime.MAX, List.of(TARGET_ID), List.of(SUCCESS), List.of(HTTP));
        PageRequest pageRequest = new PageRequest(0, 10);

        when(objectMapper.convertValue(RESULT_ENTITY, TargetResult.class)).thenReturn(TARGET_RESULT);
        when(resultEntityRepository.findAll(argThat(new ResultEntitySpecificationMatcher(filter)), any(org.springframework.data.domain.PageRequest.class))).thenReturn(page);

        assertThat(jpaPersistenceAdapter.getAllResultsFilteredBy(filter, pageRequest)).isNotNull().isEqualTo(pageResponse);
    }

    @Test
    void getAllResultsFilteredByFiltersEmpty() {
        PageImpl<ResultEntity> page = new PageImpl<>(List.of(RESULT_ENTITY), Pageable.ofSize(10), 1);
        PageResponse<TargetResult> pageResponse = new PageResponse<>(TARGET_RESULTS, 0, 10, 1, false);
        CheckResultsFilter filter = new CheckResultsFilter(null, null, List.of(), List.of(), List.of());
        PageRequest pageRequest = new PageRequest(0, 10);

        when(objectMapper.convertValue(RESULT_ENTITY, TargetResult.class)).thenReturn(TARGET_RESULT);
        when(resultEntityRepository.findAll(argThat(new ResultEntitySpecificationMatcher(filter)), any(org.springframework.data.domain.PageRequest.class))).thenReturn(page);

        assertThat(jpaPersistenceAdapter.getAllResultsFilteredBy(filter, pageRequest)).isNotNull().isEqualTo(pageResponse);
    }

    @Test
    void getAllResultsFilteredByNoFilters() {
        PageImpl<ResultEntity> page = new PageImpl<>(List.of(RESULT_ENTITY), Pageable.ofSize(10), 1);
        PageResponse<TargetResult> pageResponse = new PageResponse<>(TARGET_RESULTS, 0, 10, 1, false);
        CheckResultsFilter filter = new CheckResultsFilter(null, null, null, null, null);
        PageRequest pageRequest = new PageRequest(0, 10);

        when(objectMapper.convertValue(RESULT_ENTITY, TargetResult.class)).thenReturn(TARGET_RESULT);
        when(resultEntityRepository.findAll(argThat(new ResultEntitySpecificationMatcher(filter)), any(org.springframework.data.domain.PageRequest.class))).thenReturn(page);

        assertThat(jpaPersistenceAdapter.getAllResultsFilteredBy(filter, pageRequest)).isNotNull().isEqualTo(pageResponse);
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

            if(filter.from() != null) {
                Path pathFrom = mock(Path.class);
                Predicate predicateFrom = mock(Predicate.class);
                when(root.get("from")).thenReturn(pathFrom);
                when(criteriaBuilder.greaterThanOrEqualTo(pathFrom, filter.from())).thenReturn(predicateFrom);
                predicates.add(predicateFrom);
            }

            if(filter.to() != null) {
                Path pathTo = mock(Path.class);
                Predicate predicateTo = mock(Predicate.class);
                when(root.get("to")).thenReturn(pathTo);
                when(criteriaBuilder.lessThanOrEqualTo(pathTo, filter.to())).thenReturn(predicateTo);
                predicates.add(predicateTo);
            }

            if(filter.targetIdList() != null && !filter.targetIdList().isEmpty()) {
                Path pathTargetIdList = mock(Path.class);
                Predicate predicateTargetIdList = mock(Predicate.class);
                when(root.get("targetId")).thenReturn(pathTargetIdList);
                when(pathTargetIdList.in(filter.targetIdList())).thenReturn(predicateTargetIdList);
                predicates.add(predicateTargetIdList);
            }

            if(filter.monitoringResultList() != null && !filter.monitoringResultList().isEmpty()) {
                Path pathMonitoringResultList = mock(Path.class);
                Predicate predicateMonitoringResultList = mock(Predicate.class);
                when(root.get("result")).thenReturn(pathMonitoringResultList);
                when(pathMonitoringResultList.in(filter.monitoringResultList())).thenReturn(predicateMonitoringResultList);
                predicates.add(predicateMonitoringResultList);
            }

            if(filter.monitoringTypeList() != null && !filter.monitoringTypeList().isEmpty()) {
                Path pathMonitoringTypeList = mock(Path.class);
                Predicate predicateMonitoringTypeList = mock(Predicate.class);
                when(root.get("type")).thenReturn(pathMonitoringTypeList);
                when(pathMonitoringTypeList.in(filter.monitoringTypeList())).thenReturn(predicateMonitoringTypeList);
                predicates.add(predicateMonitoringTypeList);
            }

            Predicate totalPredicate = mock(Predicate.class);
            when(criteriaBuilder.and(predicates.toArray(Predicate[]::new))).thenReturn(totalPredicate);

            Predicate predicate = resultEntitySpecification.toPredicate(root, criteriaQuery, criteriaBuilder);
            return predicate != null && predicate.equals(totalPredicate);
        }
    }

}