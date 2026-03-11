package de.foerderung.auswertung.internal.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Aggregate Root for Monitoring entries in the Auswertung bounded context.
 * Replaces the legacy OptimusPrime.synchronisiere() pattern.
 */
public class MonitoringEintrag {

    private final MonitoringEintragId id;
    private final AntragsReferenz antragsReferenz;
    private MonitoringsStatus status;
    private final Instant erfasstAm;
    private Instant zuletztGeaendertAm;

    private MonitoringEintrag(MonitoringEintragId id, AntragsReferenz antragsReferenz,
                               MonitoringsStatus status, Instant erfasstAm,
                               Instant zuletztGeaendertAm) {
        this.id = Objects.requireNonNull(id, "MonitoringEintragId darf nicht null sein");
        this.antragsReferenz = Objects.requireNonNull(antragsReferenz,
                "AntragsReferenz darf nicht null sein");
        this.status = Objects.requireNonNull(status, "Status darf nicht null sein");
        this.erfasstAm = Objects.requireNonNull(erfasstAm, "ErfasstAm darf nicht null sein");
        this.zuletztGeaendertAm = Objects.requireNonNull(zuletztGeaendertAm,
                "ZuletztGeaendertAm darf nicht null sein");
    }

    /**
     * Factory method to create a new MonitoringEintrag.
     */
    public static MonitoringEintrag erstellen(MonitoringEintragId id,
                                               AntragsReferenz antragsReferenz,
                                               MonitoringsStatus status,
                                               Instant geaendertAm) {
        return new MonitoringEintrag(id, antragsReferenz, status, geaendertAm, geaendertAm);
    }

    /**
     * Factory method to reconstruct from persistence.
     */
    public static MonitoringEintrag rekonstruieren(MonitoringEintragId id,
                                                    AntragsReferenz antragsReferenz,
                                                    MonitoringsStatus status,
                                                    Instant erfasstAm,
                                                    Instant zuletztGeaendertAm) {
        return new MonitoringEintrag(id, antragsReferenz, status, erfasstAm, zuletztGeaendertAm);
    }

    /**
     * Updates the status and timestamp. Used for idempotent event processing.
     */
    public void aktualisiere(MonitoringsStatus neuerStatus, Instant geaendertAm) {
        this.status = Objects.requireNonNull(neuerStatus);
        this.zuletztGeaendertAm = Objects.requireNonNull(geaendertAm);
    }

    public MonitoringEintragId getId() { return id; }
    public AntragsReferenz getAntragsReferenz() { return antragsReferenz; }
    public MonitoringsStatus getStatus() { return status; }
    public Instant getErfasstAm() { return erfasstAm; }
    public Instant getZuletztGeaendertAm() { return zuletztGeaendertAm; }
}
