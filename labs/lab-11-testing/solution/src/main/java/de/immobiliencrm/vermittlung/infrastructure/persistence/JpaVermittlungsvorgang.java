package de.immobiliencrm.vermittlung.infrastructure.persistence;

import de.immobiliencrm.vermittlung.domain.model.Adresse;
import de.immobiliencrm.vermittlung.domain.model.Angebot;
import de.immobiliencrm.vermittlung.domain.model.Besichtigung;
import de.immobiliencrm.vermittlung.domain.model.Preisvorstellung;
import de.immobiliencrm.vermittlung.domain.model.Provision;
import de.immobiliencrm.vermittlung.domain.model.Vermittlungsvorgang;
import de.immobiliencrm.vermittlung.domain.model.VermittlungsvorgangStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA entity for persisting the Vermittlungsvorgang aggregate.
 */
@Entity
@Table(name = "vermittlungsvorgang")
public class JpaVermittlungsvorgang {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID immobilieId;

    @Column(nullable = false)
    private String strasse;

    @Column(nullable = false)
    private String plz;

    @Column(nullable = false)
    private String ort;

    @Column(nullable = false)
    private BigDecimal preisBetrag;

    @Column(nullable = false)
    private String preisWaehrung;

    @Column(nullable = false)
    private BigDecimal provisionProzentsatz;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VermittlungsvorgangStatus status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "besichtigung", joinColumns = @JoinColumn(name = "vorgang_id"))
    private List<JpaBesichtigung> besichtigungen = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "angebot", joinColumns = @JoinColumn(name = "vorgang_id"))
    private List<JpaAngebot> angebote = new ArrayList<>();

    protected JpaVermittlungsvorgang() {
        // Required by JPA
    }

    /**
     * Converts a domain Vermittlungsvorgang to its JPA representation.
     */
    public static JpaVermittlungsvorgang fromModel(Vermittlungsvorgang vorgang) {
        JpaVermittlungsvorgang jpa = new JpaVermittlungsvorgang();
        jpa.id = vorgang.getId();
        jpa.immobilieId = vorgang.getImmobilieId();
        jpa.strasse = vorgang.getAdresse().strasse();
        jpa.plz = vorgang.getAdresse().plz();
        jpa.ort = vorgang.getAdresse().ort();
        jpa.preisBetrag = vorgang.getPreisvorstellung().betrag();
        jpa.preisWaehrung = vorgang.getPreisvorstellung().waehrung();
        jpa.provisionProzentsatz = vorgang.getProvision().prozentsatz();
        jpa.status = vorgang.getStatus();
        jpa.besichtigungen = vorgang.getBesichtigungen().stream()
                .map(JpaBesichtigung::fromModel)
                .toList();
        jpa.angebote = vorgang.getAngebote().stream()
                .map(JpaAngebot::fromModel)
                .toList();
        return jpa;
    }

    /**
     * Converts this JPA representation back to a domain Vermittlungsvorgang.
     */
    public Vermittlungsvorgang toModel() {
        List<Besichtigung> domainBesichtigungen = besichtigungen.stream()
                .map(JpaBesichtigung::toModel)
                .toList();
        List<Angebot> domainAngebote = angebote.stream()
                .map(JpaAngebot::toModel)
                .toList();

        return Vermittlungsvorgang.reconstitute(
                id,
                immobilieId,
                new Adresse(strasse, plz, ort),
                new Preisvorstellung(preisBetrag, preisWaehrung),
                new Provision(provisionProzentsatz),
                status,
                domainBesichtigungen,
                domainAngebote
        );
    }
}
