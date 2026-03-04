package de.immobiliencrm.akquise.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing a broker contract in the Akquise bounded context.
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
     * Factory method to create a new Maklerauftrag.
     */
    public static Maklerauftrag erstellen(UUID eigentuemerId, UUID immobilieId) {
        return new Maklerauftrag(UUID.randomUUID(), eigentuemerId, immobilieId);
    }

    /**
     * Marks the contract as concluded by setting the completion timestamp.
     */
    public void abschliessen() {
        if (this.abgeschlossenAm != null) {
            throw new IllegalStateException("Maklerauftrag ist bereits abgeschlossen");
        }
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

    public boolean isAbgeschlossen() {
        return abgeschlossenAm != null;
    }
}
