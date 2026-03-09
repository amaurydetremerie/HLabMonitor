package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.in.rest;

import be.wiserisk.hlabmonitor.monitor.application.port.in.execution.ExecuteCheckUseCase;
import be.wiserisk.hlabmonitor.monitor.application.port.in.execution.ExecuteNotificationUseCase;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExecuteCheckControllerTest {

    public static final String TARGET_ID_STRING = "targetId";
    public static final TargetId TARGET_ID = new TargetId(TARGET_ID_STRING);
    @InjectMocks
    private DebugController executeCheckController;
    @Mock
    private ExecuteCheckUseCase executeCheckUseCase;
    @Mock
    private ExecuteNotificationUseCase executeNotificationUseCase;

    @Test
    void executeByTargetId() {
        assertThat(executeCheckController.executeByTargetId(TARGET_ID_STRING)).isNotNull().isEqualTo(ResponseEntity.accepted().build());

        verify(executeCheckUseCase, times(1)).executeCheck(TARGET_ID);
    }

    @Test
    void deleteByNotificationId() {
        assertThat(executeCheckController.deleteByNotificationId(1L)).isNotNull().isEqualTo(ResponseEntity.accepted().build());

        verify(executeNotificationUseCase, times(1)).deleteByNotificationId(1L);
    }


}