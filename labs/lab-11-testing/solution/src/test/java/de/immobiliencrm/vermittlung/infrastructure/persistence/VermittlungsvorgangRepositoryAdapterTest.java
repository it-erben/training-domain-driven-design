package de.immobiliencrm.vermittlung.infrastructure.persistence;

import de.immobiliencrm.vermittlung.domain.model.Adresse;
import de.immobiliencrm.vermittlung.domain.model.Besichtigung;
import de.immobiliencrm.vermittlung.domain.model.Preisvorstellung;
import de.immobiliencrm.vermittlung.domain.model.Provision;
import de.immobiliencrm.vermittlung.domain.model.Vermittlungsvorgang;
import de.immobiliencrm.vermittlung.domain.model.VermittlungsvorgangStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Repository integration test using @DataJpaTest with an embedded H2 database.
 * Only the JPA layer is started -- no full application context.
 */
@DataJpaTest
@Import(VermittlungsvorgangRepositoryAdapter.class)
class VermittlungsvorgangRepositoryAdapterTest {

    @Autowired
    private VermittlungsvorgangRepositoryAdapter repository;

    @Test
    @DisplayName("save and findById round-trip works correctly")
    void test_saveAndFindById() {
        // Arrange
        Vermittlungsvorgang vorgang = Vermittlungsvorgang.erstellen(
                UUID.randomUUID(),
                new Adresse("Teststrasse 10", "50667", "Koeln"),
                new Preisvorstellung(new BigDecimal("250000"), "EUR"),
                new Provision(new BigDecimal("3.57"))
        );

        // Act
        Vermittlungsvorgang saved = repository.save(vorgang);
        Optional<Vermittlungsvorgang> loaded = repository.findById(saved.getId());

        // Assert
        assertTrue(loaded.isPresent());
        Vermittlungsvorgang result = loaded.get();
        assertEquals(saved.getId(), result.getId());
        assertEquals("Teststrasse 10", result.getAdresse().strasse());
        assertEquals("50667", result.getAdresse().plz());
        assertEquals("Koeln", result.getAdresse().ort());
        assertEquals(VermittlungsvorgangStatus.NEU, result.getStatus());
    }

    @Test
    @DisplayName("Besichtigungen are correctly persisted and loaded")
    void test_besichtigungenWerdenPersistiert() {
        // Arrange
        Vermittlungsvorgang vorgang = Vermittlungsvorgang.erstellen(
                UUID.randomUUID(),
                new Adresse("Rheinufer 5", "50667", "Koeln"),
                new Preisvorstellung(new BigDecimal("450000"), "EUR"),
                new Provision(new BigDecimal("5.00"))
        );
        Besichtigung besichtigung = vorgang.besichtigungHinzufuegen(
                "Hans Mueller",
                LocalDateTime.of(2025, 6, 15, 14, 0),
                "Erstbesichtigung"
        );

        // Act
        Vermittlungsvorgang saved = repository.save(vorgang);
        Optional<Vermittlungsvorgang> loaded = repository.findById(saved.getId());

        // Assert
        assertTrue(loaded.isPresent());
        Vermittlungsvorgang result = loaded.get();
        assertNotNull(result.getBesichtigungen());
        assertEquals(1, result.getBesichtigungen().size());

        Besichtigung loadedBesichtigung = result.getBesichtigungen().get(0);
        assertEquals(besichtigung.getId(), loadedBesichtigung.getId());
        assertEquals("Hans Mueller", loadedBesichtigung.getInteressentName());
        assertFalse(loadedBesichtigung.isDurchgefuehrt());
        assertEquals(VermittlungsvorgangStatus.BESICHTIGUNG, result.getStatus());
    }
}
