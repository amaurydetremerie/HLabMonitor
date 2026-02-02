package be.wiserisk.hlabmonitor.monitor.infrastructure.config;

import be.wiserisk.hlabmonitor.monitor.application.port.in.management.ManageMonitoringConfigUseCase;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.Monitoring;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.MonitoringToTargetAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Configuration
public class CommandLineRunnerConfiguration {

    @Bean
    public CommandLineRunner monitoringConfigSyncRunner(
            Monitoring monitoring,
            ManageMonitoringConfigUseCase manageMonitoringConfigUseCase,
            MonitoringToTargetAdapter monitoringToTargetAdapter) {
        return args ->
                CompletableFuture.runAsync(() ->
                        manageMonitoringConfigUseCase.syncFullConfiguration(monitoringToTargetAdapter.extractTargets(monitoring)))
                        .exceptionally(e -> { log.error("An exception occured when running syncFullConfiguration", e); return null; });
    }
}
