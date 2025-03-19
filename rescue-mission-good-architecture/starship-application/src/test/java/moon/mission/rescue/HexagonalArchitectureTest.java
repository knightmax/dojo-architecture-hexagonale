package moon.mission.rescue;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Component;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.Architectures.onionArchitecture;

@AnalyzeClasses(packages = "rebelsrescue", importOptions = ImportOption.DoNotIncludeTests.class)
public class HexagonalArchitectureTest {

    @ArchTest
    ArchRule hexagonal = onionArchitecture()
            .domainModels("rebelsrescue.domain.model")
            .domainServices("rebelsrescue.domain.service")
            .applicationServices("rebelsrescue.application")
            .adapter("controller", "rebelsrescue.controller")
            .adapter("swapi", "rebelsrescue.swapi");


    @ArchTest
    ArchRule domainShouldNotDependOnSpring = classes().that().resideInAPackage("rebelsrescue.domain..")
            .should().notBeMetaAnnotatedWith(Component.class);

}
