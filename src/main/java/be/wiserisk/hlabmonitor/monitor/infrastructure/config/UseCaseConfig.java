package be.wiserisk.hlabmonitor.monitor.infrastructure.config;

import be.wiserisk.hlabmonitor.monitor.application.port.in.execution.ExecuteCheckUseCase;
import be.wiserisk.hlabmonitor.monitor.application.port.in.management.ManageMonitoringConfigUseCase;
import be.wiserisk.hlabmonitor.monitor.application.port.in.query.GetCheckResultsUseCase;
import be.wiserisk.hlabmonitor.monitor.application.port.in.query.GetCheckTargetIdsUseCase;
import be.wiserisk.hlabmonitor.monitor.application.port.out.CheckTargetPort;
import be.wiserisk.hlabmonitor.monitor.application.port.out.MonitoringSchedulerPort;
import be.wiserisk.hlabmonitor.monitor.application.port.out.PersistencePort;
import be.wiserisk.hlabmonitor.monitor.domain.service.GetResultService;
import be.wiserisk.hlabmonitor.monitor.domain.service.GetTargetIdService;
import be.wiserisk.hlabmonitor.monitor.domain.service.ManageService;
import be.wiserisk.hlabmonitor.monitor.domain.service.MonitoringService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public ExecuteCheckUseCase executeCheckUseCase(
            CheckTargetPort checkTargetPort,
            PersistencePort persistencePort) {
        return new MonitoringService(
                checkTargetPort,
                persistencePort);
    }

    @Bean
    public ManageMonitoringConfigUseCase manageMonitoringConfigUseCase(
            PersistencePort persistencePort,
            MonitoringSchedulerPort schedulerPort,
            ExecuteCheckUseCase executeCheckUseCase) {
        return new ManageService(persistencePort, schedulerPort, executeCheckUseCase);
    }

    @Bean
    public GetCheckResultsUseCase getCheckResultsUseCase(PersistencePort persistencePort) {
        return new GetResultService(persistencePort);
    }

    @Bean
    public GetCheckTargetIdsUseCase getCheckTargetIdsUseCase(PersistencePort persistencePort) {
        return new GetTargetIdService(persistencePort);
    }
}
