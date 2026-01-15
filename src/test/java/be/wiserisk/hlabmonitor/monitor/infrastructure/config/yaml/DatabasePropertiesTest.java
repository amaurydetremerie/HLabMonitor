package be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.DatabaseProperties.DatabaseType.*;
import static org.assertj.core.api.Assertions.assertThat;

class DatabasePropertiesTest {

    @Nested
    @SpringBootTest(classes = DatabasePropertiesTest.TestConfig.class)
    @ActiveProfiles("test")
    class H2DefaultTest {
        @Autowired
        private DatabaseProperties databaseProperties;

        @Test
        void shouldCreateH2DefaultConfiguration() {
            assertThat(databaseProperties)
                    .isNotNull()
                    .extracting("type")
                    .isEqualTo(H2);
        }

        @Test
        void shouldHaveCorrectH2DialectAndDriver() {
            assertThat(databaseProperties.type())
                    .isNotNull()
                    .extracting("hibernateDialect", "driverClassName")
                    .isEqualTo(List.of("org.hibernate.dialect.H2Dialect", "org.h2.Driver"));
        }
    }

    @Nested
    class SQLiteUnitTest {
        @Test
        void shouldCreateSQLiteDefaultConfigurationLinux() {
            try (MockedStatic<SystemInterface> systemMockedStatic = Mockito.mockStatic(SystemInterface.class)) {
                systemMockedStatic.when(() -> SystemInterface.getProperty("os.name")).thenReturn("Linux");
                DatabaseProperties databaseProperties = new DatabaseProperties(
                        SQLITE, null, null, null, null, null, null
                );
                assertThat(databaseProperties).isNotNull().extracting("path").isEqualTo("/var/lib/hlabmonitor/monitor.db");
            }
        }

        @Test
        void shouldCreateSQLiteDefaultConfigurationWindowsProgramData() {
            try (MockedStatic<SystemInterface> systemMockedStatic = Mockito.mockStatic(SystemInterface.class)) {
                systemMockedStatic.when(() -> SystemInterface.getProperty("os.name")).thenReturn("Windows");
                systemMockedStatic.when(() -> SystemInterface.getEnv("ProgramData")).thenReturn("ProgramData");
                DatabaseProperties databaseProperties = new DatabaseProperties(
                        SQLITE, null, null, null, null, null, null
                );
                assertThat(databaseProperties).isNotNull().extracting("path").isEqualTo("ProgramData\\hlabmonitor\\monitor.db");
            }
        }

        @Test
        void shouldCreateSQLiteDefaultConfigurationWindowsNoProgramData() {
            try (MockedStatic<SystemInterface> systemMockedStatic = Mockito.mockStatic(SystemInterface.class)) {
                systemMockedStatic.when(() -> SystemInterface.getProperty("os.name")).thenReturn("Windows");
                systemMockedStatic.when(() -> SystemInterface.getEnv("ProgramData")).thenReturn(null);
                DatabaseProperties databaseProperties = new DatabaseProperties(
                        SQLITE, null, null, null, null, null, null
                );
                assertThat(databaseProperties).isNotNull().extracting("path").isEqualTo("C:\\ProgramData\\hlabmonitor\\monitor.db");
            }
        }
    }

    @Nested
    @SpringBootTest(classes = DatabasePropertiesTest.TestConfig.class)
    @ActiveProfiles({"test", "db-sqlite-minimal"})
    class SQLiteMinimalTest {
        @Autowired
        private DatabaseProperties databaseProperties;

        @Test
        void shouldUseDefaultPath() {
            assertThat(databaseProperties)
                    .isNotNull()
                    .satisfies(props -> {
                        assertThat(props.type()).isEqualTo(SQLITE);
                        assertThat(props.path())
                                .satisfiesAnyOf(
                                        path -> assertThat(path).isEqualTo("C:\\ProgramData\\hlabmonitor\\monitor.db"),
                                        path -> assertThat(path).isEqualTo("/var/lib/hlabmonitor/monitor.db")
                                );
                    });
        }

        @Test
        void shouldHaveCorrectSQLiteDialectAndDriver() {
            assertThat(databaseProperties.type())
                    .isNotNull()
                    .extracting("hibernateDialect", "driverClassName")
                    .isEqualTo(List.of("org.hibernate.community.dialect.SQLiteDialect", "org.sqlite.JDBC"));
        }
    }

