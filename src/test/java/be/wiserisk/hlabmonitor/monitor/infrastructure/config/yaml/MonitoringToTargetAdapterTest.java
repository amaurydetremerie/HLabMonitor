package be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml;

import be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringType;
import be.wiserisk.hlabmonitor.monitor.domain.model.Target;
import be.wiserisk.hlabmonitor.monitor.domain.enums.SpeedtestType;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.monitoring.Certificate;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.monitoring.Http;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.monitoring.MonitoringToTargetAdapter;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.monitoring.Ping;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.monitoring.Speedtest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringType.PING;
import static org.assertj.core.api.Assertions.assertThat;

class MonitoringToTargetAdapterTest {

    private final MonitoringToTargetAdapter adapter = new MonitoringToTargetAdapter();

    @Nested
    class ExtractTargetsTests {

        @Test
        void shouldReturnEmptyListWhenMonitoringIsEmpty() {
            Monitoring monitoring = new Monitoring();

            List<Target> result = adapter.extractTargets(monitoring);

            assertThat(result).isEmpty();
        }

        @Test
        void shouldExtractOnlyPingTargets() {
            Map<String, Ping> pings = new HashMap<>();
            pings.put("ping1", new Ping("192.168.1.1", Duration.ofSeconds(30)));
            pings.put("ping2", new Ping("google.com", Duration.ofMinutes(1)));
            Monitoring monitoring = new Monitoring(pings, null, null, null);

            List<Target> result = adapter.extractTargets(monitoring);

            assertThat(result).hasSize(2);
            assertThat(result).extracting(Target::type)
                    .containsOnly(PING);
            assertThat(result).extracting(t -> t.id().id())
                    .containsExactlyInAnyOrder("ping1:ping", "ping2:ping");
        }

        @Test
        void shouldExtractInternalHttpTargets() {
            Map<String, Http> https = new HashMap<>();
            https.put("http1", new Http("https://example.com", Duration.ofSeconds(60), false, null, null, true));
            Monitoring monitoring = new Monitoring(null, https, null, null);

            List<Target> result = adapter.extractTargets(monitoring);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().type()).isEqualTo(MonitoringType.HTTP_INTERNAL);
            assertThat(result.getFirst().id().id()).isEqualTo("http1:http");
        }

