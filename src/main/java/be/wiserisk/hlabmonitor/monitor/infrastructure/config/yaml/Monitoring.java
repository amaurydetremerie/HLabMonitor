package be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "monitoring")
public class Monitoring {
    private Map<String, Ping> ping;
    private Map<String, Http> http;
}
