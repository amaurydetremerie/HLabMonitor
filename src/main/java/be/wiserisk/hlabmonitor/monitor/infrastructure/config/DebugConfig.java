package be.wiserisk.hlabmonitor.monitor.infrastructure.config;

import be.wiserisk.hlabmonitor.monitor.application.port.in.execution.ExecuteCheckUseCase;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.in.rest.ExecuteCheckController;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DebugConfig {

    @Bean
    @ConditionalOnProperty(
            name = "debug.controller.enabled",
            havingValue = "true",
            matchIfMissing = false
    )
    public ExecuteCheckController executeCheckController(ExecuteCheckUseCase executeCheckUseCase) {
        return new ExecuteCheckController(executeCheckUseCase);
    }
}
