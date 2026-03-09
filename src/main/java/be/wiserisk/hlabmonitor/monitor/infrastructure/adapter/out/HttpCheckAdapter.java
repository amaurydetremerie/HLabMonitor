package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out;

import be.wiserisk.hlabmonitor.monitor.application.port.out.CheckTargetPort;
import be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringResult;
import be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringType;
import be.wiserisk.hlabmonitor.monitor.domain.model.Target;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetResult;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.monitoring.Common;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import java.io.IOException;
import java.net.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;

import static be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringResult.*;

@AllArgsConstructor
public class HttpCheckAdapter implements CheckTargetPort {

    private final RestClient restClient;
    private final RestClient internalRestClient;

    @Override
    public TargetResult ping(Target target) {
        try {
            InetAddress inetAddress = InetAddress.getByName(target.target());
            return new TargetResult(target.id(), getPingResult(inetAddress),"");
        } catch (UnknownHostException e) {
            return new TargetResult(target.id(), WARNING, "Unknown host");
        } catch (IOException e) {
            return new TargetResult(target.id(), ERROR, e.getMessage());
        }
    }

    private static MonitoringResult getPingResult(InetAddress inetAddress) throws IOException {
        return inetAddress.isReachable((int) Common.DEFAULT_TIMEOUT.toMillis()) ? SUCCESS : FAILURE;
    }

    @Override
    public TargetResult httpCheck(Target target) {
        try {
            HttpStatusCode httpStatusCode = getStatusCode(target);
            if((target.acceptableStatusCode() != null && httpStatusCode.value() == target.acceptableStatusCode())
                    || httpStatusCode.is2xxSuccessful())
                return new TargetResult(target.id(), SUCCESS, "HTTP call success: status=" + httpStatusCode.value());
            if(httpStatusCode.is1xxInformational() || httpStatusCode.is3xxRedirection())
                return new TargetResult(target.id(), WARNING, "HTTP call warning: status=" + httpStatusCode.value());
            if(httpStatusCode.is4xxClientError() || httpStatusCode.is5xxServerError())
                return new TargetResult(target.id(), FAILURE, "HTTP call failed: status=" + httpStatusCode.value());
            return new TargetResult(target.id(), UNKNOWN, "HTTP call unknown: status=" + httpStatusCode.value());
        } catch (ResourceAccessException e) {
            return new TargetResult(target.id(), ERROR, e.getMessage());
        }
    }

    private HttpStatusCode getStatusCode(Target target) {
        RestClient usedRestClient = getTargetRestClient(target.type());
        return usedRestClient.get().uri(target.target()).retrieve().toBodilessEntity().getStatusCode();
    }

    private RestClient getTargetRestClient(MonitoringType type) {
        if (type == MonitoringType.HTTP_INTERNAL)
            return internalRestClient;
        return restClient;
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
            return new TargetResult(target.id(), WARNING, "Malformed URL");
        } catch (SSLPeerUnverifiedException e) {
            return new TargetResult(target.id(), WARNING, "Peer unverified");
        } catch (SocketTimeoutException e) {
            return new TargetResult(target.id(), WARNING, "Timeout");
        } catch (CertificateNotYetValidException e) {
            return new TargetResult(target.id(), FAILURE, "Certificate not yet valid");
        } catch (CertificateExpiredException e) {
            return new TargetResult(target.id(), FAILURE, "Certificate expired");
        } catch (IOException e) {
            return new TargetResult(target.id(), ERROR, e.getMessage());
        }
        return new TargetResult(target.id(), ERROR, "No certificate found");
    }
}
