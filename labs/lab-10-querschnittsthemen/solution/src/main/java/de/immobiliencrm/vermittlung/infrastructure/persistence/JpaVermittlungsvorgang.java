package de.immobiliencrm.vermittlung.infrastructure.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity mapped to the database table for Vermittlungsvorgang.
 * Includes optimistic locking via @Version and JPA auditing fields.
 */
@Entity
@Table(name = "vermittlungsvorgang")
@EntityListeners(AuditingEntityListener.class)
public class JpaVermittlungsvorgang {

    @Id
    private UUID id;

    @Version
    private Long version;

    private UUID immobilieId;

    private String strasse;
    private String plz;
    private String ort;

    private BigDecimal preisBetrag;
    private String preisWaehrung;

    private BigDecimal provisionProzentsatz;

    @Enumerated(EnumType.STRING)
    private String status;

    @CreatedDate
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime lastModifiedDate;

    @CreatedBy
    private String createdBy;

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

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
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

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
