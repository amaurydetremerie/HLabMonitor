package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.scheduler;

import be.wiserisk.hlabmonitor.monitor.application.port.out.CheckTriggerCallback;
import be.wiserisk.hlabmonitor.monitor.application.port.out.ScheduleHandle;
import be.wiserisk.hlabmonitor.monitor.domain.model.Target;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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

    @Captor
    private ArgumentCaptor<Runnable> runnableCaptor;
    @Captor
    private ArgumentCaptor<PeriodicTrigger> triggerCaptor;

    @InjectMocks
    private SchedulerAdapter schedulerAdapter;

    @BeforeEach
    void setUp() {
        Mockito.clearInvocations(scheduler, checkExecutor, callback);
        // Les captors seront réinitialisés automatiquement avec MockitoExtension
    }

    @Nested
    class ScheduleTargetTests {
        @Test
        void shouldScheduleTaskWithCorrectInterval() {
            doReturn(scheduledFuture)
                    .when(scheduler)
                    .schedule(ArgumentMatchers.<Runnable>any(), ArgumentMatchers.<PeriodicTrigger>any());

            schedulerAdapter.scheduleTarget(PING_TARGET, callback);

            verify(scheduler).schedule(any(Runnable.class), triggerCaptor.capture());
            PeriodicTrigger capturedTrigger = triggerCaptor.getValue();
            assertThat(capturedTrigger.getPeriodDuration()).isEqualTo(Duration.ofSeconds(30));
        }

        @Test
        void shouldConfigureTriggerAsNonFixedRate() {
            doReturn(scheduledFuture)
                    .when(scheduler)
                    .schedule(any(Runnable.class), any(PeriodicTrigger.class));

            schedulerAdapter.scheduleTarget(PING_TARGET, callback);

            verify(scheduler).schedule(any(Runnable.class), triggerCaptor.capture());
            PeriodicTrigger capturedTrigger = triggerCaptor.getValue();
            assertThat(capturedTrigger.isFixedRate()).isFalse();
        }


        @Test
        void shouldReturnHandleWithCorrectTargetId() {
            doReturn(scheduledFuture)
                    .when(scheduler)
                    .schedule(ArgumentMatchers.<Runnable>any(), ArgumentMatchers.<PeriodicTrigger>any());

            ScheduleHandle handle = schedulerAdapter.scheduleTarget(PING_TARGET, callback);

            assertThat(handle).isNotNull();
            assertThat(handle.getTargetId()).isEqualTo(PING_TARGET_ID_STRING);
        }

        @Test
        void shouldReturnSpringScheduleHandleWithScheduledFuture() {
            doReturn(scheduledFuture)
                    .when(scheduler)
                    .schedule(ArgumentMatchers.<Runnable>any(), ArgumentMatchers.<PeriodicTrigger>any());

            ScheduleHandle handle = schedulerAdapter.scheduleTarget(PING_TARGET, callback);

            assertThat(handle).isInstanceOf(SpringScheduleHandle.class);
        }

        @Test
        void shouldExecuteCallbackInTaskExecutorWhenTaskTriggered() {
            doReturn(scheduledFuture)
                    .when(scheduler)
                    .schedule(ArgumentMatchers.<Runnable>any(), ArgumentMatchers.<PeriodicTrigger>any());
            doAnswer(invocation -> {
                Runnable runnable = invocation.getArgument(0);
                runnable.run();
                return null;
            }).when(checkExecutor).execute(ArgumentMatchers.<Runnable>any());

            schedulerAdapter.scheduleTarget(PING_TARGET, callback);

            verify(scheduler).schedule(runnableCaptor.capture(), ArgumentMatchers.<PeriodicTrigger>any());
            Runnable scheduledTask = runnableCaptor.getValue();
            scheduledTask.run();

            verify(checkExecutor).execute(ArgumentMatchers.<Runnable>any());
            verify(callback).onTrigger(PING_TARGET.id());
        }

        @Test
        void shouldCallCallbackWithCorrectTargetId() {
            doAnswer(invocation -> {
                Runnable runnable = invocation.getArgument(0);
                runnable.run();
                return null;
            }).when(checkExecutor).execute(ArgumentMatchers.<Runnable>any());

            doReturn(scheduledFuture)
                    .when(scheduler)
                    .schedule(ArgumentMatchers.<Runnable>any(), ArgumentMatchers.<PeriodicTrigger>any());

            schedulerAdapter.scheduleTarget(PING_TARGET, callback);

            verify(scheduler).schedule(runnableCaptor.capture(), ArgumentMatchers.<PeriodicTrigger>any());
            runnableCaptor.getValue().run();

            verify(callback).onTrigger(eq(PING_TARGET_ID));
        }

        @Test
        void shouldLogErrorWhenCallbackThrowsException() {
            doReturn(scheduledFuture)
                    .when(scheduler)
                    .schedule(ArgumentMatchers.<Runnable>any(), ArgumentMatchers.<PeriodicTrigger>any());
            doAnswer(invocation -> {
                Runnable runnable = invocation.getArgument(0);
                runnable.run();
                return null;
            }).when(checkExecutor).execute(ArgumentMatchers.<Runnable>any());
            doThrow(new RuntimeException("Check failed"))
                    .when(callback).onTrigger(ArgumentMatchers.<TargetId>any());

            schedulerAdapter.scheduleTarget(PING_TARGET, callback);

            verify(scheduler).schedule(runnableCaptor.capture(), ArgumentMatchers.<PeriodicTrigger>any());

            runnableCaptor.getValue().run();

            verify(callback).onTrigger(ArgumentMatchers.<TargetId>any());
        }

        @Test
        void shouldScheduleMultipleTargetsIndependently() {
            ScheduledFuture<?> future1 = mock(ScheduledFuture.class);
            ScheduledFuture<?> future2 = mock(ScheduledFuture.class);

            doReturn(future1, future2)
                    .when(scheduler)
                    .schedule(ArgumentMatchers.<Runnable>any(), ArgumentMatchers.<PeriodicTrigger>any());

            ScheduleHandle handle1 = schedulerAdapter.scheduleTarget(PING_TARGET, callback);
            ScheduleHandle handle2 = schedulerAdapter.scheduleTarget(HTTP_TARGET, callback);

            assertThat(handle1.getTargetId()).isEqualTo(PING_TARGET_ID_STRING);
            assertThat(handle2.getTargetId()).isEqualTo(HTTP_TARGET_ID_STRING);
            verify(scheduler, times(2)).schedule(ArgumentMatchers.<Runnable>any(), ArgumentMatchers.<PeriodicTrigger>any());
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