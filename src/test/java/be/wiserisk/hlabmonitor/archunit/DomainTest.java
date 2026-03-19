package be.wiserisk.hlabmonitor.archunit;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static be.wiserisk.hlabmonitor.archunit.AppEnum.*;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

class DomainTest {

    private final JavaClasses classes = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("be.wiserisk.hlabmonitor");

    @Test
    void domain() {
        classes()
                .that().resideInAPackage(DOMAIN.getStringPackage())
                .should().onlyDependOnClassesThat()
                .resideInAnyPackage(DOMAIN.getStringPackage(), PORTS.getStringPackage(), STD_JAVA.getStringPackage(), STD_LOMBOK.getStringPackage())
                .check(classes);
    }

    @Test
    void services() {
        classes()
                .that().resideInAPackage(DOMAIN_SERVICE.getStringPackage())
                .and().areTopLevelClasses()
                .should().onlyHaveDependentClassesThat().resideInAnyPackage(PORTS_OUT.getStringPackage(), CONFIG.getStringPackage())
                .andShould().implement(JavaClass.Predicates.resideInAnyPackage(PORTS_IN.getStringPackage()))
                .check(classes);
    }

    @Test
    void enums() {
        classes()
                .that().resideInAPackage(DOMAIN_ENUM.getStringPackage())
                .and().areTopLevelClasses()
                .should().beEnums()
                .andShould().onlyHaveDependentClassesThat().resideOutsideOfPackages(DOMAIN_ENUM.getStringPackage())
                .check(classes);
    }

    @Test
    void model() {
        classes()
                .that().resideInAPackage(DOMAIN_MODEL.getStringPackage())
                .and().areTopLevelClasses()
                .should().beRecords()
                .andShould().onlyDependOnClassesThat().resideInAnyPackage(DOMAIN_MODEL.getStringPackage(), DOMAIN_ENUM.getStringPackage(), STD_JAVA.getStringPackage())
                .check(classes);
    }

    @Test
    void exceptions() {
        classes()
                .that().resideInAPackage(DOMAIN_EXCEPTION.getStringPackage())
                .and().areTopLevelClasses()
                .should().beAssignableTo(Exception.class)
                .andShould().onlyDependOnClassesThat().resideOutsideOfPackages(DOMAIN_EXCEPTION.getStringPackage())
                .check(classes);
    }
}