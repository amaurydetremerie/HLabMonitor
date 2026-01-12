package be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml;

import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.time.Duration;

import static be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.Common.calculateInterval;

public record Ping(String target, Duration interval) implements Common {
    public static final Duration DEFAULT_INTERVAL = Duration.ofMinutes(5L);

    @ConstructorBinding
    public Ping(String target, String interval) {
        this(target, calculateInterval(interval, DEFAULT_INTERVAL));
    }
}
