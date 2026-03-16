package be.wiserisk.hlabmonitor.archunit;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static be.wiserisk.hlabmonitor.archunit.AppEnum.*;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

class ApplicationTest {

    private final JavaClasses classes = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("be.wiserisk.hlabmonitor");

    @Test
    void port() {
        classes()
                .that().resideInAPackage(PORTS.getStringPackage())
                .should().beInterfaces()
                .check(classes);
    }

    @Test
    void port_in() {
        classes()
                .that().resideInAPackage(PORTS_IN.getStringPackage())
                .should().onlyHaveDependentClassesThat().resideInAnyPackage(DOMAIN_SERVICE.getStringPackage(), ADAPTER.getStringPackage(), CONFIG.getStringPackage())
                .check(classes);
    }

    @Test
    void port_out() {
        classes()
                .that().resideInAPackage(PORTS_OUT.getStringPackage())
                .should().onlyDependOnClassesThat().resideInAnyPackage(PORTS_OUT.getStringPackage(), DOMAIN.getStringPackage(), STD_JAVA.getStringPackage())
                .check(classes);
    }
}