package be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml;

public interface SystemInterface {

    static String getProperty(String key) {
        return System.getProperty(key);
    }

    static String getEnv(String key) {
        return System.getenv(key);
    }
}
