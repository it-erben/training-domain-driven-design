package de.foerderung;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ModulithStructureTest {

    ApplicationModules modules = ApplicationModules.of(FoerderantragApplication.class);

    @Test
    void verifyModuleStructure() {
        modules.verify();
    }

    @Test
    void documentModuleStructure() {
        new Documenter(modules).writeDocumentation();
    }
}
