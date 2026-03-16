package be.wiserisk.hlabmonitor.archunit;

import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.in.notification.NotificationHandlerDelegate;
import be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.in.notification.NotificationSender;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import jakarta.persistence.*;
import org.junit.jupiter.api.Test;
import org.mapstruct.Mapper;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static be.wiserisk.hlabmonitor.archunit.AppEnum.*;
import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.properties.HasName.Predicates.nameMatching;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

class InfrastructureTest {

    private final JavaClasses classes = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("be.wiserisk.hlabmonitor");

    @Test
    void infrastructure() {
        classes()
                .that().resideInAPackage(INFRASTRUCTURE.getStringPackage())
                .should().onlyHaveDependentClassesThat().resideOutsideOfPackages(DOMAIN.getStringPackage())
                .check(classes);
    }

    @Test
    void adapter_in_rest() {
        classes()
                .that().resideInAPackage(ADAPTER_IN_REST.getStringPackage())
                .should().beAnnotatedWith(RestController.class)
                .orShould().beAnnotatedWith(RestControllerAdvice.class)
                .check(classes);
    }

    @Test
    void adapter_in_notification() {
        classes()
                .that().resideInAPackage(ADAPTER_IN_NOTIFICATION.getStringPackage())
                .and().areTopLevelClasses()
                .should().beInterfaces()
                .orShould().implement(NotificationSender.class)
                .orShould().be(NotificationHandlerDelegate.class)
                .orShould().implement(JavaClass.Predicates.resideInAnyPackage(PORTS_OUT.getStringPackage()))
                .check(classes);
    }

    @Test
    void adapter_out_persistence_converter() {
        classes()
                .that().resideInAnyPackage(ADAPTER_OUT_PERSISTENCE_CONVERTER.getStringPackage())
                .should().beAnnotatedWith(Converter.class)
                .andShould().implement(AttributeConverter.class)
                .check(classes);
    }

    @Test
    void adapter_out_persistence_entity() {
        classes()
                .that().resideInAnyPackage(ADAPTER_OUT_PERSISTENCE_ENTITY.getStringPackage())
                .and(not(nameMatching(".*_")))
                .should().beAnnotatedWith(Entity.class)
                .andShould().beAnnotatedWith(Table.class)
                .andShould(ArchCondition.from(DescribedPredicate.describe("All field has @Column", c -> c.getFields().stream().allMatch(f -> f.isAnnotatedWith(Column.class)))))
                .check(classes);
    }

    @Test
    void adapter_out_persistence_repository() {
        classes()
                .that().resideInAnyPackage(ADAPTER_OUT_PERSISTENCE_REPOSITORY.getStringPackage())
                .should().beInterfaces()
                .andShould().beAnnotatedWith(Repository.class)
                .check(classes);
    }

    @Test
    void adapter_out_scheduler() {
        classes()
                .that().resideInAnyPackage(ADAPTER_OUT_SCHEDULER.getStringPackage())
                .should().implement(JavaClass.Predicates.resideInAnyPackage(PORTS_OUT.getStringPackage()))
                .check(classes);
    }

    @Test
    void adapter_out() {
        classes()
                .that().resideInAnyPackage(ADAPTER_OUT.getStringPackage())
                .and().resideOutsideOfPackages(ADAPTER_OUT_PERSISTENCE.getStringPackage(), ADAPTER_OUT_SCHEDULER.getStringPackage())
                .should().implement(JavaClass.Predicates.resideInAnyPackage(PORTS_OUT.getStringPackage()))
                .check(classes);
    }

    @Test
    void config() {
        classes()
                .that().resideInAnyPackage(CONFIG.getStringPackage())
                .and().areTopLevelClasses()
                .and().resideOutsideOfPackages(CONFIG_MAPPER.getStringPackage(), CONFIG_YAML.getStringPackage())
                .should().beAnnotatedWith(Configuration.class)
                .andShould(ArchCondition.from(DescribedPredicate.describe("All public method has @Bean", c -> c.getMethods().stream().filter(m -> m.getModifiers().contains(JavaModifier.PUBLIC)).allMatch(m -> m.isAnnotatedWith(Bean.class)))))
                .check(classes);
    }

    @Test
    void config_mapper() {
        classes()
                .that().resideInAPackage(CONFIG_MAPPER.getStringPackage())
                .should().beInterfaces()
                .andShould().beAnnotatedWith(Mapper.class)
                .orShould().haveNameMatching(".*Impl")
                .check(classes);
    }

    @Test
    void config_yaml() {
        classes()
                .that().resideInAPackage(CONFIG_YAML.getStringPackage())
                .and().areTopLevelClasses()
                .should().beAnnotatedWith(Configuration.class)
                .orShould().beAnnotatedWith(ConfigurationProperties.class)
                .orShould().beRecords()
                .orShould().beInterfaces()
                .orShould().haveSimpleName("MonitoringToTargetAdapter")
                .check(classes);
    }
}