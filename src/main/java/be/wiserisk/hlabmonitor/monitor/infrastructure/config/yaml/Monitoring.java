package be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml;

import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.monitoring.Http;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.monitoring.Ping;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.monitoring.Speedtest;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.notification.NotificationProperties;
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
    private Map<String, Speedtest> speedtest;

    private NotificationProperties notification;
}
