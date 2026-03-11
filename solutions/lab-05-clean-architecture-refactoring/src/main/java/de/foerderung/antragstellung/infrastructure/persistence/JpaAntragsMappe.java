package de.foerderung.antragstellung.infrastructure.persistence;

import de.foerderung.antragstellung.domain.model.AntragId;
import de.foerderung.antragstellung.domain.model.AntragStatus;
import de.foerderung.antragstellung.domain.model.AntragsMappe;
import de.foerderung.antragstellung.domain.model.Foerderbetrag;
import de.foerderung.antragstellung.domain.model.Foerderquote;
import de.foerderung.antragstellung.domain.model.RegistrierungsNummer;
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
 * JPA Entity representing an AntragsMappe for persistence.
 */
@Entity
@Table(name = "antragsmappe")
public class JpaAntragsMappe {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String registrierungsNummer;

    @Column(nullable = false)
    private BigDecimal foerderbetrag;

    @Column(nullable = false)
    private String waehrung;

    @Column(nullable = false)
    private BigDecimal foerderquote;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AntragStatus status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "flurstueck", joinColumns = @JoinColumn(name = "antragsmappe_id"))
    private List<JpaFlurstueck> flurstuecke = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "nachweis", joinColumns = @JoinColumn(name = "antragsmappe_id"))
    private List<JpaNachweis> nachweise = new ArrayList<>();

    protected JpaAntragsMappe() {
    }

    /**
     * Converts this JPA entity to the domain model.
     */
    public AntragsMappe toModel() {
        return AntragsMappe.rekonstruieren(
                new AntragId(this.id),
                new RegistrierungsNummer(this.registrierungsNummer),
                new Foerderbetrag(this.foerderbetrag, this.waehrung),
                new Foerderquote(this.foerderquote),
                this.status,
                this.flurstuecke.stream()
                        .map(JpaFlurstueck::toModel)
                        .collect(Collectors.toList()),
                this.nachweise.stream()
                        .map(JpaNachweis::toModel)
                        .collect(Collectors.toList())
        );
    }

    /**
     * Creates a JPA entity from the domain model.
     */
    public static JpaAntragsMappe fromModel(AntragsMappe mappe) {
        JpaAntragsMappe jpa = new JpaAntragsMappe();
        jpa.id = mappe.getId().value();
        jpa.registrierungsNummer = mappe.getRegistrierungsNummer().wert();
        jpa.foerderbetrag = mappe.getBeantragteFoerderung().betrag();
        jpa.waehrung = mappe.getBeantragteFoerderung().waehrung();
        jpa.foerderquote = mappe.getFoerderquote().prozentsatz();
        jpa.status = mappe.getStatus();
        jpa.flurstuecke = mappe.getFlurstuecke().stream()
                .map(JpaFlurstueck::fromModel)
                .collect(Collectors.toList());
        jpa.nachweise = mappe.getNachweise().stream()
                .map(JpaNachweis::fromModel)
                .collect(Collectors.toList());
        return jpa;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getRegistrierungsNummer() {
        return registrierungsNummer;
    }

    public void setRegistrierungsNummer(String registrierungsNummer) {
        this.registrierungsNummer = registrierungsNummer;
    }

    public BigDecimal getFoerderbetrag() {
        return foerderbetrag;
    }

    public void setFoerderbetrag(BigDecimal foerderbetrag) {
        this.foerderbetrag = foerderbetrag;
    }

    public String getWaehrung() {
        return waehrung;
    }

    public void setWaehrung(String waehrung) {
        this.waehrung = waehrung;
    }

    public BigDecimal getFoerderquote() {
        return foerderquote;
    }

    public void setFoerderquote(BigDecimal foerderquote) {
        this.foerderquote = foerderquote;
    }

    public AntragStatus getStatus() {
        return status;
    }

    public void setStatus(AntragStatus status) {
        this.status = status;
    }

    public List<JpaFlurstueck> getFlurstuecke() {
        return flurstuecke;
    }

    public void setFlurstuecke(List<JpaFlurstueck> flurstuecke) {
        this.flurstuecke = flurstuecke;
    }

    public List<JpaNachweis> getNachweise() {
        return nachweise;
    }

    public void setNachweise(List<JpaNachweis> nachweise) {
        this.nachweise = nachweise;
    }
}
