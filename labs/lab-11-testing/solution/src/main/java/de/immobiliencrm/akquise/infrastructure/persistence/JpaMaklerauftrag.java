package de.immobiliencrm.akquise.infrastructure.persistence;

import de.immobiliencrm.akquise.domain.model.Maklerauftrag;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity for persisting the Maklerauftrag aggregate.
 */
@Entity
@Table(name = "maklerauftrag")
public class JpaMaklerauftrag {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID eigentuemerId;

    @Column(nullable = false)
    private UUID immobilieId;

    private LocalDateTime abgeschlossenAm;

    protected JpaMaklerauftrag() {
        // Required by JPA
    }

    public static JpaMaklerauftrag fromModel(Maklerauftrag auftrag) {
        JpaMaklerauftrag jpa = new JpaMaklerauftrag();
        jpa.id = auftrag.getId();
        jpa.eigentuemerId = auftrag.getEigentuemerId();
        jpa.immobilieId = auftrag.getImmobilieId();
        jpa.abgeschlossenAm = auftrag.getAbgeschlossenAm();
        return jpa;
    }

    public Maklerauftrag toModel() {
        return Maklerauftrag.reconstitute(id, eigentuemerId, immobilieId, abgeschlossenAm);
    }
}
