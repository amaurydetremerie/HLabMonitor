package be.wiserisk.hlabmonitor.monitor.infrastructure.config;

import be.wiserisk.hlabmonitor.monitor.application.port.in.execution.ExecuteCheckUseCase;
import be.wiserisk.hlabmonitor.monitor.application.port.in.execution.ExecuteNotificationUseCase;
import be.wiserisk.hlabmonitor.monitor.application.port.in.management.ManageMonitoringConfigUseCase;
import be.wiserisk.hlabmonitor.monitor.application.port.in.query.GetCheckNotificationsUseCase;
import be.wiserisk.hlabmonitor.monitor.application.port.in.query.GetCheckResultsUseCase;
import be.wiserisk.hlabmonitor.monitor.application.port.in.query.GetCheckStatisticsUseCase;
import be.wiserisk.hlabmonitor.monitor.application.port.in.query.GetCheckTargetIdsUseCase;
import be.wiserisk.hlabmonitor.monitor.application.port.out.CheckTargetPort;
import be.wiserisk.hlabmonitor.monitor.application.port.out.MonitoringSchedulerPort;
import be.wiserisk.hlabmonitor.monitor.application.port.out.NotificationPort;
import be.wiserisk.hlabmonitor.monitor.application.port.out.PersistencePort;
import be.wiserisk.hlabmonitor.monitor.domain.service.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public NotificationService notificationService(
            PersistencePort persistencePort,
            NotificationPort notificationPort) {
        return new NotificationService(persistencePort, notificationPort);
    }

    @Bean
    public GetCheckNotificationsUseCase getCheckNotificationsUseCase(NotificationService notificationService) {
        return notificationService;
    }

    @Bean
    public ExecuteNotificationUseCase executeNotificationUseCase(NotificationService notificationService) {
        return notificationService;
    }

    @Bean
    public ExecuteCheckUseCase executeCheckUseCase(
            CheckTargetPort checkTargetPort,
            PersistencePort persistencePort,
            ExecuteNotificationUseCase executeNotificationUseCase) {
        return new MonitoringService(
                checkTargetPort,
                persistencePort,
                executeNotificationUseCase);
    }

    @Bean
    public ManageMonitoringConfigUseCase manageMonitoringConfigUseCase(
            PersistencePort persistencePort,
            MonitoringSchedulerPort schedulerPort,
            ExecuteCheckUseCase executeCheckUseCase,
            ExecuteNotificationUseCase executeNotificationUseCase) {
        return new ManageService(persistencePort, schedulerPort, executeCheckUseCase, executeNotificationUseCase);
    }

    @Bean
    public GetCheckResultsUseCase getCheckResultsUseCase(PersistencePort persistencePort) {
        return new GetResultService(persistencePort);
    }

    @Bean
    public GetCheckTargetIdsUseCase getCheckTargetIdsUseCase(PersistencePort persistencePort) {
        return new GetTargetIdService(persistencePort);
    }

    @Bean
    public GetCheckStatisticsUseCase getCheckStatisticsUseCase(PersistencePort persistencePort) {
        return new GetStatisticsService(persistencePort);
    }
}
