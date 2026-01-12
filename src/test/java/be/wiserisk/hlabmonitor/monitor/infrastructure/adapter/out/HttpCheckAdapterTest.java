package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out;

import be.wiserisk.hlabmonitor.monitor.domain.model.Target;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestHeadersUriSpec;
import org.springframework.web.client.RestClient.ResponseSpec;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import java.io.IOException;
import java.net.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;

import static be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringResult.FAILURE;
import static be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringResult.SUCCESS;
import static be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringType.HTTP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HttpCheckAdapterTest {
    public static final String TARGET = "target";
    public static final TargetId TARGET_ID = new TargetId("TargetId");
    public static final String EXCEPTION_MESSAGE = "Exception Message";
    @InjectMocks
    private HttpCheckAdapter httpCheckAdapter;

    @Mock
    private RestClient restClient;

    @Test
    void pingSuccess() throws IOException {
        InetAddress addressMock = mock(InetAddress.class);
        Target target = new Target(TARGET_ID, HTTP, TARGET);
        when(addressMock.isReachable(5000)).thenReturn(true);
        try (MockedStatic<InetAddress> inetAddressMockedStatic = Mockito.mockStatic(InetAddress.class)) {
            inetAddressMockedStatic.when(() -> InetAddress.getByName("target")).thenReturn(addressMock);
            assertThat(httpCheckAdapter.ping(target)).isNotNull().extracting("id", "result", "message").isEqualTo(List.of(TARGET_ID, SUCCESS, ""));
        }
    }

    @Test
    void pingFailure() throws IOException {
        InetAddress addressMock = mock(InetAddress.class);
        Target target = new Target(TARGET_ID, HTTP, TARGET);
        when(addressMock.isReachable(5000)).thenReturn(false);
        try (MockedStatic<InetAddress> inetAddressMockedStatic = Mockito.mockStatic(InetAddress.class)) {
            inetAddressMockedStatic.when(() -> InetAddress.getByName("target")).thenReturn(addressMock);
            assertThat(httpCheckAdapter.ping(target)).isNotNull().extracting("id", "result", "message").isEqualTo(List.of(TARGET_ID, FAILURE, ""));
        }
    }

    @Test
    void pingIOException() throws IOException {
        InetAddress addressMock = mock(InetAddress.class);
        Target target = new Target(TARGET_ID, HTTP, TARGET);
        doThrow(new IOException(EXCEPTION_MESSAGE)).when(addressMock).isReachable(5000);
        try (MockedStatic<InetAddress> inetAddressMockedStatic = Mockito.mockStatic(InetAddress.class)) {
            inetAddressMockedStatic.when(() -> InetAddress.getByName("target")).thenReturn(addressMock);
            assertThat(httpCheckAdapter.ping(target)).isNotNull().extracting("id", "result", "message").isEqualTo(List.of(TARGET_ID, FAILURE, EXCEPTION_MESSAGE));
        }
    }

    @Test
    void pingUnknownHostException() {
        Target target = new Target(TARGET_ID, HTTP, TARGET);
        try (MockedStatic<InetAddress> inetAddressMockedStatic = Mockito.mockStatic(InetAddress.class)) {
            inetAddressMockedStatic.when(() -> InetAddress.getByName("target")).thenThrow(new UnknownHostException());
            assertThat(httpCheckAdapter.ping(target)).isNotNull().extracting("id", "result", "message").isEqualTo(List.of(TARGET_ID, FAILURE, "Unknown host"));
        }
    }

    @Test
    void httpCheckSuccess() {
        Target target = new Target(TARGET_ID, HTTP, TARGET);
        RequestHeadersUriSpec requestHeadersUriSpec = mock(RequestHeadersUriSpec.class);
        ResponseSpec responseSpec = mock(ResponseSpec.class);

        when(restClient.head()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(TARGET)).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        assertThat(httpCheckAdapter.httpCheck(target)).isNotNull().extracting("id", "result", "message").isEqualTo(List.of(TARGET_ID, SUCCESS, ""));
    }

    @Test
    void httpCheckFailure() {
        Target target = new Target(TARGET_ID, HTTP, TARGET);
        RequestHeadersUriSpec requestHeadersUriSpec = mock(RequestHeadersUriSpec.class);
        ResponseSpec responseSpec = mock(ResponseSpec.class);

        when(restClient.head()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(TARGET)).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        assertThat(httpCheckAdapter.httpCheck(target)).isNotNull().extracting("id", "result", "message").isEqualTo(List.of(TARGET_ID, FAILURE, ""));
    }

    @Test
    void certCheckIllegalArgumentException() {
        Target target = new Target(TARGET_ID, HTTP, TARGET);

        try (MockedStatic<URI> uriMockedStatic = Mockito.mockStatic(URI.class)) {
            uriMockedStatic.when(() -> URI.create(TARGET)).thenThrow(new IllegalArgumentException());
            assertThat(httpCheckAdapter.certCheck(target)).isNotNull().extracting("id", "result", "message").isEqualTo(List.of(TARGET_ID, FAILURE, "Malformed URL"));
        }
    }

    @Test
    void certCheckMalformedURLException() {
        Target target = new Target(TARGET_ID, HTTP, TARGET);
        URI uri = mock(URI.class);

        try (MockedStatic<URI> uriMockedStatic = Mockito.mockStatic(URI.class)) {
            uriMockedStatic.when(() -> URI.create(TARGET)).thenReturn(uri);
            when(uri.toURL()).thenThrow(new MalformedURLException());
            assertThat(httpCheckAdapter.certCheck(target)).isNotNull().extracting("id", "result", "message").isEqualTo(List.of(TARGET_ID, FAILURE, "Malformed URL"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void certCheckIOException() {
        Target target = new Target(TARGET_ID, HTTP, TARGET);
        URI uri = mock(URI.class);
        URL url = mock(URL.class);

        try (MockedStatic<URI> uriMockedStatic = Mockito.mockStatic(URI.class)) {
            uriMockedStatic.when(() -> URI.create(TARGET)).thenReturn(uri);
            when(uri.toURL()).thenReturn(url);
            when(url.openConnection()).thenThrow(new IOException("IOException"));
            assertThat(httpCheckAdapter.certCheck(target)).isNotNull().extracting("id", "result", "message").isEqualTo(List.of(TARGET_ID, FAILURE, "IOException"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void certCheckSocketTimeoutException() {
        Target target = new Target(TARGET_ID, HTTP, TARGET);
        URI uri = mock(URI.class);
        URL url = mock(URL.class);
        HttpsURLConnection hsc = mock(HttpsURLConnection.class);

        try (MockedStatic<URI> uriMockedStatic = Mockito.mockStatic(URI.class)) {
            uriMockedStatic.when(() -> URI.create(TARGET)).thenReturn(uri);
            when(uri.toURL()).thenReturn(url);
            when(url.openConnection()).thenReturn(hsc);
            doThrow(new SocketTimeoutException()).when(hsc).connect();
            assertThat(httpCheckAdapter.certCheck(target)).isNotNull().extracting("id", "result", "message").isEqualTo(List.of(TARGET_ID, FAILURE, "Timeout"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void certCheckSSLPeerUnverifiedException() {
        Target target = new Target(TARGET_ID, HTTP, TARGET);
        URI uri = mock(URI.class);
        URL url = mock(URL.class);
        HttpsURLConnection hsc = mock(HttpsURLConnection.class);

        try (MockedStatic<URI> uriMockedStatic = Mockito.mockStatic(URI.class)) {
            uriMockedStatic.when(() -> URI.create(TARGET)).thenReturn(uri);
            when(uri.toURL()).thenReturn(url);
            when(url.openConnection()).thenReturn(hsc);
            doNothing().when(hsc).connect();
            when(hsc.getServerCertificates()).thenThrow(new SSLPeerUnverifiedException(""));
            assertThat(httpCheckAdapter.certCheck(target)).isNotNull().extracting("id", "result", "message").isEqualTo(List.of(TARGET_ID, FAILURE, "Peer unverified"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void certCheckCertificateExpiredException() {
        Target target = new Target(TARGET_ID, HTTP, TARGET);
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
        Target target = new Target(TARGET_ID, HTTP, TARGET);
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
        Target target = new Target(TARGET_ID, HTTP, TARGET);
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
        Target target = new Target(TARGET_ID, HTTP, TARGET);
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
            assertThat(httpCheckAdapter.certCheck(target)).isNotNull().extracting("id", "result", "message").isEqualTo(List.of(TARGET_ID, FAILURE, "No certificate found"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}