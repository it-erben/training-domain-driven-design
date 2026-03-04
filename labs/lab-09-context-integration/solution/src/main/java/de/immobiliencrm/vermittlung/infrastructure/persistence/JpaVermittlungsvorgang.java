package de.immobiliencrm.vermittlung.infrastructure.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * JPA entity mapped to the database table for Vermittlungsvorgang.
 */
@Entity
@Table(name = "vermittlungsvorgang")
public class JpaVermittlungsvorgang {

    @Id
    private UUID id;

    private UUID immobilieId;

    private String strasse;
    private String plz;
    private String ort;

    private BigDecimal preisBetrag;
    private String preisWaehrung;

    private BigDecimal provisionProzentsatz;

    @Enumerated(EnumType.STRING)
    private String status;

    // Default constructor required by JPA
    protected JpaVermittlungsvorgang() {
    }

    public JpaVermittlungsvorgang(UUID id, UUID immobilieId, String strasse, String plz, String ort,
                                   BigDecimal preisBetrag, String preisWaehrung,
                                   BigDecimal provisionProzentsatz, String status) {
        this.id = id;
        this.immobilieId = immobilieId;
        this.strasse = strasse;
        this.plz = plz;
        this.ort = ort;
        this.preisBetrag = preisBetrag;
        this.preisWaehrung = preisWaehrung;
        this.provisionProzentsatz = provisionProzentsatz;
        this.status = status;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
