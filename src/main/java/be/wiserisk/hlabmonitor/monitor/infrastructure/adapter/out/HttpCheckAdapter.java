package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out;

import be.wiserisk.hlabmonitor.monitor.application.port.out.CheckTargetPort;
import be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringResult;
import be.wiserisk.hlabmonitor.monitor.domain.model.Target;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetResult;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.Common;
import lombok.AllArgsConstructor;
import org.springframework.web.client.RestClient;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import java.io.IOException;
import java.net.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;

import static be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringResult.FAILURE;
import static be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringResult.SUCCESS;

@AllArgsConstructor
public class HttpCheckAdapter implements CheckTargetPort {

    private final RestClient restClient;

    @Override
    public TargetResult ping(Target target) {
        try {
            InetAddress inetAddress = InetAddress.getByName(target.target());
            return new TargetResult(target.id(), getPingResult(inetAddress),"");
        } catch (UnknownHostException e) {
            return new TargetResult(target.id(), FAILURE, "Unknown host");
        } catch (IOException e) {
            return new TargetResult(target.id(), FAILURE, e.getMessage());
        }
    }

    private static MonitoringResult getPingResult(InetAddress inetAddress) throws IOException {
        return inetAddress.isReachable((int) Common.DEFAULT_TIMEOUT.toMillis()) ? SUCCESS : FAILURE;
    }

    @Override
    public TargetResult httpCheck(Target target) {
        return new TargetResult(target.id(), is2xxSuccessful(target) ? SUCCESS : FAILURE, "");
    }

    private boolean is2xxSuccessful(Target target) {
        return restClient.head().uri(target.target()).retrieve().toBodilessEntity().getStatusCode().is2xxSuccessful();
    }

    @Override
    public TargetResult certCheck(Target target) {
        try {
            URL url = URI.create(target.target()).toURL();
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.connect();
            Certificate[] certs = conn.getServerCertificates();
            for (Certificate cert : certs) {
                if (cert instanceof X509Certificate x509Cert) {
                    x509Cert.checkValidity();
                    return new TargetResult(target.id(), SUCCESS, "Valid to " + x509Cert.getNotAfter());
                }
            }
            conn.disconnect();
        } catch (IllegalArgumentException|MalformedURLException e) {
            return new TargetResult(target.id(), FAILURE, "Malformed URL");
        } catch (SSLPeerUnverifiedException e) {
            return new TargetResult(target.id(), FAILURE, "Peer unverified");
        } catch (SocketTimeoutException e) {
            return new TargetResult(target.id(), FAILURE, "Timeout");
        } catch (CertificateNotYetValidException e) {
            return new TargetResult(target.id(), FAILURE, "Certificate not yet valid");
        } catch (CertificateExpiredException e) {
            return new TargetResult(target.id(), FAILURE, "Certificate expired");
        } catch (IOException e) {
            return new TargetResult(target.id(), FAILURE, e.getMessage());
        }
        return new TargetResult(target.id(), FAILURE, "No certificate found");
    }
}
