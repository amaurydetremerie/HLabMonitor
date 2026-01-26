package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.scheduler;

import be.wiserisk.hlabmonitor.monitor.application.port.out.CheckTriggerCallback;
import be.wiserisk.hlabmonitor.monitor.application.port.out.ScheduleHandle;
import be.wiserisk.hlabmonitor.monitor.domain.model.Target;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetId;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;

import static be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchedulerAdapterTest {

    public static final String TARGET = "target";
    public static final Duration INTERVAL = Duration.ofSeconds(30);

    public static final String PING_TARGET_ID_STRING = "ping-1";
    public static final TargetId PING_TARGET_ID = new TargetId(PING_TARGET_ID_STRING);
    public static final Target PING_TARGET = new Target(PING_TARGET_ID, PING, TARGET, INTERVAL);

    public static final String HTTP_TARGET_ID_STRING = "http-1";
    public static final TargetId HTTP_TARGET_ID = new TargetId(HTTP_TARGET_ID_STRING);
    public static final Target HTTP_TARGET = new Target(HTTP_TARGET_ID, HTTP, TARGET, INTERVAL);

    public static final String CERT_TARGET_ID_STRING = "cert-1";
    public static final TargetId CERT_TARGET_ID = new TargetId(CERT_TARGET_ID_STRING);
    public static final Target CERT_TARGET = new Target(CERT_TARGET_ID, CERTIFICATE, TARGET, INTERVAL);

    @Mock
    private ThreadPoolTaskScheduler scheduler;
    @Mock
    private TaskExecutor checkExecutor;
    @Mock
    private CheckTriggerCallback callback;
    @Mock
    private ScheduledFuture<?> scheduledFuture;

    private SchedulerAdapter schedulerAdapter;

    @BeforeEach
    void setUp() {
        schedulerAdapter = new SchedulerAdapter(scheduler, checkExecutor);
    }

    @Nested
    class CallbackTests {

        @Test
        void shouldExecuteCallbackSuccessfully() {
            when(scheduler.schedule(any(Runnable.class), any(PeriodicTrigger.class)))
                    .thenReturn((ScheduledFuture) scheduledFuture);

            doAnswer(invocation -> {
                Runnable task = invocation.getArgument(0);
                task.run();
                return null;
            }).when(checkExecutor).execute(any(Runnable.class));

            ArgumentCaptor<Runnable> scheduledTaskCaptor = ArgumentCaptor.forClass(Runnable.class);

            schedulerAdapter.scheduleTarget(PING_TARGET, callback);

            verify(scheduler).schedule(scheduledTaskCaptor.capture(), any(PeriodicTrigger.class));
            scheduledTaskCaptor.getValue().run();

            verify(checkExecutor).execute(any(Runnable.class));
            verify(callback).onTrigger(PING_TARGET_ID);
        }

        @Test
        void shouldCatchExceptionWhenCallbackThrows() {
            doThrow(new RuntimeException("Callback failed"))
                    .when(callback).onTrigger(HTTP_TARGET_ID);

            doAnswer(invocation -> {
                Runnable task = invocation.getArgument(0);
                task.run();
                return null;
            }).when(checkExecutor).execute(any(Runnable.class));

            when(scheduler.schedule(any(Runnable.class), any(PeriodicTrigger.class)))
                    .thenReturn((ScheduledFuture) scheduledFuture);

            ArgumentCaptor<Runnable> scheduledTaskCaptor = ArgumentCaptor.forClass(Runnable.class);

            schedulerAdapter.scheduleTarget(HTTP_TARGET, callback);

            verify(scheduler).schedule(scheduledTaskCaptor.capture(), any(PeriodicTrigger.class));

            assertThatCode(() -> scheduledTaskCaptor.getValue().run())
                    .doesNotThrowAnyException();

            verify(checkExecutor).execute(any(Runnable.class));
            verify(callback).onTrigger(HTTP_TARGET_ID);
        }
    }

    @Nested
    class UnscheduleTests {

        @Test
        void shouldCancelActiveHandle() {
            ScheduleHandle handle = mock(ScheduleHandle.class);
            when(handle.isActive()).thenReturn(true);

            schedulerAdapter.unschedule(handle);

            verify(handle).isActive();
            verify(handle).cancel();
        }

        @Test
        void shouldNotCancelInactiveHandle() {
            ScheduleHandle handle = mock(ScheduleHandle.class);
            when(handle.isActive()).thenReturn(false);

            schedulerAdapter.unschedule(handle);

            verify(handle).isActive();
            verify(handle, never()).cancel();
        }

        @Test
        void shouldNotThrowExceptionWhenHandleIsNull() {
            schedulerAdapter.unschedule(null);

            verifyNoInteractions(scheduler, checkExecutor);
        }

        @Test
        void shouldHandleSpringScheduleHandleCorrectly() {
            when(scheduledFuture.isCancelled()).thenReturn(false);
            when(scheduledFuture.isDone()).thenReturn(false);

            SpringScheduleHandle handle = new SpringScheduleHandle("test-id", scheduledFuture);

            schedulerAdapter.unschedule(handle);

            verify(scheduledFuture).cancel(false);
        }

        @Test
        void shouldNotCancelAlreadyCancelledHandle() {
            when(scheduledFuture.isCancelled()).thenReturn(true);

            SpringScheduleHandle handle = new SpringScheduleHandle("test-id", scheduledFuture);

            schedulerAdapter.unschedule(handle);

            verify(scheduledFuture, never()).cancel(anyBoolean());
        }

        @Test
        void shouldNotCancelAlreadyCompletedHandle() {
            when(scheduledFuture.isCancelled()).thenReturn(false);
            when(scheduledFuture.isDone()).thenReturn(true);

            SpringScheduleHandle handle = new SpringScheduleHandle("test-id", scheduledFuture);

            schedulerAdapter.unschedule(handle);

            verify(scheduledFuture, never()).cancel(anyBoolean());
        }
    }

    @Nested
    class IntegrationScenarios {
        @Test
        void shouldAllowScheduleThenUnschedule() {
            doReturn(scheduledFuture)
                    .when(scheduler)
                    .schedule(ArgumentMatchers.<Runnable>any(), ArgumentMatchers.<PeriodicTrigger>any());
            when(scheduledFuture.isCancelled()).thenReturn(false);
            when(scheduledFuture.isDone()).thenReturn(false);

            ScheduleHandle handle = schedulerAdapter.scheduleTarget(PING_TARGET, callback);
            schedulerAdapter.unschedule(handle);

            verify(scheduler).schedule(ArgumentMatchers.<Runnable>any(), ArgumentMatchers.<PeriodicTrigger>any());
            verify(scheduledFuture).cancel(false);
        }

        @Test
        void shouldHandleDifferentMonitoringTypes() {
            ScheduledFuture<?> future = mock(ScheduledFuture.class);
            doReturn(future).when(scheduler).schedule(ArgumentMatchers.<Runnable>any(), ArgumentMatchers.<PeriodicTrigger>any());

            ScheduleHandle handle1 = schedulerAdapter.scheduleTarget(PING_TARGET, callback);
            ScheduleHandle handle2 = schedulerAdapter.scheduleTarget(HTTP_TARGET, callback);
            ScheduleHandle handle3 = schedulerAdapter.scheduleTarget(CERT_TARGET, callback);

            assertThat(handle1.getTargetId()).isEqualTo(PING_TARGET_ID_STRING);
            assertThat(handle2.getTargetId()).isEqualTo(HTTP_TARGET_ID_STRING);
            assertThat(handle3.getTargetId()).isEqualTo(CERT_TARGET_ID_STRING);
            verify(scheduler, times(3)).schedule(ArgumentMatchers.<Runnable>any(), ArgumentMatchers.<PeriodicTrigger>any());
        }
    }
}