package be.wiserisk.hlabmonitor.archunit;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static be.wiserisk.hlabmonitor.archunit.AppEnum.MONITOR;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

class AppEnumTest {

    public static final JavaClasses CLASSES = new ClassFileImporter().importPackages("be.wiserisk.hlabmonitor");

    @EnumSource(value = AppEnum.class, mode = EnumSource.Mode.EXCLUDE, names = {"STD_JAVA", "STD_LOMBOK"})
    @ParameterizedTest
    void test(AppEnum packageEnum) {
        ArchRule.Assertions.check(
                classes().that().resideInAPackage(packageEnum.getStringPackage())
                        .should().resideInAPackage(MONITOR.getStringPackage()), CLASSES);
    }
}