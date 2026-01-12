package be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml;

import java.time.Duration;

import static be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.Common.calculateInterval;

public record Certificate(Duration interval) implements Common {

    public static final Duration DEFAULT_INTERVAL = Duration.ofDays(1L);

    public Certificate(String interval) {
        this(calculateInterval(interval, DEFAULT_INTERVAL));
    }

    public Certificate() {
        this(DEFAULT_INTERVAL);
    }
}
