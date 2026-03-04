package de.immobiliencrm.integration;

import de.immobiliencrm.akquise.application.service.MaklerauftragAbschliessenUseCase;
import de.immobiliencrm.akquise.domain.model.Maklerauftrag;
import de.immobiliencrm.vermittlung.domain.port.VermittlungsvorgangRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Integration test verifying that completing a Maklerauftrag in the Akquise BC
 * automatically creates a Vermittlungsvorgang in the Vermittlung BC via events.
 */
@SpringBootTest
class ContextIntegrationTest {

    @Autowired
    private MaklerauftragAbschliessenUseCase maklerauftragUseCase;

    @Autowired
    private VermittlungsvorgangRepository vermittlungsvorgangRepository;

    @Test
    void shouldCreateVermittlungsvorgangWhenMaklerauftragAbgeschlossen() {
        // Given: a new Maklerauftrag is created
        UUID eigentuemerId = UUID.randomUUID();
        UUID immobilieId = UUID.randomUUID();
        Maklerauftrag auftrag = maklerauftragUseCase.erstellen(eigentuemerId, immobilieId);

        // When: the Maklerauftrag is concluded
        maklerauftragUseCase.abschliessen(auftrag.getId());

        // Then: a Vermittlungsvorgang should have been created automatically
        assertFalse(vermittlungsvorgangRepository.findAll().isEmpty(),
                "A Vermittlungsvorgang should have been created via event listener");
    }
}
