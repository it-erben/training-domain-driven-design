package de.foerderung.antragstellung.domain.model;

import de.foerderung.antragstellung.domain.event.AntragsmappeEingereicht;
import de.foerderung.antragstellung.domain.event.AntragsmappeErstellt;
import de.foerderung.antragstellung.domain.event.FlurstueckHinzugefuegt;
import de.foerderung.antragstellung.domain.event.NachweisAkzeptiert;
import de.foerderung.antragstellung.domain.event.NachweisEingereicht;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AntragsMappeTest {

    private AntragsMappe createMappe() {
        return AntragsMappe.erstellen(
                new RegistrierungsNummer("DZ-BW-2024-0042"),
                new Foerderbetrag(new BigDecimal("10000"), "EUR"),
                new Foerderquote(new BigDecimal("0.35"))
        );
    }

    // --- Invariant Tests ---

    @Test
    void einreichen_ohneFlurstueck_wirftException() {
        var mappe = createMappe();

        assertThatThrownBy(mappe::einreichen)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Flurstueck");
    }

    @Test
    void einreichen_mitFlurstueck_erfolgreich() {
        var mappe = createMappe();
        mappe.flurstueckHinzufuegen(
                new FlurstueckNummer("042/0815"), new BigDecimal("12.5"), null);

        mappe.einreichen();

        assertThat(mappe.getStatus()).isEqualTo(AntragStatus.EINGEREICHT);
    }

    // --- Complete Flow Tests ---

    @Test
    void flurstueckHinzufuegen_setsStatusAndEmitsEvent() {
        var mappe = createMappe();

        var flurstueckId = mappe.flurstueckHinzufuegen(
                new FlurstueckNummer("042/0815"), new BigDecimal("12.5"), "Acker");

        assertThat(mappe.getStatus()).isEqualTo(AntragStatus.IN_BEARBEITUNG);
        assertThat(mappe.getFlurstuecke()).hasSize(1);
        assertThat(flurstueckId).isNotNull();
        assertThat(mappe.getDomainEvents())
                .filteredOn(e -> e instanceof FlurstueckHinzugefuegt)
                .hasSize(1);
    }

    @Test
    void flurstueckPruefen_marksAsGeprueft() {
        var mappe = createMappe();
        var flurstueckId = mappe.flurstueckHinzufuegen(
                new FlurstueckNummer("042/0815"), new BigDecimal("12.5"), null);

        mappe.flurstueckPruefen(flurstueckId);

        assertThat(mappe.getFlurstuecke().get(0).isGeprueft()).isTrue();
    }

    @Test
    void nachweisEinreichen_emitsEvent() {
        var mappe = createMappe();

        var nachweisId = mappe.nachweisEinreichen("Eigentumsnachweis", "Max Mustermann");

        assertThat(nachweisId).isNotNull();
        assertThat(mappe.getNachweise()).hasSize(1);
        assertThat(mappe.getDomainEvents())
                .anyMatch(e -> e instanceof NachweisEingereicht);
    }

    @Test
    void nachweisAkzeptieren_emitsEvent() {
        var mappe = createMappe();
        var nachweisId = mappe.nachweisEinreichen("Eigentumsnachweis", "Max Mustermann");

        mappe.nachweisAkzeptieren(nachweisId);

        assertThat(mappe.getNachweise().get(0).isAkzeptiert()).isTrue();
        assertThat(mappe.getDomainEvents())
                .anyMatch(e -> e instanceof NachweisAkzeptiert);
    }

    @Test
    void completeFlow() {
        var mappe = createMappe();

        // 1. Flurstueck hinzufuegen
        var flurstueckId = mappe.flurstueckHinzufuegen(
                new FlurstueckNummer("042/0815"), new BigDecimal("12.5"), null);
        assertThat(mappe.getStatus()).isEqualTo(AntragStatus.IN_BEARBEITUNG);

        // 2. Flurstueck pruefen
        mappe.flurstueckPruefen(flurstueckId);

        // 3. Nachweis einreichen
        var nachweisId = mappe.nachweisEinreichen("Eigentumsnachweis", "Max Mustermann");

        // 4. Nachweis akzeptieren
        mappe.nachweisAkzeptieren(nachweisId);

        // 5. Einreichen
        mappe.einreichen();
        assertThat(mappe.getStatus()).isEqualTo(AntragStatus.EINGEREICHT);

        // Verify all events (AntragsmappeErstellt from factory + 4 from operations)
        assertThat(mappe.getDomainEvents()).hasSize(5);
        assertThat(mappe.getDomainEvents().get(0)).isInstanceOf(AntragsmappeErstellt.class);
        assertThat(mappe.getDomainEvents().get(1)).isInstanceOf(FlurstueckHinzugefuegt.class);
        assertThat(mappe.getDomainEvents().get(2)).isInstanceOf(NachweisEingereicht.class);
        assertThat(mappe.getDomainEvents().get(3)).isInstanceOf(NachweisAkzeptiert.class);
        assertThat(mappe.getDomainEvents().get(4)).isInstanceOf(AntragsmappeEingereicht.class);
    }

    // --- Edge Cases ---

    @Test
    void flurstueckMitLeererNummer_wirftException() {
        assertThatThrownBy(() -> new FlurstueckNummer(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nachweisMitLeeremDokumentTyp_wirftException() {
        var mappe = createMappe();

        assertThatThrownBy(() -> mappe.nachweisEinreichen("", "Max Mustermann"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void flurstueckPruefen_nichtVorhanden_wirftException() {
        var mappe = createMappe();

        assertThatThrownBy(() -> mappe.flurstueckPruefen(FlurstueckId.generate()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nachweisAkzeptieren_nichtVorhanden_wirftException() {
        var mappe = createMappe();

        assertThatThrownBy(() -> mappe.nachweisAkzeptieren(NachweisId.generate()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rekonstruieren_storesStateWithoutEvents() {
        var id = AntragId.generate();
        var mappe = AntragsMappe.rekonstruieren(
                id,
                new RegistrierungsNummer("DZ-BW-2024-0042"),
                new Foerderbetrag(new BigDecimal("10000"), "EUR"),
                new Foerderquote(new BigDecimal("0.35")),
                AntragStatus.IN_BEARBEITUNG,
                List.of(Flurstueck.rekonstruieren(
                        FlurstueckId.generate(),
                        new FlurstueckNummer("042/0815"),
                        new BigDecimal("12.5"),
                        "Acker",
                        true)),
                List.of()
        );

        assertThat(mappe.getId()).isEqualTo(id);
        assertThat(mappe.getStatus()).isEqualTo(AntragStatus.IN_BEARBEITUNG);
        assertThat(mappe.getFlurstuecke()).hasSize(1);
        assertThat(mappe.getFlurstuecke().get(0).isGeprueft()).isTrue();
        assertThat(mappe.getDomainEvents()).isEmpty();
    }

    @Test
    void clearDomainEvents_emptiesList() {
        var mappe = createMappe();
        mappe.flurstueckHinzufuegen(
                new FlurstueckNummer("042/0815"), new BigDecimal("12.5"), null);

        assertThat(mappe.getDomainEvents()).isNotEmpty();

        mappe.clearDomainEvents();

        assertThat(mappe.getDomainEvents()).isEmpty();
    }
}