    @Nested
    @SpringBootTest(classes = DatabasePropertiesTest.TestConfig.class)
    @ActiveProfiles({"test", "db-sqlite-empty-path"})
    class SQLiteEmptyPathTest {
        @Autowired
        private DatabaseProperties databaseProperties;

        @Test
        void shouldUseDefaultPath() {
            assertThat(databaseProperties)
                    .isNotNull()
                    .satisfies(props -> {
                        assertThat(props.type()).isEqualTo(SQLITE);
                        assertThat(props.path())
                                .satisfiesAnyOf(
                                        path -> assertThat(path).isEqualTo("C:\\ProgramData\\hlabmonitor\\monitor.db"),
                                        path -> assertThat(path).isEqualTo("/var/lib/hlabmonitor/monitor.db")
                                );
                    });
        }
    }

    @Nested
    @SpringBootTest(classes = DatabasePropertiesTest.TestConfig.class)
    @ActiveProfiles({"test", "db-sqlite-custom"})
    class SQLiteCustomTest {
        @Autowired
        private DatabaseProperties databaseProperties;

        @Test
        void shouldUseCustomPath() {
            assertThat(databaseProperties).isNotNull().extracting("path").isEqualTo("/data/custom/monitor.db");
        }
    }

    @Nested
    @SpringBootTest(classes = DatabasePropertiesTest.TestConfig.class)
    @ActiveProfiles({"test", "db-postgresql-minimal"})
    class PostgreSQLMinimalTest {
        @Autowired
        private DatabaseProperties databaseProperties;

        @Test
        void shouldCreatePostgreSQLMinimalConfiguration() {
            assertThat(databaseProperties)
                    .isNotNull()
                    .extracting("type", "host", "port", "name", "username", "password")
                    .isEqualTo(List.of(POSTGRESQL, "localhost", 5432, "monitor", "testuser", "testpass"));
        }

        @Test
        void shouldHaveCorrectPostgreSQLDialectAndDriver() {
            assertThat(databaseProperties.type())
                    .isNotNull()
                    .extracting("hibernateDialect", "driverClassName")
                    .isEqualTo(List.of("org.hibernate.dialect.PostgreSQLDialect", "org.postgresql.Driver"));
        }
    }

    @Nested
    @SpringBootTest(classes = DatabasePropertiesTest.TestConfig.class)
    @ActiveProfiles({"test", "db-postgresql-full"})
    class PostgreSQLFullTest {
        @Autowired
        private DatabaseProperties databaseProperties;

        @Test
        void shouldCreatePostgreSQLFullConfiguration() {
            assertThat(databaseProperties)
                    .isNotNull()
                    .extracting("type", "host", "port", "name", "username", "password")
                    .isEqualTo(List.of(POSTGRESQL, "db.example.com", 5433, "production_db", "produser", "prodpass"));
        }
    }

    @Nested
    @SpringBootTest(classes = DatabasePropertiesTest.TestConfig.class)
    @ActiveProfiles({"test", "db-sqlserver-minimal"})
    class SQLServerMinimalTest {
        @Autowired
        private DatabaseProperties databaseProperties;

        @Test
        void shouldCreateSQLServerMinimalConfiguration() {
            assertThat(databaseProperties)
                    .isNotNull()
                    .extracting("type", "host", "port", "name", "username", "password")
                    .isEqualTo(List.of(SQLSERVER, "localhost", 1433, "monitor", "sa", "TestPassword123"));
        }

        @Test
        void shouldHaveCorrectSQLServerDialectAndDriver() {
            assertThat(databaseProperties.type())
                    .isNotNull()
                    .extracting("hibernateDialect", "driverClassName")
                    .isEqualTo(List.of("org.hibernate.dialect.SQLServerDialect", "com.microsoft.sqlserver.jdbc.SQLServerDriver"));
        }
    }

    @Nested
    @SpringBootTest(classes = DatabasePropertiesTest.TestConfig.class)
    @ActiveProfiles({"test", "db-sqlserver-full"})
    class SQLServerFullTest {
        @Autowired
        private DatabaseProperties databaseProperties;

        @Test
        void shouldCreateSQLServerFullConfiguration() {
            assertThat(databaseProperties)
                    .isNotNull()
                    .extracting("type", "host", "port", "name", "username", "password")
                    .isEqualTo(List.of(SQLSERVER, "sqlserver.example.com", 14331, "MonitorDB", "dbadmin", "AdminPassword456"));
        }
    }

    @EnableConfigurationProperties(DatabaseProperties.class)
    public static class TestConfig {
    }
}