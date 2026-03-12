package be.wiserisk.hlabmonitor.archunit;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static be.wiserisk.hlabmonitor.archunit.AppEnum.*;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(packages = "be.wiserisk.hlabmonitor", importOptions = ImportOption.DoNotIncludeTests.class)
public class DomainTest {

    @ArchTest
    static final ArchRule domain =
            classes()
                    .that().resideInAPackage(DOMAIN.getStringPackage())
                    .should().onlyDependOnClassesThat()
                    .resideInAnyPackage(DOMAIN.getStringPackage(), PORTS.getStringPackage(), STD_JAVA.getStringPackage(), STD_LOMBOK.getStringPackage());

    @ArchTest
    static final ArchRule services =
            classes()
                    .that().resideInAPackage(DOMAIN_SERVICE.getStringPackage())
                    .and().areTopLevelClasses()
                    .should().onlyHaveDependentClassesThat().resideInAnyPackage(PORTS_OUT.getStringPackage(), CONFIG.getStringPackage())
                    .andShould().implement(JavaClass.Predicates.resideInAnyPackage(PORTS_IN.getStringPackage()));

    @ArchTest
    static final ArchRule enums =
            classes()
                    .that().resideInAPackage(DOMAIN_ENUM.getStringPackage())
                    .and().areTopLevelClasses()
                    .should().beEnums()
                    .andShould().onlyHaveDependentClassesThat().resideOutsideOfPackages(DOMAIN_ENUM.getStringPackage());

    @ArchTest
    static final ArchRule model =
            classes()
                    .that().resideInAPackage(DOMAIN_MODEL.getStringPackage())
                    .and().areTopLevelClasses()
                    .should().beRecords()
                    .andShould().onlyDependOnClassesThat().resideInAnyPackage(DOMAIN_MODEL.getStringPackage(), DOMAIN_ENUM.getStringPackage(), STD_JAVA.getStringPackage());

    @ArchTest
    static final ArchRule exceptions =
            classes()
                    .that().resideInAPackage(DOMAIN_EXCEPTION.getStringPackage())
                    .and().areTopLevelClasses()
                    .should().beAssignableTo(Exception.class)
                    .andShould().onlyDependOnClassesThat().resideOutsideOfPackages(DOMAIN_EXCEPTION.getStringPackage());
}