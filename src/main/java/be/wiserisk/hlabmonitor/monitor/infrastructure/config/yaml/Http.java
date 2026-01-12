package be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml;

import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.time.Duration;
import java.util.Map;

import static be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.Common.calculateInterval;

public record Http(String target, Duration interval, boolean ssl, Certificate certificate) implements Common {
    public static final Duration DEFAULT_INTERVAL = Duration.ofMinutes(10L);

    @ConstructorBinding
    public Http(String target, String interval, Boolean ssl, Map<String, String> certificate) {
        this(target, calculateInterval(interval, DEFAULT_INTERVAL), isSecured(ssl), getCertificateConfig(isSecured(ssl), certificate));
    }

    private static boolean isSecured(Boolean secured) {
        return !Boolean.FALSE.equals(secured);
    }

    private static Certificate getCertificateConfig(boolean secured, Map<String, String> certificate) {
        if(certificate == null || certificate.isEmpty()) return null;
        if(secured) {
            if(Boolean.parseBoolean(certificate.getOrDefault("verify", Boolean.TRUE.toString()))) {
                return new Certificate(certificate.get("interval"));
            }
            return new Certificate();
        }
        return null;
    }
}
