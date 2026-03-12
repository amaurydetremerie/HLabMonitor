package be.wiserisk.hlabmonitor.archunit;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static be.wiserisk.hlabmonitor.archunit.AppEnum.*;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(packages = "be.wiserisk.hlabmonitor", importOptions = ImportOption.DoNotIncludeTests.class)
public class ApplicationTest {

    @ArchTest
    static final ArchRule port =
            classes()
                    .that().resideInAPackage(PORTS.getStringPackage())
                    .should().beInterfaces();

    @ArchTest
    static final ArchRule port_in =
            classes()
                    .that().resideInAPackage(PORTS_IN.getStringPackage())
                    .should().onlyHaveDependentClassesThat().resideInAnyPackage(DOMAIN_SERVICE.getStringPackage(), ADAPTER.getStringPackage(), CONFIG.getStringPackage());

    @ArchTest
    static final ArchRule port_out =
            classes()
                    .that().resideInAPackage(PORTS_OUT.getStringPackage())
                    .should().onlyDependOnClassesThat().resideInAnyPackage(PORTS_OUT.getStringPackage(), DOMAIN.getStringPackage(), STD_JAVA.getStringPackage());
}