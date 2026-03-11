package de.foerderung.pruefung.internal.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * Aggregate Root for the Pruefvorgang (review process).
 * Created when an AntragsMappe is submitted in the Antragstellung context.
 */
public class Pruefvorgang {

    private final PruefvorgangId id;
    private final AntragsReferenz antragsReferenz;
    private final RegistrierungsNummer registrierungsNummer;
    private final Instant eingereichtAm;
    private PruefvorgangStatus status;

    private Pruefvorgang(PruefvorgangId id, AntragsReferenz antragsReferenz,
                          RegistrierungsNummer registrierungsNummer, Instant eingereichtAm,
                          PruefvorgangStatus status) {
        this.id = Objects.requireNonNull(id, "PruefvorgangId darf nicht null sein");
        this.antragsReferenz = Objects.requireNonNull(antragsReferenz,
                "AntragsReferenz darf nicht null sein");
        this.registrierungsNummer = Objects.requireNonNull(registrierungsNummer,
                "RegistrierungsNummer darf nicht null sein");
        this.eingereichtAm = Objects.requireNonNull(eingereichtAm,
                "EingereichtAm darf nicht null sein");
        this.status = Objects.requireNonNull(status, "Status darf nicht null sein");
    }

    /**
     * Factory method to start a new Pruefvorgang.
     */
    public static Pruefvorgang starten(PruefvorgangId id, AntragsReferenz antragsReferenz,
                                        RegistrierungsNummer registrierungsNummer,
                                        Instant eingereichtAm) {
        return new Pruefvorgang(id, antragsReferenz, registrierungsNummer,
                eingereichtAm, PruefvorgangStatus.NEU);
    }

    /**
     * Factory method to reconstruct a Pruefvorgang from persistence.
     */
    public static Pruefvorgang rekonstruieren(PruefvorgangId id, AntragsReferenz antragsReferenz,
                                               RegistrierungsNummer registrierungsNummer,
                                               Instant eingereichtAm,
                                               PruefvorgangStatus status) {
        return new Pruefvorgang(id, antragsReferenz, registrierungsNummer,
                eingereichtAm, status);
    }

    // --- Getters ---

    public PruefvorgangId getId() {
        return id;
    }

    public AntragsReferenz getAntragsReferenz() {
        return antragsReferenz;
    }

    public RegistrierungsNummer getRegistrierungsNummer() {
        return registrierungsNummer;
    }

    public Instant getEingereichtAm() {
        return eingereichtAm;
    }

    public PruefvorgangStatus getStatus() {
        return status;
    }
}
