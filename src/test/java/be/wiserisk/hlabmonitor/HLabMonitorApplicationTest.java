package be.wiserisk.hlabmonitor;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

class HLabMonitorApplicationTest {

    @Nested
    @SpringBootTest
    @ActiveProfiles("test")
    class HLabMonitorApplicationTestH2 {
        @Test
        void contextLoads() {
            // Method to verify if the context can load
        }
    }

    @Nested
    @SpringBootTest
    @ActiveProfiles({"test", "db-sqlite-minimal"})
    class HLabMonitorApplicationTestSQLite {
        @Test
        void contextLoads() {
            // Method to verify if the context can load
        }
    }

    @Nested
    @SpringBootTest
    @ActiveProfiles({"test", "db-postgresql-minimal"})
    class HLabMonitorApplicationTestPostgreSQL {
        @Test
        void contextLoads() {
            // Method to verify if the context can load
        }
    }

    @Nested
    @SpringBootTest
    @ActiveProfiles({"test", "db-sqlserver-minimal"})
    class HLabMonitorApplicationTestSQLServer {
        @Test
        void contextLoads() {
            // Method to verify if the context can load
        }
    }




}