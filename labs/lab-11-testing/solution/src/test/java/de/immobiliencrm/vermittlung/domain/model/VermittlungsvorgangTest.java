package de.immobiliencrm.vermittlung.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pure domain unit tests -- no Spring context required.
 * Tests the invariants and behavior of the Vermittlungsvorgang aggregate root.
 */
class VermittlungsvorgangTest {

    private Vermittlungsvorgang vorgang;

    @BeforeEach
    void setUp() {
        vorgang = Vermittlungsvorgang.erstellen(
                UUID.randomUUID(),
                new Adresse("Musterstrasse 1", "50667", "Koeln"),
                new Preisvorstellung(new BigDecimal("350000"), "EUR"),
                new Provision(new BigDecimal("3.57"))
        );
    }

    @Test
    @DisplayName("statusAufNotarterminSetzen() throws IllegalStateException when no accepted offer exists")
    void test_statusAufNotartermin_ohneAngenommenesAngebot_wirftException() {
        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> vorgang.statusAufNotarterminSetzen()
        );

        assertTrue(exception.getMessage().contains("angenommenes Angebot"));
    }

    @Test
    @DisplayName("statusAufNotarterminSetzen() succeeds when an accepted offer exists")
    void test_statusAufNotartermin_mitAngenommenemAngebot_erfolgreich() {
        // Arrange: add and accept an offer
        Angebot angebot = vorgang.angebotEntgegennehmen("Max Mustermann", new BigDecimal("340000"));
        vorgang.angebotAnnehmen(angebot.getId());

        // Act
        vorgang.statusAufNotarterminSetzen();

        // Assert
        assertEquals(VermittlungsvorgangStatus.NOTARTERMIN, vorgang.getStatus());
    }

    @Test
    @DisplayName("besichtigungHinzufuegen() creates a Besichtigung and updates the status")
    void test_besichtigungHinzufuegen_erstelltBesichtigung() {
        // Act
        Besichtigung besichtigung = vorgang.besichtigungHinzufuegen(
                "Erika Musterfrau",
                LocalDateTime.now().plusDays(3),
                "Erstbesichtigung"
        );

        // Assert
        assertNotNull(besichtigung);
        assertNotNull(besichtigung.getId());
        assertEquals("Erika Musterfrau", besichtigung.getInteressentName());
        assertEquals(1, vorgang.getBesichtigungen().size());
        assertEquals(VermittlungsvorgangStatus.BESICHTIGUNG, vorgang.getStatus());
    }

    @Test
    @DisplayName("angebotAnnehmen() sets angenommen to true")
    void test_angebotAnnehmen_setztAngenommenAufTrue() {
        // Arrange
        Angebot angebot = vorgang.angebotEntgegennehmen("Max Mustermann", new BigDecimal("340000"));

        // Act
        vorgang.angebotAnnehmen(angebot.getId());

        // Assert
        assertTrue(vorgang.getAngebote().get(0).isAngenommen());
    }
}
