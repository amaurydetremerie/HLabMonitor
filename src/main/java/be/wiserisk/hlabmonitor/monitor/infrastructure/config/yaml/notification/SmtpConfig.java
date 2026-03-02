package be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.notification;

import org.springframework.boot.context.properties.bind.ConstructorBinding;

public record SmtpConfig(String host, int port, String username, String password, boolean auth, boolean tls) {

    @ConstructorBinding
    public SmtpConfig(String host, Integer port, String username, String password, Boolean auth, Boolean tls) {
        this(host == null ? "localhost" : host, port == null ? 587 : port, username == null ? "" : username, password == null ? "" : password, !Boolean.FALSE.equals(auth), !Boolean.FALSE.equals(tls));
    }
}
