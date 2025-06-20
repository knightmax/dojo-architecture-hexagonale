package moon.mission.rescue;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@AnalyzeClasses(packages = "moon.mission.rescue", importOptions = ImportOption.DoNotIncludeTests.class)
public class HexagonalArchitectureTest {

    @ArchTest
    ArchRule hexagonal = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .layer("Domain").definedBy("moon.mission.rescue.domain..")
            .layer("Application").definedBy("moon.mission.rescue.application..")
            .layer("Fleet Adapter").definedBy("moon.mission.rescue.fleet..")
            .layer("StarShip Adapter").definedBy("moon.mission.rescue.starship..")
            .whereLayer("Application").mayNotBeAccessedByAnyLayer()
            .whereLayer("Application").mayOnlyAccessLayers("Domain")
            .whereLayer("Fleet Adapter").mayNotBeAccessedByAnyLayer()
            .whereLayer("StarShip Adapter").mayNotBeAccessedByAnyLayer()
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Fleet Adapter", "StarShip Adapter")
            .as("Hexagonal Architecture");


    @ArchTest
    ArchRule domainShouldNotDependOnSpring = classes().that().resideInAPackage("moon.mission.rescue.domain..")
            .should().notBeMetaAnnotatedWith(Component.class)
            .andShould().notBeAnnotatedWith(Service.class)
            .as("Domain should not depend on Spring");
}
