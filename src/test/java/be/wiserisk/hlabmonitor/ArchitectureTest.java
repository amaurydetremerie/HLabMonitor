package be.wiserisk.hlabmonitor;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@AnalyzeClasses(packages = "be.wiserisk.hlabmonitor")
class ArchitectureTest {

    private static final String DOMAIN = "be.wiserisk.hlabmonitor.monitor.domain..";
    private static final String DOMAIN_PORT_IN = "be.wiserisk.hlabmonitor.monitor.application.port.in..";
    private static final String DOMAIN_PORT_OUT = "be.wiserisk.hlabmonitor.monitor.application.port.out..";
    private static final String DOMAIN_PORTS = DOMAIN_PORT_IN + "," + DOMAIN_PORT_OUT;
    private static final String DOMAIN_SERVICES = DOMAIN + "service..";
    private static final String APPLICATION = "be.wiserisk.hlabmonitor.monitor.application..";
    private static final String INFRASTRUCTURE = "be.wiserisk.hlabmonitor.monitor.infrastructure..";
    private static final String ADAPTER_IN = INFRASTRUCTURE + "adapter.in..";
    private static final String ADAPTER_OUT = INFRASTRUCTURE + "adapter.out..";

    @ArchTest
    static final ArchRule domain_is_independent =
            noClasses()
                    .that().resideInAPackage(DOMAIN)
                    .should().dependOnClassesThat()
                    .resideOutsideOfPackage(DOMAIN)
                    .as("Le domain ne dépend que de lui-même (pas d'infra, pas d'app, pas d'adapters)");

    @ArchTest
    static final ArchRule application_may_access_domain_ports =
            classes()
                    .that().resideInAPackage(APPLICATION)
                    .should().onlyDependOnClassesThat()
                    .resideInAnyPackage(DOMAIN, DOMAIN_PORTS, APPLICATION)
                    .as("L'application accède au domain et ports uniquement");

    /*@ArchTest
    static final ArchRule domain_services_only_use_ports =
            noClasses()
                    .that().resideInAPackage(DOMAIN_SERVICES)
                    .should().dependOnClassesThat()
                    .resideOutsideOfPackages(DOMAIN, DOMAIN_PORTS)
                    .as("Les services domain ne dépendent que du domain et de ses ports");



    @ArchTest
    static final ArchRule adapters_cannot_access_domain =
            noClasses()
                    .that().resideInAnyPackage(ADAPTER_IN, ADAPTER_OUT)
                    .should().dependOnClassesThat()
                    .resideInAPackage(DOMAIN)
                    .as("Les adapters n'accèdent pas directement au domain (seulement ports)");

    @ArchTest
    static final ArchRule hexagonal_architecture = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .layer("Domain").definedBy(DOMAIN)
            .layer("Ports").definedBy(DOMAIN_PORTS)
            .layer("Application").definedBy(APPLICATION)
            .layer("Infrastructure").definedBy(INFRASTRUCTURE)
            .whereLayer("Domain").mayNotAccessAnyLayer()
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Ports")
            .whereLayer("Application").mayOnlyAccessLayers("Domain", "Ports")
            .whereLayer("Infrastructure").mayOnlyAccessLayers("Application", "Ports");

    @ArchTest
    static final ArchRule no_spring_in_domain =
            noClasses()
                    .that().resideInAPackage(DOMAIN)
                    .should().accessClassesThat()
                    .resideInAnyPackage("org.springframework..")
                    .as("Pas de Spring (@Service, @Repository) dans le domain");*/
}