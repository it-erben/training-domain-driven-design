package de.foerderung.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * ArchUnit tests enforcing Clean Architecture rules.
 */
@AnalyzeClasses(packages = "de.foerderung.antragstellung")
class ArchitectureTest {

    @ArchTest
    static final ArchRule domain_should_not_depend_on_infrastructure_or_adapter =
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..infrastructure..", "..adapter..");

    @ArchTest
    static final ArchRule domain_has_no_spring_imports =
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("org.springframework..");

    @ArchTest
    static final ArchRule rest_controller_should_not_access_domain_model_directly =
            noClasses()
                    .that().resideInAPackage("..adapter.web..")
                    .and().areAnnotatedWith(RestController.class)
                    .should().dependOnClassesThat()
                    .resideInAPackage("..domain.model..");

    @ArchTest
    static final ArchRule only_web_adapter_should_use_rest_controller =
            classes()
                    .that().areAnnotatedWith(RestController.class)
                    .should().resideInAPackage("..adapter.web..");

    @ArchTest
    static final ArchRule transactional_only_in_application_service =
            noClasses()
                    .that().resideOutsideOfPackage("..application.service..")
                    .should().beAnnotatedWith(Transactional.class);
}
