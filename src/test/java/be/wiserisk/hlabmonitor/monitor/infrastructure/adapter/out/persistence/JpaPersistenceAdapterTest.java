package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence;

import be.wiserisk.hlabmonitor.monitor.domain.model.Target;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetId;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetResult;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.entity.ResultEntity;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.entity.TargetEntity;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.repository.ResultEntityRepository;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.repository.TargetEntityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import static be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringResult.SUCCESS;
import static be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringType.HTTP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JpaPersistenceAdapterTest {
    public static final String TARGET_ID = "TargetId";
    @InjectMocks
    private JpaPersistenceAdapter jpaPersistenceAdapter;

    @Mock
    private ResultEntityRepository resultEntityRepository;
    @Mock
    private TargetEntityRepository targetEntityRepository;
    @Mock
    private ObjectMapper objectMapper;

    public static final String MESSAGE = "message";

    @Test
    void saveResult() {
        TargetId targetId = new TargetId("TargetId");
        TargetResult targetResult = new TargetResult(targetId, SUCCESS, MESSAGE);
        ResultEntity resultEntity = new ResultEntity();

        when(objectMapper.convertValue(targetResult, ResultEntity.class)).thenReturn(resultEntity);

        assertDoesNotThrow(() -> jpaPersistenceAdapter.saveResult(targetResult));
        verify(resultEntityRepository, times(1)).save(resultEntity);
    }

    @Test
    void getTarget() {
        TargetId targetId = new TargetId(TARGET_ID);
        Target target = new Target(targetId, HTTP, MESSAGE);
        TargetEntity targetEntity = new TargetEntity();

        when(targetEntityRepository.findByTargetId(TARGET_ID)).thenReturn(targetEntity);
        when(objectMapper.convertValue(targetEntity, Target.class)).thenReturn(target);

        assertThat(jpaPersistenceAdapter.getTarget(targetId)).isEqualTo(target);
    }

}