        @Test
        void shouldExtractOnlyHttpTargets() {
            Map<String, Http> https = new HashMap<>();
            https.put("http1", new Http("https://example.com", Duration.ofSeconds(60), false, null, null, false));
            Monitoring monitoring = new Monitoring(null, https, null, null);

            List<Target> result = adapter.extractTargets(monitoring);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().type()).isEqualTo(MonitoringType.HTTP);
            assertThat(result.getFirst().id().id()).isEqualTo("http1:http");
        }

        @Test
        void shouldExtractHttpAndCertificateTargetsWhenSslEnabled() {
            Certificate cert = new Certificate(Duration.ofDays(1));
            Map<String, Http> https = new HashMap<>();
            https.put("https1", new Http("https://secure.com", Duration.ofSeconds(60), true, cert, null, false));
            Monitoring monitoring = new Monitoring(null, https, null, null);

            List<Target> result = adapter.extractTargets(monitoring);

            assertThat(result).hasSize(2);
            assertThat(result).extracting(Target::type)
                    .containsExactlyInAnyOrder(MonitoringType.HTTP, MonitoringType.CERTIFICATE);
        }

        @Test
        void shouldExtractAllTargetTypesCombined() {
            Map<String, Ping> pings = new HashMap<>();
            pings.put("ping1", new Ping("192.168.1.1", Duration.ofSeconds(30)));

            Certificate cert = new Certificate(Duration.ofDays(7));
            Map<String, Http> https = new HashMap<>();
            https.put("http1", new Http("https://example.com", Duration.ofSeconds(60), false, null, null, false));
            https.put("https1", new Http("https://secure.com", Duration.ofSeconds(60), true, cert, null, false));
            https.put("https2", new Http("https://secure.com", Duration.ofSeconds(60), true, null, null, false));

            Monitoring monitoring = new Monitoring(pings, https, null, null);

            List<Target> result = adapter.extractTargets(monitoring);

            assertThat(result).hasSize(5);
            assertThat(result).extracting(Target::type)
                    .containsExactlyInAnyOrder(
                            PING,
                            MonitoringType.HTTP,
                            MonitoringType.HTTP,
                            MonitoringType.CERTIFICATE,
                            MonitoringType.HTTP
                    );
        }

        @Test
        void shouldUseCorrectTargetAndIntervalForEachType() {
            Duration pingInterval = Duration.ofSeconds(30);
            Duration httpInterval = Duration.ofSeconds(60);
            Duration certInterval = Duration.ofDays(1);

            Map<String, Ping> pings = new HashMap<>();
            pings.put("ping1", new Ping("192.168.1.1", pingInterval));

            Certificate cert = new Certificate(certInterval);
            Map<String, Http> https = new HashMap<>();
            https.put("https1", new Http("https://secure.com", httpInterval, true, cert, null, false));

            Monitoring monitoring = new Monitoring(pings, https, null, null);

            List<Target> result = adapter.extractTargets(monitoring);

            Target pingTarget = result.stream()
                    .filter(t -> t.type() == PING)
                    .findFirst()
                    .orElseThrow();
            assertThat(pingTarget.target()).isEqualTo("192.168.1.1");
            assertThat(pingTarget.interval()).isEqualTo(pingInterval);

            Target httpTarget = result.stream()
                    .filter(t -> t.type() == MonitoringType.HTTP)
                    .findFirst()
                    .orElseThrow();
            assertThat(httpTarget.target()).isEqualTo("https://secure.com");
            assertThat(httpTarget.interval()).isEqualTo(httpInterval);

            Target certTarget = result.stream()
                    .filter(t -> t.type() == MonitoringType.CERTIFICATE)
                    .findFirst()
                    .orElseThrow();
            assertThat(certTarget.target()).isEqualTo("https://secure.com");
            assertThat(certTarget.interval()).isEqualTo(certInterval);
            assertThat(certTarget.id().id()).isEqualTo("https1:certificate");
        }
    }

    @Nested
    class ExtractPingTargetsTests {

        @Test
        void shouldReturnEmptyStreamWhenPingsIsNull() {
            List<Target> result = adapter.extractTargets(new Monitoring());
            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnEmptyStreamWhenPingsIsEmpty() {
            List<Target> result = adapter.extractTargets(new Monitoring(Map.of(), null, null, null));
            assertThat(result).isEmpty();
        }

        @Test
        void shouldCreatePingTargetsWithCorrectProperties() {
            Map<String, Ping> pings = new HashMap<>();
            pings.put("server1", new Ping("192.168.1.10", Duration.ofSeconds(15)));
            pings.put("server2", new Ping("10.0.0.1", Duration.ofMinutes(2)));

            List<Target> result = adapter.extractTargets(new Monitoring(pings, null, null, null));

            assertThat(result).hasSize(2).allMatch(t -> t.type() == PING);
        }
    }

    @Nested
    class ExtractHttpTargetsTests {

        @Test
        void shouldReturnEmptyStreamWhenHttpsIsNull() {
            List<Target> result = adapter.extractTargets(new Monitoring());
            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnEmptyStreamWhenHttpsIsEmpty() {
            List<Target> result = adapter.extractTargets(new Monitoring(null, Map.of(), null, null));
            assertThat(result).isEmpty();
        }

        @Test
        void shouldCreateHttpTargetsForAllEntries() {
            Map<String, Http> https = new HashMap<>();
            https.put("api1", new Http("https://api.example.com", Duration.ofSeconds(30), false, null, 302, false));
            https.put("api2", new Http("https://api2.example.com", Duration.ofMinutes(1), true, new Certificate(Duration.ofDays(1)), null, false));

            Monitoring monitoring = new Monitoring(null, https, null, null);
            List<Target> result = adapter.extractTargets(monitoring);

            long httpCount = result.stream()
                    .filter(t -> t.type() == MonitoringType.HTTP)
                    .count();
            assertThat(httpCount).isEqualTo(2);
        }
    }

    @Nested
    class ExtractSpeedtestTargetsTests {

        @Test
        void shouldReturnEmptyWhenSpeedtestIsNull() {
            List<Target> result = adapter.extractTargets(new Monitoring(null, null, null, null));
            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnEmptyWhenSpeedtestIsEmpty() {
            List<Target> result = adapter.extractTargets(new Monitoring(null, null, Map.of(), null));
            assertThat(result).isEmpty();
        }

        @Test
        void shouldExtractOoklaSpeedtestTarget() {
            Map<String, Speedtest> speedtests = new HashMap<>();
            speedtests.put("home", new Speedtest("", Duration.ofHours(1), SpeedtestType.OOKLA, 500.0, 200.0));
            Monitoring monitoring = new Monitoring(null, null, speedtests, null);

            List<Target> result = adapter.extractTargets(monitoring);

            assertThat(result).hasSize(1);
            Target t = result.getFirst();
            assertThat(t.type()).isEqualTo(MonitoringType.SPEEDTEST);
            assertThat(t.id().id()).isEqualTo("home:ookla");
            assertThat(t.speedtestType()).isEqualTo(SpeedtestType.OOKLA);
            assertThat(t.warningThresholdMbps()).isEqualTo(500.0);
            assertThat(t.failureThresholdMbps()).isEqualTo(200.0);
        }

        @Test
        void shouldExtractLibreSpeedTarget() {
            Map<String, Speedtest> speedtests = new HashMap<>();
            speedtests.put("internal", new Speedtest("http://speedtest.local", Duration.ofHours(6), SpeedtestType.LIBRESPEED, 800.0, 400.0));
            Monitoring monitoring = new Monitoring(null, null, speedtests, null);

            List<Target> result = adapter.extractTargets(monitoring);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().id().id()).isEqualTo("internal:librespeed");
            assertThat(result.getFirst().target()).isEqualTo("http://speedtest.local");
        }

        @Test
        void shouldGenerateCorrectTargetIdForAllTypes() {
            Map<String, Speedtest> speedtests = new HashMap<>();
            speedtests.put("st1", new Speedtest("", Duration.ofHours(1), SpeedtestType.OOKLA, 500.0, 200.0));
            speedtests.put("st2", new Speedtest("host", Duration.ofHours(1), SpeedtestType.IPERF, 500.0, 200.0));
            speedtests.put("st3", new Speedtest("http://s", Duration.ofHours(1), SpeedtestType.LIBRESPEED, 500.0, 200.0));
            speedtests.put("st4", new Speedtest("http://s", Duration.ofHours(1), SpeedtestType.OPENSPEEDTEST, 500.0, 200.0));
            Monitoring monitoring = new Monitoring(null, null, speedtests, null);

            List<Target> result = adapter.extractTargets(monitoring);

            assertThat(result).extracting(t -> t.id().id())
                    .containsExactlyInAnyOrder("st1:ookla", "st2:iperf", "st3:librespeed", "st4:openspeedtest");
        }
    }

    @Nested
    class ExtractCertificateTargetsTests {

        @Test
        void shouldReturnEmptyStreamWhenHttpsIsNull() {
            List<Target> result = adapter.extractTargets(new Monitoring());
            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnEmptyStreamWhenHttpsIsEmpty() {
            List<Target> result = adapter.extractTargets(new Monitoring(null, Map.of(), null, null));
            assertThat(result).isEmpty();
        }

        @Test
        void shouldIgnoreHttpEntriesWithoutSsl() {
            Map<String, Http> https = new HashMap<>();
            https.put("http1", new Http("https://example.com", Duration.ofSeconds(60), false, null, null, false));

            List<Target> result = adapter.extractTargets(new Monitoring(null, https, null, null));

            assertThat(result).noneMatch(t -> t.type() == MonitoringType.CERTIFICATE);
        }

        @Test
        void shouldCreateCertificateTargetsOnlyForSslEnabled() {
            Certificate cert = new Certificate(Duration.ofDays(7));
            Map<String, Http> https = new HashMap<>();
            https.put("http1", new Http("https://example.com", Duration.ofSeconds(60), false, null, null, false));
            https.put("https1", new Http("https://secure1.com", Duration.ofSeconds(60), true, cert, null, false));
            https.put("https2", new Http("https://secure2.com", Duration.ofSeconds(60), true, cert, null, false));

            List<Target> result = adapter.extractTargets(new Monitoring(null, https, null, null));

            long certCount = result.stream()
                    .filter(t -> t.type() == MonitoringType.CERTIFICATE)
                    .count();
            assertThat(certCount).isEqualTo(2);
        }

        @Test
        void shouldAppendCertificateToTargetId() {
            Certificate cert = new Certificate(Duration.ofDays(30));
            Map<String, Http> https = new HashMap<>();
            https.put("secure-api", new Http("https://secure.com", Duration.ofSeconds(60), true, cert, null, false));

            List<Target> result = adapter.extractTargets(new Monitoring(null, https, null, null));

            Target certTarget = result.stream()
                    .filter(t -> t.type() == MonitoringType.CERTIFICATE)
                    .findFirst()
                    .orElseThrow();
            assertThat(certTarget.id().id()).isEqualTo("secure-api:certificate");
        }

        @Test
        void shouldUseCertificateInterval() {
            Duration certInterval = Duration.ofDays(15);
            Certificate cert = new Certificate(certInterval);
            Map<String, Http> https = new HashMap<>();
            https.put("https1", new Http("https://secure.com", Duration.ofSeconds(60), true, cert, null, false));

            List<Target> result = adapter.extractTargets(new Monitoring(null, https, null, null));

            Target certTarget = result.stream()
                    .filter(t -> t.type() == MonitoringType.CERTIFICATE)
                    .findFirst()
                    .orElseThrow();
            assertThat(certTarget.interval()).isEqualTo(certInterval);
        }
    }
}