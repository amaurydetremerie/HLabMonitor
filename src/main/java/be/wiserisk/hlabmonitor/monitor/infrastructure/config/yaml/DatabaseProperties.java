package be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import static be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.SystemInterface.getEnv;
import static be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.SystemInterface.getProperty;

@ConfigurationProperties(prefix = "database")
public record DatabaseProperties(
        DatabaseType type,
        String path,
        String host,
        Integer port,
        String name,
        String username,
        String password
) implements SystemInterface {

    @ConstructorBinding
    public DatabaseProperties {
        if (type == null) {
            type = DatabaseType.H2;
        }
        if (host == null) {
            host = "localhost";
        }
        if (name == null) {
            name = "monitor";
        }
        if(path == null || path.isEmpty()) {
            path = getDefaultSQLitePath();
        }
        if(port == null) {
            port = switch (type) {
                case POSTGRESQL -> 5432;
                case SQLSERVER -> 1433;
                default -> null;
            };
        }
    }

    private static String getDefaultSQLitePath() {
        String osName = getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            String programData = getEnv("ProgramData");
            if (programData != null) {
                return programData + "\\hlabmonitor\\monitor.db";
            }
            return "C:\\ProgramData\\hlabmonitor\\monitor.db";
        } else {
            return "/var/lib/hlabmonitor/monitor.db";
        }
    }

    public enum DatabaseType {
        H2("org.hibernate.dialect.H2Dialect",
                "org.h2.Driver"),
        SQLITE("org.hibernate.community.dialect.SQLiteDialect",
                "org.sqlite.JDBC"),
        POSTGRESQL("org.hibernate.dialect.PostgreSQLDialect",
                "org.postgresql.Driver"),
        SQLSERVER("org.hibernate.dialect.SQLServerDialect",
                "com.microsoft.sqlserver.jdbc.SQLServerDriver");

        DatabaseType(String hibernateDialect, String driverClassName) {
            this.hibernateDialect = hibernateDialect;
            this.driverClassName = driverClassName;
        }

        public final String hibernateDialect;
        public final String driverClassName;
    }
}