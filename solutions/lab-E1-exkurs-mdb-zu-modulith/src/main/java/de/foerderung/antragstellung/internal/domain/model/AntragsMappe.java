package de.foerderung.antragstellung.internal.domain.model;

import de.foerderung.antragstellung.internal.domain.event.AntragEvent;
import de.foerderung.antragstellung.internal.domain.event.FlurstueckHinzugefuegt;
import de.foerderung.antragstellung.internal.domain.event.InternalAntragsmappeEingereicht;
import de.foerderung.antragstellung.internal.domain.event.NachweisAkzeptiert;
import de.foerderung.antragstellung.internal.domain.event.NachweisEingereicht;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class AntragsMappe {

    private final AntragId id;
    private final RegistrierungsNummer registrierungsNummer;
    private final Foerderbetrag beantragteFoerderung;
    private final Foerderquote foerderquote;
    private AntragStatus status;
    private final List<Flurstueck> flurstuecke;
    private final List<Nachweis> nachweise;
    private transient final List<AntragEvent> domainEvents;

    private AntragsMappe(AntragId id, RegistrierungsNummer registrierungsNummer,
                          Foerderbetrag beantragteFoerderung, Foerderquote foerderquote) {
        this.id = Objects.requireNonNull(id, "AntragId darf nicht null sein");
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

    public static AntragsMappe erstellen(RegistrierungsNummer registrierungsNummer,
                                          Foerderbetrag beantragteFoerderung,
                                          Foerderquote foerderquote) {
        var id = AntragId.generate();
        return new AntragsMappe(id, registrierungsNummer,
                beantragteFoerderung, foerderquote);
    }

    public static AntragsMappe rekonstruieren(AntragId id,
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

    public FlurstueckId flurstueckHinzufuegen(FlurstueckNummer nummer,
                                               BigDecimal flaeche, String bemerkung) {
        var flurstueckId = FlurstueckId.generate();
        Flurstueck flurstueck = new Flurstueck(flurstueckId, nummer, flaeche, bemerkung);
        this.flurstuecke.add(flurstueck);
        this.status = AntragStatus.IN_BEARBEITUNG;

        domainEvents.add(new FlurstueckHinzugefuegt(
                this.id, flurstueckId, nummer, flaeche, Instant.now()));
        return flurstueckId;
    }

    public void flurstueckPruefen(FlurstueckId flurstueckId) {
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

    public NachweisId nachweisEinreichen(String dokumentTyp, String eingereichtVon) {
        var nachweisId = NachweisId.generate();
        Nachweis nachweis = new Nachweis(nachweisId, dokumentTyp,
                eingereichtVon, Instant.now());
        this.nachweise.add(nachweis);

        domainEvents.add(new NachweisEingereicht(
                this.id, nachweisId, dokumentTyp, Instant.now()));
        return nachweisId;
    }

    public void nachweisAkzeptieren(NachweisId nachweisId) {
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
                this.id, nachweisId, Instant.now()));
    }

    public void einreichen() {
        if (flurstuecke.isEmpty()) {
            throw new IllegalStateException(
                    "Antrag muss mindestens ein Flurstueck enthalten");
        }
        this.status = AntragStatus.EINGEREICHT;
        domainEvents.add(new InternalAntragsmappeEingereicht(this.id, Instant.now()));
    }

    public AntragId getId() { return id; }
    public RegistrierungsNummer getRegistrierungsNummer() { return registrierungsNummer; }
    public Foerderbetrag getBeantragteFoerderung() { return beantragteFoerderung; }
    public Foerderquote getFoerderquote() { return foerderquote; }
    public AntragStatus getStatus() { return status; }
    public List<Flurstueck> getFlurstuecke() { return Collections.unmodifiableList(flurstuecke); }
    public List<Nachweis> getNachweise() { return Collections.unmodifiableList(nachweise); }
    public List<AntragEvent> getDomainEvents() { return List.copyOf(domainEvents); }

    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
}
