package be.wiserisk.hlabmonitor.monitor.infrastructure.config;

import be.wiserisk.hlabmonitor.monitor.infrastructure.config.yaml.DatabaseProperties;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJpaRepositories(basePackages = "be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence")
@EnableTransactionManagement
public class DataSourceConfig {

    @Bean
    @Primary
    public DataSource dataSource(DatabaseProperties databaseProperties) {
        return switch (databaseProperties.type()) {
            case SQLITE -> configureSQLite(databaseProperties);
            case POSTGRESQL -> configurePostgreSQL(databaseProperties);
            case SQLSERVER -> configureSQLServer(databaseProperties);
            default -> configureH2(databaseProperties);
        };
    }

    private DataSource configureH2(DatabaseProperties databaseProperties) {
        HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName(databaseProperties.type().driverClassName);
        ds.setJdbcUrl("jdbc:h2:mem:monitor;DB_CLOSE_DELAY=-1");
        return ds;
    }

    private DataSource configureSQLite(DatabaseProperties databaseProperties) {
        try {
            Path dbPath = Paths.get(databaseProperties.path());
            Path parentDir = dbPath.getParent();

            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Impossible de créer le dossier pour SQLite : " + databaseProperties.path() +
                            ". Vérifiez les permissions d'écriture.", e
            );
        }

        HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName(databaseProperties.type().driverClassName);
        ds.setJdbcUrl("jdbc:sqlite:" + databaseProperties.path());
        ds.setMaximumPoolSize(1);
        return ds;
    }

    private DataSource configurePostgreSQL(DatabaseProperties databaseProperties) {
        HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName(databaseProperties.type().driverClassName);
        ds.setJdbcUrl(String.format("jdbc:postgresql://%s:%d/%s",
                databaseProperties.host(),
                databaseProperties.port(),
                databaseProperties.name()));
        ds.setUsername(databaseProperties.username());
        ds.setPassword(databaseProperties.password());
        return ds;
    }

    private DataSource configureSQLServer(DatabaseProperties databaseProperties) {
        HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName(databaseProperties.type().driverClassName);
        ds.setJdbcUrl(String.format("jdbc:sqlserver://%s:%d;databaseName=%s",
                databaseProperties.host(),
                databaseProperties.port(),
                databaseProperties.name()));
        ds.setUsername(databaseProperties.username());
        ds.setPassword(databaseProperties.password());
        return ds;
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource, DatabaseProperties databaseProperties) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.entity");
        em.setPersistenceUnitName("monitorPU");

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "none");
        properties.put("hibernate.dialect", databaseProperties.type().hibernateDialect);
        properties.put("hibernate.show_sql", "true");

        em.setJpaPropertyMap(properties);
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        return em;
    }

    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}