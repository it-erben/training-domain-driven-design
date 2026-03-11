package de.foerderung.pruefung.internal.domain;

import java.time.Instant;
import java.util.Objects;

public class Pruefvorgang {

    private final PruefvorgangId id;
    private final AntragsReferenz antragsReferenz;
    private final RegistrierungsNummer registrierungsNummer;
    private final Instant eingereichtAm;
    private PruefvorgangStatus status;

    private Pruefvorgang(PruefvorgangId id, AntragsReferenz antragsReferenz,
                          RegistrierungsNummer registrierungsNummer, Instant eingereichtAm,
                          PruefvorgangStatus status) {
        this.id = Objects.requireNonNull(id);
        this.antragsReferenz = Objects.requireNonNull(antragsReferenz);
        this.registrierungsNummer = Objects.requireNonNull(registrierungsNummer);
        this.eingereichtAm = Objects.requireNonNull(eingereichtAm);
        this.status = Objects.requireNonNull(status);
    }

    public static Pruefvorgang starten(PruefvorgangId id, AntragsReferenz antragsReferenz,
                                        RegistrierungsNummer registrierungsNummer,
                                        Instant eingereichtAm) {
        return new Pruefvorgang(id, antragsReferenz, registrierungsNummer,
                eingereichtAm, PruefvorgangStatus.NEU);
    }

    public static Pruefvorgang rekonstruieren(PruefvorgangId id, AntragsReferenz antragsReferenz,
                                               RegistrierungsNummer registrierungsNummer,
                                               Instant eingereichtAm,
                                               PruefvorgangStatus status) {
        return new Pruefvorgang(id, antragsReferenz, registrierungsNummer,
                eingereichtAm, status);
    }

    public PruefvorgangId getId() { return id; }
    public AntragsReferenz getAntragsReferenz() { return antragsReferenz; }
    public RegistrierungsNummer getRegistrierungsNummer() { return registrierungsNummer; }
    public Instant getEingereichtAm() { return eingereichtAm; }
    public PruefvorgangStatus getStatus() { return status; }
}
