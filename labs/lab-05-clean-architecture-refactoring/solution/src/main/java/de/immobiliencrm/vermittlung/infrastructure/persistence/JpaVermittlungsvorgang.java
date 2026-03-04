package de.immobiliencrm.vermittlung.infrastructure.persistence;

import de.immobiliencrm.vermittlung.domain.model.Adresse;
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
import java.util.stream.Collectors;

/**
 * JPA Entity representing a Vermittlungsvorgang for persistence.
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
    @CollectionTable(name = "besichtigungen", joinColumns = @JoinColumn(name = "vermittlungsvorgang_id"))
    private List<JpaBesichtigung> besichtigungen = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "angebote", joinColumns = @JoinColumn(name = "vermittlungsvorgang_id"))
    private List<JpaAngebot> angebote = new ArrayList<>();

    protected JpaVermittlungsvorgang() {
    }

    /**
     * Converts this JPA entity to the domain model.
     */
    public Vermittlungsvorgang toModel() {
        return new Vermittlungsvorgang(
                this.id,
                this.immobilieId,
                new Adresse(this.strasse, this.plz, this.ort),
                new Preisvorstellung(this.preisBetrag, this.preisWaehrung),
                new Provision(this.provisionProzentsatz),
                this.status,
                this.besichtigungen.stream()
                        .map(JpaBesichtigung::toModel)
                        .collect(Collectors.toList()),
                this.angebote.stream()
                        .map(JpaAngebot::toModel)
                        .collect(Collectors.toList())
        );
    }

    /**
     * Creates a JPA entity from the domain model.
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
                .collect(Collectors.toList());
        jpa.angebote = vorgang.getAngebote().stream()
                .map(JpaAngebot::fromModel)
                .collect(Collectors.toList());
        return jpa;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getImmobilieId() {
        return immobilieId;
    }

    public void setImmobilieId(UUID immobilieId) {
        this.immobilieId = immobilieId;
    }

    public String getStrasse() {
        return strasse;
    }

    public void setStrasse(String strasse) {
        this.strasse = strasse;
    }

    public String getPlz() {
        return plz;
    }

    public void setPlz(String plz) {
        this.plz = plz;
    }

    public String getOrt() {
        return ort;
    }

    public void setOrt(String ort) {
        this.ort = ort;
    }

    public BigDecimal getPreisBetrag() {
        return preisBetrag;
    }

    public void setPreisBetrag(BigDecimal preisBetrag) {
        this.preisBetrag = preisBetrag;
    }

    public String getPreisWaehrung() {
        return preisWaehrung;
    }

    public void setPreisWaehrung(String preisWaehrung) {
        this.preisWaehrung = preisWaehrung;
    }

    public BigDecimal getProvisionProzentsatz() {
        return provisionProzentsatz;
    }

    public void setProvisionProzentsatz(BigDecimal provisionProzentsatz) {
        this.provisionProzentsatz = provisionProzentsatz;
    }

    public VermittlungsvorgangStatus getStatus() {
        return status;
    }

    public void setStatus(VermittlungsvorgangStatus status) {
        this.status = status;
    }

    public List<JpaBesichtigung> getBesichtigungen() {
        return besichtigungen;
    }

    public void setBesichtigungen(List<JpaBesichtigung> besichtigungen) {
        this.besichtigungen = besichtigungen;
    }

    public List<JpaAngebot> getAngebote() {
        return angebote;
    }

    public void setAngebote(List<JpaAngebot> angebote) {
        this.angebote = angebote;
    }
}
