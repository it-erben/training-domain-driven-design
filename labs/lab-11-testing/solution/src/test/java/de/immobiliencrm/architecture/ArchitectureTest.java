package de.immobiliencrm.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Architecture tests using ArchUnit.
 * Verifies that Clean Architecture rules are enforced across the codebase.
 */
@AnalyzeClasses(packages = "de.immobiliencrm")
class ArchitectureTest {

    // Rule 1: Domain must not depend on infrastructure or adapter
    @ArchTest
    static final ArchRule domain_should_not_depend_on_infrastructure_or_adapter =
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..infrastructure..", "..adapter..");

    // Rule 2: No Spring framework classes in the domain layer
    @ArchTest
    static final ArchRule domain_should_not_use_spring =
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("org.springframework..");

    // Rule 3: Web adapter must not access domain model directly
    @ArchTest
    static final ArchRule web_adapter_should_not_access_domain_model_directly =
            noClasses()
                    .that().resideInAPackage("..adapter.web..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..domain.model..");

    // Rule 4: Only web adapter classes may use @RestController
    @ArchTest
    static final ArchRule only_web_adapter_should_use_rest_controller =
            classes()
                    .that().areAnnotatedWith(RestController.class)
                    .should().resideInAPackage("..adapter.web..");

    // Rule 5: Domain events should be records
    @ArchTest
    static final ArchRule domain_events_should_be_records =
            classes()
                    .that().resideInAPackage("..domain.event..")
                    .should().beAssignableTo(Record.class);
}
