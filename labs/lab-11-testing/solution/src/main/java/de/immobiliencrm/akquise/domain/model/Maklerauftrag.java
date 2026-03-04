package de.immobiliencrm.akquise.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing a broker mandate within the acquisition bounded context.
 */
public class Maklerauftrag {

    private final UUID id;
    private final UUID eigentuemerId;
    private final UUID immobilieId;
    private LocalDateTime abgeschlossenAm;

    private Maklerauftrag(UUID id, UUID eigentuemerId, UUID immobilieId) {
        this.id = Objects.requireNonNull(id, "ID darf nicht null sein");
        this.eigentuemerId = Objects.requireNonNull(eigentuemerId, "EigentuemerId darf nicht null sein");
        this.immobilieId = Objects.requireNonNull(immobilieId, "ImmobilieId darf nicht null sein");
    }

    /**
     * Factory method to create a new broker mandate.
     */
    public static Maklerauftrag erstellen(UUID eigentuemerId, UUID immobilieId) {
        return new Maklerauftrag(UUID.randomUUID(), eigentuemerId, immobilieId);
    }

    /**
     * Reconstitution constructor for loading from persistence.
     */
    public static Maklerauftrag reconstitute(UUID id, UUID eigentuemerId, UUID immobilieId,
                                              LocalDateTime abgeschlossenAm) {
        Maklerauftrag auftrag = new Maklerauftrag(id, eigentuemerId, immobilieId);
        auftrag.abgeschlossenAm = abgeschlossenAm;
        return auftrag;
    }

    /**
     * Finalizes this broker mandate by setting the completion timestamp.
     */
    public void abschliessen() {
        this.abgeschlossenAm = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getEigentuemerId() {
        return eigentuemerId;
    }

    public UUID getImmobilieId() {
        return immobilieId;
    }

    public LocalDateTime getAbgeschlossenAm() {
        return abgeschlossenAm;
    }
}
