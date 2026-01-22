package be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "monitoring")
@AllArgsConstructor
@NoArgsConstructor
public class Monitoring {
    private Map<String, Ping> ping;
    private Map<String, Http> http;
}
