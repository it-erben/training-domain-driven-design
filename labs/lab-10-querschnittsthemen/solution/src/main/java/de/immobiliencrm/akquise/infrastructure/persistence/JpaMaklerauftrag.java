package de.immobiliencrm.akquise.infrastructure.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity mapped to the database table for Maklerauftrag.
 */
@Entity
@Table(name = "maklerauftrag")
public class JpaMaklerauftrag {

    @Id
    private UUID id;

    private UUID eigentuemerId;

    private UUID immobilieId;

    private LocalDateTime abgeschlossenAm;

    // Default constructor required by JPA
    protected JpaMaklerauftrag() {
    }

    public JpaMaklerauftrag(UUID id, UUID eigentuemerId, UUID immobilieId, LocalDateTime abgeschlossenAm) {
        this.id = id;
        this.eigentuemerId = eigentuemerId;
        this.immobilieId = immobilieId;
        this.abgeschlossenAm = abgeschlossenAm;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getEigentuemerId() {
        return eigentuemerId;
    }

    public void setEigentuemerId(UUID eigentuemerId) {
        this.eigentuemerId = eigentuemerId;
    }

    public UUID getImmobilieId() {
        return immobilieId;
    }

    public void setImmobilieId(UUID immobilieId) {
        this.immobilieId = immobilieId;
    }

    public LocalDateTime getAbgeschlossenAm() {
        return abgeschlossenAm;
    }

    public void setAbgeschlossenAm(LocalDateTime abgeschlossenAm) {
        this.abgeschlossenAm = abgeschlossenAm;
    }
}
