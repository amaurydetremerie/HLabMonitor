package be.wiserisk.hlabmonitor.monitor.infrastructure.config;

import be.wiserisk.hlabmonitor.monitor.application.port.out.CheckTargetPort;
import be.wiserisk.hlabmonitor.monitor.application.port.out.MonitoringSchedulerPort;
import be.wiserisk.hlabmonitor.monitor.application.port.out.NotificationPort;
import be.wiserisk.hlabmonitor.monitor.application.port.out.PersistencePort;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.HttpCheckAdapter;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.in.notification.NotificationAdapter;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.JpaPersistenceAdapter;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.repository.NotificationEntityRepository;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.repository.ResultEntityRepository;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.repository.TargetEntityRepository;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.scheduler.SchedulerAdapter;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.mapper.NotificationMapper;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.mapper.ResultMapper;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.mapper.TargetMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.client.RestClient;

@Configuration
public class AdapterConfig {

    @Bean
    public NotificationPort notificationPort( MessageChannel notificationChannel) {
        return new NotificationAdapter(notificationChannel);
    }

    @Bean
    public CheckTargetPort checkTargetPort(
            RestClient restClient) {
        return new HttpCheckAdapter(
                restClient);
    }

    @Bean
    public MonitoringSchedulerPort monitoringSchedulerPort(
            ThreadPoolTaskScheduler monitoringTaskScheduler,
            TaskExecutor checkExecutor) {
        return new SchedulerAdapter(monitoringTaskScheduler, checkExecutor);
    }

    @Bean
    public PersistencePort persistencePort(
            NotificationEntityRepository notificationEntityRepository,
            ResultEntityRepository resultEntityRepository,
            TargetEntityRepository targetEntityRepository,
            NotificationMapper notificationMapper,
            TargetMapper targetMapper,
            ResultMapper resultMapper
    ) {
        return new JpaPersistenceAdapter(
                notificationEntityRepository,
                resultEntityRepository,
                targetEntityRepository,
                notificationMapper,
                targetMapper,
                resultMapper);
    }
}
