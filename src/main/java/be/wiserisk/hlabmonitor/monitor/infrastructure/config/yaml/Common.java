package be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml;

import java.time.Duration;

public interface Common {
    Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5L);
    Duration DEFAULT_DURATION = Duration.ofMinutes(5L);


    private static Duration parseInterval(String interval) {
        return switch (interval.charAt(interval.length() - 1)) {
            case 's' -> Duration.ofSeconds(getIntervalWithoutSuffix(interval));
            case 'm' -> Duration.ofMinutes(getIntervalWithoutSuffix(interval));
            case 'h' -> Duration.ofHours(getIntervalWithoutSuffix(interval));
            case 'd' -> Duration.ofDays(getIntervalWithoutSuffix(interval));
            case 'w' -> Duration.ofDays(getIntervalWithoutSuffix(interval) * 7);
            default -> throw new IllegalArgumentException("Invalid interval: " + interval);
        };
    }

    static Duration calculateInterval(String interval, Duration defaultInterval) {
        if (interval == null || interval.isEmpty()) {
            if(defaultInterval == null)
                return DEFAULT_DURATION;
            return defaultInterval;
        }
        try {
            return parseInterval(interval);
        } catch (IllegalArgumentException e) {
            return defaultInterval;
        }
    }

    private static long getIntervalWithoutSuffix(String interval) {
        return Long.parseLong(interval.substring(0, interval.length() - 1));
    }
}
