package be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml;

import be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringType;
import be.wiserisk.hlabmonitor.monitor.domain.model.Target;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetId;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public class MonitoringToTargetAdapter {
    public List<Target> extractTargets(Monitoring monitoring) {
        return Stream.of(
                        extractPingTargets(monitoring.getPing()),
                        extractHttpTargets(monitoring.getHttp()),
                        extractCertificateTargets(monitoring.getHttp())
                )
                .flatMap(Function.identity())
                .toList();
    }

    private Stream<Target> extractPingTargets(Map<String, Ping> pings) {
        if (pings == null || pings.isEmpty()) {
            return Stream.empty();
        }
        return pings.entrySet().stream()
                .map(entry -> new Target(
                        new TargetId(entry.getKey()),
                        MonitoringType.PING,
                        entry.getValue().target(),
                        entry.getValue().interval()
                ));
    }

    private Stream<Target> extractHttpTargets(Map<String, Http> https) {
        if (https == null || https.isEmpty()) {
            return Stream.empty();
        }
        return https.entrySet().stream()
                .map(entry -> new Target(
                        new TargetId(entry.getKey()),
                        MonitoringType.HTTP,
                        entry.getValue().target(),
                        entry.getValue().interval()
                ));
    }

    private Stream<Target> extractCertificateTargets(Map<String, Http> https) {
        if (https == null || https.isEmpty()) {
            return Stream.empty();
        }
        return https.entrySet().stream()
                .filter(entry -> entry.getValue().ssl())
                .map(entry -> new Target(
                        new TargetId(entry.getKey() + ":certificate"),
                        MonitoringType.CERTIFICATE,
                        entry.getValue().target(),
                        entry.getValue().certificate().interval()
                ));
    }
}
