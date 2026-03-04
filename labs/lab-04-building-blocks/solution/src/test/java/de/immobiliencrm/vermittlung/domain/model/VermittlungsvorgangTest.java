package de.immobiliencrm.vermittlung.domain.model;

import de.immobiliencrm.vermittlung.domain.event.AngebotAngenommen;
import de.immobiliencrm.vermittlung.domain.event.AngebotEingegangen;
import de.immobiliencrm.vermittlung.domain.event.BesichtigungDurchgefuehrt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

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
    @DisplayName("Neuer Vermittlungsvorgang hat Status NEU")
    void neuerVorgangHatStatusNeu() {
        assertEquals(VermittlungsvorgangStatus.NEU, vorgang.getStatus());
    }

    @Test
    @DisplayName("Invariante: Status kann nicht auf NOTARTERMIN gesetzt werden ohne angenommenes Angebot")
    void statusAufNotarterminOhneAngenommenesAngebotWirftException() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> vorgang.statusAufNotarterminSetzen()
        );

        assertTrue(exception.getMessage().contains("angenommenes Angebot"));
    }

    @Test
    @DisplayName("Invariante: Status kann auf NOTARTERMIN gesetzt werden mit angenommenem Angebot")
    void statusAufNotarterminMitAngenommenemAngebotErfolgreich() {
        // Arrange: add and accept an offer
        Angebot angebot = vorgang.angebotEntgegennehmen("Max Mustermann", new BigDecimal("340000"));
        vorgang.angebotAnnehmen(angebot.getId());

        // Act
        vorgang.statusAufNotarterminSetzen();

        // Assert
        assertEquals(VermittlungsvorgangStatus.NOTARTERMIN, vorgang.getStatus());
    }

    @Test
    @DisplayName("Vollstaendiger Ablauf: Besichtigung, Angebot, Notartermin")
    void vollstaendigerAblauf() {
        // Step 1: Add a viewing
        Besichtigung besichtigung = vorgang.besichtigungHinzufuegen(
                "Erika Musterfrau",
                LocalDateTime.now().plusDays(3),
                "Erstbesichtigung"
        );
        assertEquals(VermittlungsvorgangStatus.BESICHTIGUNG, vorgang.getStatus());
        assertEquals(1, vorgang.getBesichtigungen().size());

        // Step 2: Complete the viewing
        vorgang.besichtigungDurchfuehren(besichtigung.getId());
        assertTrue(vorgang.getBesichtigungen().get(0).isDurchgefuehrt());

        // Step 3: Receive an offer
        Angebot angebot = vorgang.angebotEntgegennehmen("Erika Musterfrau", new BigDecimal("345000"));
        assertEquals(VermittlungsvorgangStatus.ANGEBOT_PHASE, vorgang.getStatus());
        assertEquals(1, vorgang.getAngebote().size());

        // Step 4: Accept the offer
        vorgang.angebotAnnehmen(angebot.getId());
        assertTrue(vorgang.getAngebote().get(0).isAngenommen());

        // Step 5: Set status to NOTARTERMIN
        vorgang.statusAufNotarterminSetzen();
        assertEquals(VermittlungsvorgangStatus.NOTARTERMIN, vorgang.getStatus());

        // Verify domain events were collected
        assertEquals(3, vorgang.getDomainEvents().size());
        assertInstanceOf(BesichtigungDurchgefuehrt.class, vorgang.getDomainEvents().get(0));
        assertInstanceOf(AngebotEingegangen.class, vorgang.getDomainEvents().get(1));
        assertInstanceOf(AngebotAngenommen.class, vorgang.getDomainEvents().get(2));
    }

    @Test
    @DisplayName("Besichtigung durchfuehren mit unbekannter ID wirft Exception")
    void besichtigungDurchfuehrenMitUnbekannterIdWirftException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> vorgang.besichtigungDurchfuehren(UUID.randomUUID())
        );
    }

    @Test
    @DisplayName("Angebot annehmen mit unbekannter ID wirft Exception")
    void angebotAnnehmenMitUnbekannterIdWirftException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> vorgang.angebotAnnehmen(UUID.randomUUID())
        );
    }

    @Test
    @DisplayName("Domain Events koennen geleert werden")
    void domainEventsKoennenGeleertWerden() {
        vorgang.angebotEntgegennehmen("Max Mustermann", new BigDecimal("340000"));
        assertEquals(1, vorgang.getDomainEvents().size());

        vorgang.clearDomainEvents();
        assertEquals(0, vorgang.getDomainEvents().size());
    }
}
