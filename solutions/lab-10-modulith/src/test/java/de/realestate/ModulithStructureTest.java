package de.realestate;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Verifies the module structure of the application using Spring Modulith.
 * This test checks that:
 * - No module accesses internal packages of another module
 * - Only declared dependencies between modules exist
 */
class ModulithStructureTest {

    ApplicationModules modules =
            ApplicationModules.of(RealEstateCrmApplication.class);

    @Test
    void verifyModuleStructure() {
        modules.verify();
    }

    @Test
    void documentModuleStructure() {
        new Documenter(modules)
                .writeDocumentation();
    }
}
