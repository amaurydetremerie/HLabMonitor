package be.wiserisk.hlabmonitor.monitor.infrastructure.config;

import be.wiserisk.hlabmonitor.monitor.application.port.in.execution.ExecuteCheckUseCase;
import be.wiserisk.hlabmonitor.monitor.application.port.in.query.GetCheckResultsUseCase;
import be.wiserisk.hlabmonitor.monitor.application.port.out.CheckTargetPort;
import be.wiserisk.hlabmonitor.monitor.application.port.out.PersistencePort;
import be.wiserisk.hlabmonitor.monitor.domain.service.GetResultService;
import be.wiserisk.hlabmonitor.monitor.domain.service.MonitoringService;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.HttpCheckAdapter;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.JpaPersistenceAdapter;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.repository.ResultEntityRepository;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.repository.TargetEntityRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class ApplicationConfig {

    @Bean
    public ExecuteCheckUseCase executeCheckUseCase(
            CheckTargetPort checkTargetPort,
            PersistencePort persistencePort) {
        return new MonitoringService(
                checkTargetPort,
                persistencePort);
    }

    @Bean
    public GetCheckResultsUseCase  getCheckResultsUseCase(PersistencePort persistencePort) {
        return new GetResultService(persistencePort);
    }

    @Bean
    public CheckTargetPort checkTargetPort(
            RestClient restClient) {
        return new HttpCheckAdapter(
                restClient);
    }

    @Bean
    public PersistencePort persistencePort(
            ResultEntityRepository resultEntityRepository,
            TargetEntityRepository targetEntityRepository,
            ObjectMapper objectMapper
    ) {
        return new JpaPersistenceAdapter(
                resultEntityRepository,
                targetEntityRepository,
                objectMapper);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public RestClient restClient() {
        return RestClient.builder().build();
    }
}
