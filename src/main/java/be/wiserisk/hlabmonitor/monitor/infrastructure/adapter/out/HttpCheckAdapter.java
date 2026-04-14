package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out;

import be.wiserisk.hlabmonitor.monitor.application.port.out.CheckTargetPort;
import be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringResult;
import be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringType;
import be.wiserisk.hlabmonitor.monitor.domain.model.Target;
import be.wiserisk.hlabmonitor.monitor.domain.model.TargetResult;
import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.monitoring.Common;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static be.wiserisk.hlabmonitor.monitor.domain.enums.MonitoringResult.*;

@AllArgsConstructor
public class HttpCheckAdapter implements CheckTargetPort {

    public static final String MBPS = " Mbps";
    private final RestClient restClient;
    private final RestClient internalRestClient;
    private final ObjectMapper objectMapper;

    private record ProcessOutput(int exitCode, String stdout, String stderr) {}

    @Override
    public TargetResult ping(Target target) {
        try {
            InetAddress inetAddress = InetAddress.getByName(target.target());
            return new TargetResult(target.id(), getPingResult(inetAddress), "");
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
            if ((target.acceptableStatusCode() != null && httpStatusCode.value() == target.acceptableStatusCode())
                    || httpStatusCode.is2xxSuccessful())
                return new TargetResult(target.id(), SUCCESS, "HTTP call success: status=" + httpStatusCode.value());
            if (httpStatusCode.is1xxInformational() || httpStatusCode.is3xxRedirection())
                return new TargetResult(target.id(), WARNING, "HTTP call warning: status=" + httpStatusCode.value());
            if (httpStatusCode.is4xxClientError() || httpStatusCode.is5xxServerError())
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
        } catch (IllegalArgumentException | MalformedURLException e) {
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

    @Override
    public TargetResult speedtestCheck(Target target) {
        try {
            return switch (target.speedtestType()) {
                case OOKLA -> doOoklaSpeedtest(target);
                case LIBRESPEED -> doHttpDownloadTest(target, target.target() + "/garbage?ckSize=20");
                case OPENSPEEDTEST -> doHttpDownloadTest(target, target.target() + "/download");
                case IPERF -> doIperfSpeedtest(target);
            };
        } catch (Exception e) {
            return new TargetResult(target.id(), ERROR, e.getMessage());
        }
    }

    private TargetResult doOoklaSpeedtest(Target target) throws IOException, InterruptedException, TimeoutException {
        List<String> command = new ArrayList<>(List.of("speedtest", "--accept-license", "--accept-gdpr", "-f", "json"));
        if (target.target() != null && !target.target().isBlank()) {
            command.add("--server-id");
            command.add(target.target());
        }

        ProcessOutput output;
        try {
            output = runProcess(command, 60);
        } catch (IOException e) {
            if (e.getMessage() != null && e.getMessage().contains("No such file")) {
                return new TargetResult(target.id(), ERROR, "speedtest CLI not installed. Please install the Ookla speedtest CLI (https://www.speedtest.net/apps/cli).");
            }
            throw e;
        }

        if (output.exitCode() != 0) {
            String err = output.stderr().isBlank() ? output.stdout() : output.stderr();
            return new TargetResult(target.id(), ERROR, "Ookla speedtest failed: " + err);
        }

        JsonNode root = objectMapper.readTree(output.stdout());
        double bandwidthBytesPerSec = root.path("download").path("bandwidth").asDouble();
        double speedMbps = (bandwidthBytesPerSec * 8.0) / 1_000_000.0;

        return checkSpeedThresholds(target, speedMbps);
    }

    private TargetResult doHttpDownloadTest(Target target, String url) {
        try {
            long startTime = System.nanoTime();
            byte[] data = restClient.get().uri(url).retrieve().body(byte[].class);
            long endTime = System.nanoTime();

            if (data == null || data.length == 0) {
                return new TargetResult(target.id(), ERROR, "No data received from speedtest server.");
            }

            double elapsedSeconds = (endTime - startTime) / 1_000_000_000.0;
            double speedMbps = (data.length * 8.0) / 1_000_000.0 / elapsedSeconds;

            return checkSpeedThresholds(target, speedMbps);
        } catch (ResourceAccessException e) {
            return new TargetResult(target.id(), ERROR, "Speedtest server unreachable: " + e.getMessage());
        } catch (Exception e) {
            return new TargetResult(target.id(), ERROR, "HTTP download test failed: " + e.getMessage());
        }
    }

    private TargetResult doIperfSpeedtest(Target target) throws IOException, InterruptedException, TimeoutException {
        List<String> command = List.of("iperf3", "-c", target.target(), "-J", "-t", "10");

        ProcessOutput output;
        try {
            output = runProcess(command, 60);
        } catch (IOException e) {
            if (e.getMessage() != null && e.getMessage().contains("No such file")) {
                return new TargetResult(target.id(), ERROR, "iperf3 not installed. Please install iperf3.");
            }
            throw e;
        }

        if (output.exitCode() != 0) {
            String err = output.stderr().isBlank() ? output.stdout() : output.stderr();
            return new TargetResult(target.id(), ERROR, "iperf3 failed: " + err);
        }

        JsonNode root = objectMapper.readTree(output.stdout());
        double bitsPerSecond = root.path("end").path("sum_received").path("bits_per_second").asDouble();
        double speedMbps = bitsPerSecond / 1_000_000.0;

        return checkSpeedThresholds(target, speedMbps);
    }

    private TargetResult checkSpeedThresholds(Target target, double speedMbps) {
        String speedStr = String.format("%.2f", speedMbps);
        if (speedMbps < target.failureThresholdMbps()) {
            return new TargetResult(target.id(), FAILURE,
                "Download speed " + speedStr + " Mbps below failure threshold " + target.failureThresholdMbps() + MBPS);
        }
        if (speedMbps < target.warningThresholdMbps()) {
            return new TargetResult(target.id(), WARNING,
                "Download speed " + speedStr + " Mbps below warning threshold " + target.warningThresholdMbps() + MBPS);
        }
        return new TargetResult(target.id(), SUCCESS, "Download speed: " + speedStr + MBPS);
    }

    /**
     * Runs an external process and captures stdout/stderr concurrently to avoid pipe buffer deadlocks.
     */
    private ProcessOutput runProcess(List<String> command, long timeoutSeconds) throws IOException, InterruptedException, TimeoutException {
        ProcessBuilder pb = new ProcessBuilder(command);
        Process process = pb.start();

        StringBuilder stdout = new StringBuilder();
        StringBuilder stderr = new StringBuilder();

        Thread stdoutThread = Thread.ofVirtual().start(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stdout.append(line).append('\n');
                }
            } catch (IOException ignored) {/*Ignored*/}
        });

        Thread stderrThread = Thread.ofVirtual().start(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stderr.append(line).append('\n');
                }
            } catch (IOException ignored) {/*Ignored*/}
        });

        if (!process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            throw new TimeoutException("Process timed out after " + timeoutSeconds + " seconds: " + command.getFirst());
        }

        stdoutThread.join(5000);
        stderrThread.join(5000);

        return new ProcessOutput(process.exitValue(), stdout.toString().trim(), stderr.toString().trim());
    }
}
