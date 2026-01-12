package be.wiserisk.hlabmonitor.monitor.infrastructure.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource dataSource() {
        //Mettre en place le choix entre H2, SQLite, Postgres et SQLServer
        HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName("org.h2.Driver");
        ds.setJdbcUrl("jdbc:h2:mem:monitor;DB_CLOSE_DELAY=-1");
        return ds;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.out.persistence.entity"); // Vos entités JPA ici
        em.setPersistenceUnitName("monitorPU");

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        properties.put("hibernate.show_sql", "true"); // Optionnel pour debug
        em.setJpaPropertyMap(properties);

        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        return em;
    }


}
