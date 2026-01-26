package be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml;

import be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringType;
import be.wiserisk.hlabmonitor.monitor.domain.model.Target;
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
            Monitoring monitoring = new Monitoring(pings, null);

            List<Target> result = adapter.extractTargets(monitoring);

            assertThat(result).hasSize(2);
            assertThat(result).extracting(Target::type)
                    .containsOnly(PING);
            assertThat(result).extracting(t -> t.id().id())
                    .containsExactlyInAnyOrder("ping1", "ping2");
        }

        @Test
        void shouldExtractOnlyHttpTargets() {
            Map<String, Http> https = new HashMap<>();
            https.put("http1", new Http("https://example.com", Duration.ofSeconds(60), false, null));
            Monitoring monitoring = new Monitoring(null, https);

            List<Target> result = adapter.extractTargets(monitoring);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).type()).isEqualTo(MonitoringType.HTTP);
            assertThat(result.get(0).id().id()).isEqualTo("http1");
        }

        @Test
        void shouldExtractHttpAndCertificateTargetsWhenSslEnabled() {
            Certificate cert = new Certificate(Duration.ofDays(1));
            Map<String, Http> https = new HashMap<>();
            https.put("https1", new Http("https://secure.com", Duration.ofSeconds(60), true, cert));
            Monitoring monitoring = new Monitoring(null, https);

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
            https.put("http1", new Http("https://example.com", Duration.ofSeconds(60), false, null));
            https.put("https1", new Http("https://secure.com", Duration.ofSeconds(60), true, cert));

            Monitoring monitoring = new Monitoring(pings, https);

            List<Target> result = adapter.extractTargets(monitoring);

            assertThat(result).hasSize(4);
            assertThat(result).extracting(Target::type)
                    .containsExactlyInAnyOrder(
                            PING,
                            MonitoringType.HTTP,
                            MonitoringType.HTTP,
                            MonitoringType.CERTIFICATE
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
            https.put("https1", new Http("https://secure.com", httpInterval, true, cert));

            Monitoring monitoring = new Monitoring(pings, https);

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
            List<Target> result = adapter.extractTargets(new Monitoring(Map.of(), null));
            assertThat(result).isEmpty();
        }

        @Test
        void shouldCreatePingTargetsWithCorrectProperties() {
            Map<String, Ping> pings = new HashMap<>();
            pings.put("server1", new Ping("192.168.1.10", Duration.ofSeconds(15)));
            pings.put("server2", new Ping("10.0.0.1", Duration.ofMinutes(2)));

            List<Target> result = adapter.extractTargets(new Monitoring(pings, null));

            assertThat(result).hasSize(2);
            assertThat(result).allMatch(t -> t.type() == PING);
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
            List<Target> result = adapter.extractTargets(new Monitoring(null, Map.of()));
            assertThat(result).isEmpty();
        }

        @Test
        void shouldCreateHttpTargetsForAllEntries() {
            Map<String, Http> https = new HashMap<>();
            https.put("api1", new Http("https://api.example.com", Duration.ofSeconds(30), false, null));
            https.put("api2", new Http("https://api2.example.com", Duration.ofMinutes(1), true, new Certificate(Duration.ofDays(1))));

            Monitoring monitoring = new Monitoring(null, https);
            List<Target> result = adapter.extractTargets(monitoring);

            long httpCount = result.stream()
                    .filter(t -> t.type() == MonitoringType.HTTP)
                    .count();
            assertThat(httpCount).isEqualTo(2);
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
            List<Target> result = adapter.extractTargets(new Monitoring(null, Map.of()));
            assertThat(result).isEmpty();
        }

        @Test
        void shouldIgnoreHttpEntriesWithoutSsl() {
            Map<String, Http> https = new HashMap<>();
            https.put("http1", new Http("https://example.com", Duration.ofSeconds(60), false, null));

            List<Target> result = adapter.extractTargets(new Monitoring(null, https));

            assertThat(result).noneMatch(t -> t.type() == MonitoringType.CERTIFICATE);
        }

        @Test
        void shouldCreateCertificateTargetsOnlyForSslEnabled() {
            Certificate cert = new Certificate(Duration.ofDays(7));
            Map<String, Http> https = new HashMap<>();
            https.put("http1", new Http("https://example.com", Duration.ofSeconds(60), false, null));
            https.put("https1", new Http("https://secure1.com", Duration.ofSeconds(60), true, cert));
            https.put("https2", new Http("https://secure2.com", Duration.ofSeconds(60), true, cert));

            List<Target> result = adapter.extractTargets(new Monitoring(null, https));

            long certCount = result.stream()
                    .filter(t -> t.type() == MonitoringType.CERTIFICATE)
                    .count();
            assertThat(certCount).isEqualTo(2);
        }

        @Test
        void shouldAppendCertificateToTargetId() {
            Certificate cert = new Certificate(Duration.ofDays(30));
            Map<String, Http> https = new HashMap<>();
            https.put("secure-api", new Http("https://secure.com", Duration.ofSeconds(60), true, cert));

            List<Target> result = adapter.extractTargets(new Monitoring(null, https));

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
            https.put("https1", new Http("https://secure.com", Duration.ofSeconds(60), true, cert));

            List<Target> result = adapter.extractTargets(new Monitoring(null, https));

            Target certTarget = result.stream()
                    .filter(t -> t.type() == MonitoringType.CERTIFICATE)
                    .findFirst()
                    .orElseThrow();
            assertThat(certTarget.interval()).isEqualTo(certInterval);
        }
    }
}