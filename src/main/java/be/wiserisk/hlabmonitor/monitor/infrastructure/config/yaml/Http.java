package be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml;

import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.time.Duration;
import java.util.Map;

import static be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.Common.calculateInterval;

public record Http(String target, Duration interval, boolean ssl, Certificate certificate) implements Common {
    public static final Duration DEFAULT_INTERVAL = Duration.ofMinutes(10L);

    @ConstructorBinding
    public Http(String target, String interval, Boolean ssl, Map<String, String> certificate) {
        this(addHttpToTarget(target, isSecured(ssl)), calculateInterval(interval, DEFAULT_INTERVAL), isSecured(ssl), getCertificateConfig(isSecured(ssl), certificate));
    }

    private static String addHttpToTarget(String target, boolean secured) {
        return secured ? "https://" + getTargetWithoutProtocol(target) : "http://" + getTargetWithoutProtocol(target);
    }

    private static String getTargetWithoutProtocol(String target) {
        String[] split = target.split("//");
        return split.length == 1 ? split[0] : split[1];
    }

    private static boolean isSecured(Boolean secured) {
        return !Boolean.FALSE.equals(secured);
    }

    private static Certificate getCertificateConfig(boolean secured, Map<String, String> certificate) {
        if(!secured) return null;
        if(certificate == null || certificate.isEmpty()) return new Certificate();
        if(!Boolean.parseBoolean(certificate.getOrDefault("verify", Boolean.TRUE.toString()))) return null;
        return new Certificate(certificate.get("interval"));
    }
}
