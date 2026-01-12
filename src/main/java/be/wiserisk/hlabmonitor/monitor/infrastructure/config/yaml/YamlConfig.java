package be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationPropertiesScan("be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml")
public class YamlConfig {

    @Bean
    public Monitoring monitoring() {
        return new Monitoring();
    }
}
