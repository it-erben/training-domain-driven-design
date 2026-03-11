package de.foerderung.antragstellung.internal.domain.model;

import de.foerderung.antragstellung.internal.domain.event.AntragEvent;
import de.foerderung.antragstellung.internal.domain.event.FlurstueckHinzugefuegt;
import de.foerderung.antragstellung.internal.domain.event.InternalAntragsmappeEingereicht;
import de.foerderung.antragstellung.internal.domain.event.NachweisAkzeptiert;
import de.foerderung.antragstellung.internal.domain.event.NachweisEingereicht;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root for the AntragsMappe (application folder).
 * Enforces invariants and collects domain events.
 */
public class AntragsMappe {

    private final UUID id;
    private final RegistrierungsNummer registrierungsNummer;
    private final Foerderbetrag beantragteFoerderung;
    private final Foerderquote foerderquote;
    private AntragStatus status;
    private final List<Flurstueck> flurstuecke;
    private final List<Nachweis> nachweise;
    private transient final List<AntragEvent> domainEvents;

    private AntragsMappe(UUID id, RegistrierungsNummer registrierungsNummer,
                          Foerderbetrag beantragteFoerderung, Foerderquote foerderquote) {
        this.id = Objects.requireNonNull(id, "ID darf nicht null sein");
        this.registrierungsNummer = Objects.requireNonNull(registrierungsNummer,
                "RegistrierungsNummer darf nicht null sein");
        this.beantragteFoerderung = Objects.requireNonNull(beantragteFoerderung,
                "BeantragteFoerderung darf nicht null sein");
        this.foerderquote = Objects.requireNonNull(foerderquote,
                "Foerderquote darf nicht null sein");
        this.status = AntragStatus.NEU;
        this.flurstuecke = new ArrayList<>();
        this.nachweise = new ArrayList<>();
        this.domainEvents = new ArrayList<>();
    }

    /**
     * Factory method to create a new AntragsMappe.
     */
    public static AntragsMappe erstellen(RegistrierungsNummer registrierungsNummer,
                                          Foerderbetrag beantragteFoerderung,
                                          Foerderquote foerderquote) {
        return new AntragsMappe(UUID.randomUUID(), registrierungsNummer,
                beantragteFoerderung, foerderquote);
    }

    /**
     * Factory method to restore an aggregate from persistence without emitting domain events.
     */
    public static AntragsMappe rekonstruieren(UUID id,
                                               RegistrierungsNummer registrierungsNummer,
                                               Foerderbetrag beantragteFoerderung,
                                               Foerderquote foerderquote,
                                               AntragStatus status,
                                               List<Flurstueck> flurstuecke,
                                               List<Nachweis> nachweise) {
        AntragsMappe mappe = new AntragsMappe(id, registrierungsNummer,
                beantragteFoerderung, foerderquote);
        mappe.status = Objects.requireNonNull(status, "Status darf nicht null sein");
        mappe.flurstuecke.addAll(List.copyOf(flurstuecke));
        mappe.nachweise.addAll(List.copyOf(nachweise));
        return mappe;
    }

    /**
     * Adds a new Flurstueck to this AntragsMappe and sets the status to IN_BEARBEITUNG.
     */
    public Flurstueck flurstueckHinzufuegen(FlurstueckNummer nummer,
                                             BigDecimal flaeche, String bemerkung) {
        Flurstueck flurstueck = new Flurstueck(UUID.randomUUID(), nummer, flaeche, bemerkung);
        this.flurstuecke.add(flurstueck);
        this.status = AntragStatus.IN_BEARBEITUNG;

        domainEvents.add(new FlurstueckHinzugefuegt(
                this.id, flurstueck.getId(), LocalDateTime.now()));
        return flurstueck;
    }

    /**
     * Marks an existing Flurstueck as verified.
     */
    public void flurstueckPruefen(UUID flurstueckId) {
        Flurstueck flurstueck = flurstuecke.stream()
                .filter(f -> f.getId().equals(flurstueckId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Flurstueck mit ID " + flurstueckId + " nicht gefunden"));

        if (flurstueck.isGeprueft()) {
            throw new IllegalStateException(
                    "Flurstueck mit ID " + flurstueckId + " ist bereits geprueft");
        }

        flurstueck.pruefen();
    }

    /**
     * Submits a new Nachweis and raises a domain event.
     */
    public Nachweis nachweisEinreichen(String dokumentTyp, String eingereichtVon) {
        Nachweis nachweis = new Nachweis(UUID.randomUUID(), dokumentTyp,
                eingereichtVon, LocalDateTime.now());
        this.nachweise.add(nachweis);

        domainEvents.add(new NachweisEingereicht(
                this.id, dokumentTyp, nachweis.getEingereichtAm()));
        return nachweis;
    }

    /**
     * Accepts an existing Nachweis and raises a domain event.
     */
    public void nachweisAkzeptieren(UUID nachweisId) {
        Nachweis nachweis = nachweise.stream()
                .filter(n -> n.getId().equals(nachweisId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Nachweis mit ID " + nachweisId + " nicht gefunden"));

        if (nachweis.isAkzeptiert()) {
            throw new IllegalStateException(
                    "Nachweis mit ID " + nachweisId + " ist bereits akzeptiert");
        }

        nachweis.akzeptieren();

        domainEvents.add(new NachweisAkzeptiert(
                this.id, nachweisId, LocalDateTime.now()));
    }

    /**
     * Submits the AntragsMappe.
     * Invariant: at least one Flurstueck must exist.
     *
     * @throws IllegalStateException if no Flurstueck exists
     */
    public void einreichen() {
        if (flurstuecke.isEmpty()) {
            throw new IllegalStateException(
                    "Antrag muss mindestens ein Flurstueck enthalten");
        }

        this.status = AntragStatus.EINGEREICHT;
        domainEvents.add(new InternalAntragsmappeEingereicht(this.id, LocalDateTime.now()));
    }

    // --- Getters ---

    public UUID getId() {
        return id;
    }

    public RegistrierungsNummer getRegistrierungsNummer() {
        return registrierungsNummer;
    }

    public Foerderbetrag getBeantragteFoerderung() {
        return beantragteFoerderung;
    }

    public Foerderquote getFoerderquote() {
        return foerderquote;
    }

    public AntragStatus getStatus() {
        return status;
    }

    public List<Flurstueck> getFlurstuecke() {
        return Collections.unmodifiableList(flurstuecke);
    }

    public List<Nachweis> getNachweise() {
        return Collections.unmodifiableList(nachweise);
    }

    public List<AntragEvent> getDomainEvents() {
        return List.copyOf(domainEvents);
    }

    /**
     * Clears all collected domain events (typically called after events have been published).
     */
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
}
