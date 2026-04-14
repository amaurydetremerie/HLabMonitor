package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out;

import be.wiserisk.hlabmonitor.monitor.domain.model.Target;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestHeadersUriSpec;
import org.springframework.web.client.RestClient.ResponseSpec;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import be.wiserisk.hlabmonitor.monitor.domain.enums.SpeedtestType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringResult.*;
import static be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringType.HTTP;
import static be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringType.HTTP_INTERNAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HttpCheckAdapterTest {
    public static final String TARGET = "target";
    public static final TargetId TARGET_ID = new TargetId("TargetId");
    public static final String EXCEPTION_MESSAGE = "Exception Message";

    private HttpCheckAdapter httpCheckAdapter;

    @Mock
    private RestClient restClient;
    @Mock
    private RestClient internalRestClient;
    @Mock
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        httpCheckAdapter = new HttpCheckAdapter(restClient, internalRestClient, objectMapper);
    }

    @Test
    void pingSuccess() throws IOException {
        InetAddress addressMock = mock(InetAddress.class);
        Target target = new Target(TARGET_ID, HTTP, TARGET, Duration.ofMinutes(1));
        when(addressMock.isReachable(5000)).thenReturn(true);
        try (MockedStatic<InetAddress> inetAddressMockedStatic = Mockito.mockStatic(InetAddress.class)) {
            inetAddressMockedStatic.when(() -> InetAddress.getByName("target")).thenReturn(addressMock);
            assertThat(httpCheckAdapter.ping(target)).isNotNull().extracting("id", "result", "message").isEqualTo(List.of(TARGET_ID, SUCCESS, ""));
        }
    }

    @Test
    void pingFailure() throws IOException {
        InetAddress addressMock = mock(InetAddress.class);
        Target target = new Target(TARGET_ID, HTTP, TARGET, Duration.ofMinutes(1));
        when(addressMock.isReachable(5000)).thenReturn(false);
        try (MockedStatic<InetAddress> inetAddressMockedStatic = Mockito.mockStatic(InetAddress.class)) {
            inetAddressMockedStatic.when(() -> InetAddress.getByName("target")).thenReturn(addressMock);
            assertThat(httpCheckAdapter.ping(target)).isNotNull().extracting("id", "result", "message").isEqualTo(List.of(TARGET_ID, FAILURE, ""));
        }
    }

    @Test
    void pingIOException() throws IOException {
        InetAddress addressMock = mock(InetAddress.class);
        Target target = new Target(TARGET_ID, HTTP, TARGET, Duration.ofMinutes(1));
        doThrow(new IOException(EXCEPTION_MESSAGE)).when(addressMock).isReachable(5000);
        try (MockedStatic<InetAddress> inetAddressMockedStatic = Mockito.mockStatic(InetAddress.class)) {
            inetAddressMockedStatic.when(() -> InetAddress.getByName("target")).thenReturn(addressMock);
            assertThat(httpCheckAdapter.ping(target)).isNotNull().extracting("id", "result", "message").isEqualTo(List.of(TARGET_ID, ERROR, EXCEPTION_MESSAGE));
        }
    }

    @Test
    void pingUnknownHostException() {
        Target target = new Target(TARGET_ID, HTTP, TARGET, Duration.ofMinutes(1));
        try (MockedStatic<InetAddress> inetAddressMockedStatic = Mockito.mockStatic(InetAddress.class)) {
            inetAddressMockedStatic.when(() -> InetAddress.getByName("target")).thenThrow(new UnknownHostException());
            assertThat(httpCheckAdapter.ping(target)).isNotNull().extracting("id", "result", "message").isEqualTo(List.of(TARGET_ID, WARNING, "Unknown host"));
        }
    }

    @Test
    void httpCheckInternal() {
        Target target = new Target(TARGET_ID, HTTP_INTERNAL, TARGET, Duration.ofMinutes(1));
        RequestHeadersUriSpec requestHeadersUriSpec = mock(RequestHeadersUriSpec.class);
        ResponseSpec responseSpec = mock(ResponseSpec.class);

        when(internalRestClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(TARGET)).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        assertThat(httpCheckAdapter.httpCheck(target)).isNotNull().extracting("id", "result", "message").isEqualTo(List.of(TARGET_ID, SUCCESS, "HTTP call success: status=200"));
    }

    @Test
    void httpCheckSuccess() {
        Target target = new Target(TARGET_ID, HTTP, TARGET, Duration.ofMinutes(1));
        RequestHeadersUriSpec requestHeadersUriSpec = mock(RequestHeadersUriSpec.class);
        ResponseSpec responseSpec = mock(ResponseSpec.class);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(TARGET)).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        assertThat(httpCheckAdapter.httpCheck(target)).isNotNull().extracting("id", "result", "message").isEqualTo(List.of(TARGET_ID, SUCCESS, "HTTP call success: status=200"));
    }

    @Test
    void httpCheckSuccessCustomCode() {
        Target target = new Target(TARGET_ID, HTTP, TARGET, Duration.ofMinutes(1), 302);
        RequestHeadersUriSpec requestHeadersUriSpec = mock(RequestHeadersUriSpec.class);
        ResponseSpec responseSpec = mock(ResponseSpec.class);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(TARGET)).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(new ResponseEntity<>(HttpStatus.FOUND));

        assertThat(httpCheckAdapter.httpCheck(target)).isNotNull().extracting("id", "result", "message").isEqualTo(List.of(TARGET_ID, SUCCESS, "HTTP call success: status=302"));
    }

    @Test
    void httpCheck1xx() {
        Target target = new Target(TARGET_ID, HTTP, TARGET, Duration.ofMinutes(1));
        RequestHeadersUriSpec requestHeadersUriSpec = mock(RequestHeadersUriSpec.class);
        ResponseSpec responseSpec = mock(ResponseSpec.class);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(TARGET)).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(new ResponseEntity<>(HttpStatus.CONTINUE));

        assertThat(httpCheckAdapter.httpCheck(target)).isNotNull().extracting("id", "result", "message").isEqualTo(List.of(TARGET_ID, WARNING, "HTTP call warning: status=100"));
    }

    @Test
    void httpCheck3xx() {
        Target target = new Target(TARGET_ID, HTTP, TARGET, Duration.ofMinutes(1));
        RequestHeadersUriSpec requestHeadersUriSpec = mock(RequestHeadersUriSpec.class);
        ResponseSpec responseSpec = mock(ResponseSpec.class);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(TARGET)).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(new ResponseEntity<>(HttpStatus.MULTIPLE_CHOICES));

        assertThat(httpCheckAdapter.httpCheck(target)).isNotNull().extracting("id", "result", "message").isEqualTo(List.of(TARGET_ID, WARNING, "HTTP call warning: status=300"));
    }

    @Test
    void httpCheck4xx() {
        Target target = new Target(TARGET_ID, HTTP, TARGET, Duration.ofMinutes(1));
        RequestHeadersUriSpec requestHeadersUriSpec = mock(RequestHeadersUriSpec.class);
        ResponseSpec responseSpec = mock(ResponseSpec.class);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(TARGET)).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        assertThat(httpCheckAdapter.httpCheck(target)).isNotNull().extracting("id", "result", "message").isEqualTo(List.of(TARGET_ID, FAILURE, "HTTP call failed: status=400"));
    }

    @Test
    void httpCheck5xx() {
        Target target = new Target(TARGET_ID, HTTP, TARGET, Duration.ofMinutes(1));
        RequestHeadersUriSpec requestHeadersUriSpec = mock(RequestHeadersUriSpec.class);
        ResponseSpec responseSpec = mock(ResponseSpec.class);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(TARGET)).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThat(httpCheckAdapter.httpCheck(target)).isNotNull().extracting("id", "result", "message").isEqualTo(List.of(TARGET_ID, FAILURE, "HTTP call failed: status=500"));
    }

    @Test
    void httpCheckUnknown() {
        Target target = new Target(TARGET_ID, HTTP, TARGET, Duration.ofMinutes(1));
        RequestHeadersUriSpec requestHeadersUriSpec = mock(RequestHeadersUriSpec.class);
        ResponseSpec responseSpec = mock(ResponseSpec.class);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(TARGET)).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        ResponseEntity responseEntity = mock(ResponseEntity.class);
        when(responseSpec.toBodilessEntity()).thenReturn(responseEntity);
        HttpStatusCode httpStatusCode = mock(HttpStatus.class);
        when(responseEntity.getStatusCode()).thenReturn(httpStatusCode);
        when(httpStatusCode.value()).thenReturn(900);
        when(httpStatusCode.is1xxInformational()).thenReturn(false);
        when(httpStatusCode.is2xxSuccessful()).thenReturn(false);
        when(httpStatusCode.is3xxRedirection()).thenReturn(false);
        when(httpStatusCode.is4xxClientError()).thenReturn(false);
        when(httpStatusCode.is5xxServerError()).thenReturn(false);

        assertThat(httpCheckAdapter.httpCheck(target)).isNotNull().extracting("id", "result", "message").isEqualTo(List.of(TARGET_ID, UNKNOWN, "HTTP call unknown: status=900"));
    }

    @Test
    void httpCheckFailure() {
        Target target = new Target(TARGET_ID, HTTP, TARGET, Duration.ofMinutes(1));
        RequestHeadersUriSpec requestHeadersUriSpec = mock(RequestHeadersUriSpec.class);
        ResponseSpec responseSpec = mock(ResponseSpec.class);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(TARGET)).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        assertThat(httpCheckAdapter.httpCheck(target)).isNotNull().extracting("id", "result", "message").isEqualTo(List.of(TARGET_ID, FAILURE, "HTTP call failed: status=404"));
    }

    @Test
    void httpCheckFailureResourceAccessException() {
        Target target = new Target(TARGET_ID, HTTP, TARGET, Duration.ofMinutes(1));
        RequestHeadersUriSpec requestHeadersUriSpec = mock(RequestHeadersUriSpec.class);
        ResponseSpec responseSpec = mock(ResponseSpec.class);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(TARGET)).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenThrow(new ResourceAccessException("exception"));

        assertThat(httpCheckAdapter.httpCheck(target)).isNotNull().extracting("id", "result", "message").isEqualTo(List.of(TARGET_ID, ERROR, "exception"));
    }

    @Test
    void certCheckIllegalArgumentException() {
        Target target = new Target(TARGET_ID, HTTP, TARGET, Duration.ofMinutes(1));

        try (MockedStatic<URI> uriMockedStatic = Mockito.mockStatic(URI.class)) {
            uriMockedStatic.when(() -> URI.create(TARGET)).thenThrow(new IllegalArgumentException());
            assertThat(httpCheckAdapter.certCheck(target)).isNotNull().extracting("id", "result", "message").isEqualTo(List.of(TARGET_ID, WARNING, "Malformed URL"));
        }
    }

    @Test
    void certCheckMalformedURLException() {
        Target target = new Target(TARGET_ID, HTTP, TARGET, Duration.ofMinutes(1));
        URI uri = mock(URI.class);

        try (MockedStatic<URI> uriMockedStatic = Mockito.mockStatic(URI.class)) {
            uriMockedStatic.when(() -> URI.create(TARGET)).thenReturn(uri);
            when(uri.toURL()).thenThrow(new MalformedURLException());
            assertThat(httpCheckAdapter.certCheck(target)).isNotNull().extracting("id", "result", "message").isEqualTo(List.of(TARGET_ID, WARNING, "Malformed URL"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void certCheckIOException() {
        Target target = new Target(TARGET_ID, HTTP, TARGET, Duration.ofMinutes(1));
        URI uri = mock(URI.class);
        URL url = mock(URL.class);

        try (MockedStatic<URI> uriMockedStatic = Mockito.mockStatic(URI.class)) {
            uriMockedStatic.when(() -> URI.create(TARGET)).thenReturn(uri);
            when(uri.toURL()).thenReturn(url);
            when(url.openConnection()).thenThrow(new IOException("IOException"));
            assertThat(httpCheckAdapter.certCheck(target)).isNotNull().extracting("id", "result", "message").isEqualTo(List.of(TARGET_ID, ERROR, "IOException"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void certCheckSocketTimeoutException() {
        Target target = new Target(TARGET_ID, HTTP, TARGET, Duration.ofMinutes(1));
        URI uri = mock(URI.class);
        URL url = mock(URL.class);
        HttpsURLConnection hsc = mock(HttpsURLConnection.class);

        try (MockedStatic<URI> uriMockedStatic = Mockito.mockStatic(URI.class)) {
            uriMockedStatic.when(() -> URI.create(TARGET)).thenReturn(uri);
            when(uri.toURL()).thenReturn(url);
            when(url.openConnection()).thenReturn(hsc);
            doThrow(new SocketTimeoutException()).when(hsc).connect();
            assertThat(httpCheckAdapter.certCheck(target)).isNotNull().extracting("id", "result", "message").isEqualTo(List.of(TARGET_ID, WARNING, "Timeout"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void certCheckSSLPeerUnverifiedException() {
        Target target = new Target(TARGET_ID, HTTP, TARGET, Duration.ofMinutes(1));
        URI uri = mock(URI.class);
        URL url = mock(URL.class);
        HttpsURLConnection hsc = mock(HttpsURLConnection.class);

        try (MockedStatic<URI> uriMockedStatic = Mockito.mockStatic(URI.class)) {
            uriMockedStatic.when(() -> URI.create(TARGET)).thenReturn(uri);
            when(uri.toURL()).thenReturn(url);
            when(url.openConnection()).thenReturn(hsc);
            doNothing().when(hsc).connect();
            when(hsc.getServerCertificates()).thenThrow(new SSLPeerUnverifiedException(""));
            assertThat(httpCheckAdapter.certCheck(target)).isNotNull().extracting("id", "result", "message").isEqualTo(List.of(TARGET_ID, WARNING, "Peer unverified"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void certCheckCertificateExpiredException() {
        Target target = new Target(TARGET_ID, HTTP, TARGET, Duration.ofMinutes(1));
        URI uri = mock(URI.class);
        URL url = mock(URL.class);
        HttpsURLConnection hsc = mock(HttpsURLConnection.class);
        Certificate[] certs = new Certificate[1];
        X509Certificate x509Cert = mock(X509Certificate.class);
        certs[0] = x509Cert;

        try (MockedStatic<URI> uriMockedStatic = Mockito.mockStatic(URI.class)) {
            uriMockedStatic.when(() -> URI.create(TARGET)).thenReturn(uri);
            when(uri.toURL()).thenReturn(url);
            when(url.openConnection()).thenReturn(hsc);
            doNothing().when(hsc).connect();
            when(hsc.getServerCertificates()).thenReturn(certs);
            doThrow(new CertificateExpiredException()).when(x509Cert).checkValidity();
            assertThat(httpCheckAdapter.certCheck(target)).isNotNull().extracting("id", "result", "message").isEqualTo(List.of(TARGET_ID, FAILURE, "Certificate expired"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void certCheckCertificateNotYetValidException() {
        Target target = new Target(TARGET_ID, HTTP, TARGET, Duration.ofMinutes(1));
        URI uri = mock(URI.class);
        URL url = mock(URL.class);
        HttpsURLConnection hsc = mock(HttpsURLConnection.class);
        Certificate[] certs = new Certificate[1];
        X509Certificate x509Cert = mock(X509Certificate.class);
        certs[0] = x509Cert;

        try (MockedStatic<URI> uriMockedStatic = Mockito.mockStatic(URI.class)) {
            uriMockedStatic.when(() -> URI.create(TARGET)).thenReturn(uri);
            when(uri.toURL()).thenReturn(url);
            when(url.openConnection()).thenReturn(hsc);
            doNothing().when(hsc).connect();
            when(hsc.getServerCertificates()).thenReturn(certs);
            doThrow(new CertificateNotYetValidException()).when(x509Cert).checkValidity();
            assertThat(httpCheckAdapter.certCheck(target)).isNotNull().extracting("id", "result", "message").isEqualTo(List.of(TARGET_ID, FAILURE, "Certificate not yet valid"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void certCheck() {
        Target target = new Target(TARGET_ID, HTTP, TARGET, Duration.ofMinutes(1));
        URI uri = mock(URI.class);
        URL url = mock(URL.class);
        HttpsURLConnection hsc = mock(HttpsURLConnection.class);
        Certificate[] certs = new Certificate[1];
        X509Certificate x509Cert = mock(X509Certificate.class);
        certs[0] = x509Cert;
        Date validityDate = new Date();

        try (MockedStatic<URI> uriMockedStatic = Mockito.mockStatic(URI.class)) {
            uriMockedStatic.when(() -> URI.create(TARGET)).thenReturn(uri);
            when(uri.toURL()).thenReturn(url);
            when(url.openConnection()).thenReturn(hsc);
            doNothing().when(hsc).connect();
            when(hsc.getServerCertificates()).thenReturn(certs);
            doNothing().when(x509Cert).checkValidity();
            when(x509Cert.getNotAfter()).thenReturn(validityDate);
            assertThat(httpCheckAdapter.certCheck(target)).isNotNull().extracting("id", "result", "message").isEqualTo(List.of(TARGET_ID, SUCCESS, "Valid to " + validityDate));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void certCheckNoCertificate() {
        Target target = new Target(TARGET_ID, HTTP, TARGET, Duration.ofMinutes(1));
        URI uri = mock(URI.class);
        URL url = mock(URL.class);
        HttpsURLConnection hsc = mock(HttpsURLConnection.class);
        Certificate[] certs = new Certificate[0];


        try (MockedStatic<URI> uriMockedStatic = Mockito.mockStatic(URI.class)) {
            uriMockedStatic.when(() -> URI.create(TARGET)).thenReturn(uri);
            when(uri.toURL()).thenReturn(url);
            when(url.openConnection()).thenReturn(hsc);
            doNothing().when(hsc).connect();
            when(hsc.getServerCertificates()).thenReturn(certs);
            assertThat(httpCheckAdapter.certCheck(target)).isNotNull().extracting("id", "result", "message").isEqualTo(List.of(TARGET_ID, ERROR, "No certificate found"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void certCheckNoX509Cert() {
        Target target = new Target(TARGET_ID, HTTP, TARGET, Duration.ofMinutes(1));
        URI uri = mock(URI.class);
        URL url = mock(URL.class);
        HttpsURLConnection hsc = mock(HttpsURLConnection.class);
        Certificate[] certs = new Certificate[1];
        certs[0] = mock(Certificate.class);


        try (MockedStatic<URI> uriMockedStatic = Mockito.mockStatic(URI.class)) {
            uriMockedStatic.when(() -> URI.create(TARGET)).thenReturn(uri);
            when(uri.toURL()).thenReturn(url);
            when(url.openConnection()).thenReturn(hsc);
            doNothing().when(hsc).connect();
            when(hsc.getServerCertificates()).thenReturn(certs);
            assertThat(httpCheckAdapter.certCheck(target)).isNotNull().extracting("id", "result", "message").isEqualTo(List.of(TARGET_ID, ERROR, "No certificate found"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Nested
    class SpeedtestTests {

        private static final double WARNING_MBPS = 500.0;
        private static final double FAILURE_MBPS = 200.0;
        private HttpCheckAdapter speedtestAdapter;

        @BeforeEach
        void setup() {
            speedtestAdapter = new HttpCheckAdapter(restClient, internalRestClient, new ObjectMapper());
        }

        private Target speedtestTarget(SpeedtestType type, String target) {
            return new Target(TARGET_ID, target, Duration.ofHours(1), type, WARNING_MBPS, FAILURE_MBPS);
        }

        private Process mockProcess(String stdout, String stderr, int exitCode) throws Exception {
            Process process = mock(Process.class);
            when(process.getInputStream()).thenReturn(new ByteArrayInputStream(stdout.getBytes(StandardCharsets.UTF_8)));
            when(process.getErrorStream()).thenReturn(new ByteArrayInputStream(stderr.getBytes(StandardCharsets.UTF_8)));
            when(process.waitFor(60L, TimeUnit.SECONDS)).thenReturn(true);
            when(process.exitValue()).thenReturn(exitCode);
            return process;
        }

        // ── Ookla ──────────────────────────────────────────────────────────────

        @Test
        void ooklaSuccess() throws Exception {
            // 600 Mbps = 75_000_000 bytes/s → above warning threshold
            Process process = mockProcess("{\"download\":{\"bandwidth\":75000000}}", "", 0);
            try (MockedConstruction<ProcessBuilder> ignored = mockConstruction(ProcessBuilder.class,
                    (pb, ctx) -> when(pb.start()).thenReturn(process))) {
                assertThat(speedtestAdapter.speedtestCheck(speedtestTarget(SpeedtestType.OOKLA, "")))
                        .extracting("id", "result").isEqualTo(List.of(TARGET_ID, SUCCESS));
            }
        }

        @Test
        void ooklaWarning() throws Exception {
            // 350 Mbps = 43_750_000 bytes/s → between failure and warning
            Process process = mockProcess("{\"download\":{\"bandwidth\":43750000}}", "", 0);
            try (MockedConstruction<ProcessBuilder> ignored = mockConstruction(ProcessBuilder.class,
                    (pb, ctx) -> when(pb.start()).thenReturn(process))) {
                assertThat(speedtestAdapter.speedtestCheck(speedtestTarget(SpeedtestType.OOKLA, "")))
                        .extracting("id", "result").isEqualTo(List.of(TARGET_ID, WARNING));
            }
        }

        @Test
        void ooklaFailure() throws Exception {
            // 100 Mbps = 12_500_000 bytes/s → below failure threshold
            Process process = mockProcess("{\"download\":{\"bandwidth\":12500000}}", "", 0);
            try (MockedConstruction<ProcessBuilder> ignored = mockConstruction(ProcessBuilder.class,
                    (pb, ctx) -> when(pb.start()).thenReturn(process))) {
                assertThat(speedtestAdapter.speedtestCheck(speedtestTarget(SpeedtestType.OOKLA, "")))
                        .extracting("id", "result").isEqualTo(List.of(TARGET_ID, FAILURE));
            }
        }

        @Test
        void ooklaNotInstalled() throws Exception {
            try (MockedConstruction<ProcessBuilder> ignored = mockConstruction(ProcessBuilder.class,
                    (pb, ctx) -> when(pb.start()).thenThrow(new IOException("No such file or directory")))) {
                assertThat(speedtestAdapter.speedtestCheck(speedtestTarget(SpeedtestType.OOKLA, "")))
                        .extracting("result").isEqualTo(ERROR);
            }
        }

        @Test
        void ooklaProcessError() throws Exception {
            Process process = mockProcess("", "Connection error", 1);
            try (MockedConstruction<ProcessBuilder> ignored = mockConstruction(ProcessBuilder.class,
                    (pb, ctx) -> when(pb.start()).thenReturn(process))) {
                assertThat(speedtestAdapter.speedtestCheck(speedtestTarget(SpeedtestType.OOKLA, "")))
                        .extracting("result").isEqualTo(ERROR);
            }
        }

        @Test
        @SuppressWarnings("unchecked")
        void ooklaWithServerId() throws Exception {
            Process process = mockProcess("{\"download\":{\"bandwidth\":75000000}}", "", 0);
            List<String> capturedCommand = new ArrayList<>();
            try (MockedConstruction<ProcessBuilder> ignored = mockConstruction(ProcessBuilder.class,
                    (pb, ctx) -> {
                        capturedCommand.addAll((List<String>) ctx.arguments().getFirst());
                        when(pb.start()).thenReturn(process);
                    })) {
                speedtestAdapter.speedtestCheck(speedtestTarget(SpeedtestType.OOKLA, "12345"));
                assertThat(capturedCommand).contains("--server-id", "12345");
            }
        }

        // ── iperf ──────────────────────────────────────────────────────────────

        @Test
        void iperfSuccess() throws Exception {
            // 600 Mbps = 600_000_000 bits/s
            Process process = mockProcess("{\"end\":{\"sum_received\":{\"bits_per_second\":600000000}}}", "", 0);
            try (MockedConstruction<ProcessBuilder> ignored = mockConstruction(ProcessBuilder.class,
                    (pb, ctx) -> when(pb.start()).thenReturn(process))) {
                assertThat(speedtestAdapter.speedtestCheck(speedtestTarget(SpeedtestType.IPERF, "iperf.host")))
                        .extracting("id", "result").isEqualTo(List.of(TARGET_ID, SUCCESS));
            }
        }

        @Test
        void iperfNotInstalled() throws Exception {
            try (MockedConstruction<ProcessBuilder> ignored = mockConstruction(ProcessBuilder.class,
                    (pb, ctx) -> when(pb.start()).thenThrow(new IOException("No such file or directory")))) {
                assertThat(speedtestAdapter.speedtestCheck(speedtestTarget(SpeedtestType.IPERF, "iperf.host")))
                        .extracting("result").isEqualTo(ERROR);
            }
        }

        @Test
        void iperfProcessError() throws Exception {
            Process process = mockProcess("", "connect failed", 1);
            try (MockedConstruction<ProcessBuilder> ignored = mockConstruction(ProcessBuilder.class,
                    (pb, ctx) -> when(pb.start()).thenReturn(process))) {
                assertThat(speedtestAdapter.speedtestCheck(speedtestTarget(SpeedtestType.IPERF, "iperf.host")))
                        .extracting("result").isEqualTo(ERROR);
            }
        }

        // ── LibreSpeed ────────────────────────────────────────────────────────

        @Test
        void libreSpeedSuccess() {
            byte[] data = new byte[20 * 1024 * 1024];
            RequestHeadersUriSpec uriSpec = mock(RequestHeadersUriSpec.class);
            ResponseSpec responseSpec = mock(ResponseSpec.class);
            when(restClient.get()).thenReturn(uriSpec);
            when(uriSpec.uri(anyString())).thenReturn(uriSpec);
            when(uriSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.body(byte[].class)).thenReturn(data);

            assertThat(speedtestAdapter.speedtestCheck(speedtestTarget(SpeedtestType.LIBRESPEED, "http://speedtest.local")))
                    .extracting("id").isEqualTo(TARGET_ID);
        }

        @Test
        void libreSpeedUnreachable() {
            RequestHeadersUriSpec uriSpec = mock(RequestHeadersUriSpec.class);
            ResponseSpec responseSpec = mock(ResponseSpec.class);
            when(restClient.get()).thenReturn(uriSpec);
            when(uriSpec.uri(anyString())).thenReturn(uriSpec);
            when(uriSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.body(byte[].class)).thenThrow(new ResourceAccessException("Connection refused"));

            assertThat(speedtestAdapter.speedtestCheck(speedtestTarget(SpeedtestType.LIBRESPEED, "http://speedtest.local")))
                    .extracting("result").isEqualTo(ERROR);
        }

        // ── OpenSpeedTest ─────────────────────────────────────────────────────

        @Test
        void openSpeedTestSuccess() {
            byte[] data = new byte[20 * 1024 * 1024];
            RequestHeadersUriSpec uriSpec = mock(RequestHeadersUriSpec.class);
            ResponseSpec responseSpec = mock(ResponseSpec.class);
            when(restClient.get()).thenReturn(uriSpec);
            when(uriSpec.uri(anyString())).thenReturn(uriSpec);
            when(uriSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.body(byte[].class)).thenReturn(data);

            assertThat(speedtestAdapter.speedtestCheck(speedtestTarget(SpeedtestType.OPENSPEEDTEST, "http://openspeedtest.local")))
                    .extracting("id").isEqualTo(TARGET_ID);
        }
    }

}