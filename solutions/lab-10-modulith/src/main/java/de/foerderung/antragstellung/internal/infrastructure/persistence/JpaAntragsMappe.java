package de.foerderung.antragstellung.internal.infrastructure.persistence;

import de.foerderung.antragstellung.internal.domain.model.AntragsMappe;
import de.foerderung.antragstellung.internal.domain.model.AntragStatus;
import de.foerderung.antragstellung.internal.domain.model.Flurstueck;
import de.foerderung.antragstellung.internal.domain.model.Foerderbetrag;
import de.foerderung.antragstellung.internal.domain.model.Foerderquote;
import de.foerderung.antragstellung.internal.domain.model.Nachweis;
import de.foerderung.antragstellung.internal.domain.model.RegistrierungsNummer;
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

    public static JpaAntragsMappe fromModel(AntragsMappe mappe) {
        JpaAntragsMappe jpa = new JpaAntragsMappe();
        jpa.id = mappe.getId();
        jpa.registrierungsNummer = mappe.getRegistrierungsNummer().wert();
        jpa.foerderbetrag = mappe.getBeantragteFoerderung().betrag();
        jpa.waehrung = mappe.getBeantragteFoerderung().waehrung();
        jpa.foerderquote = mappe.getFoerderquote().prozentsatz();
        jpa.status = mappe.getStatus();
        jpa.flurstuecke = mappe.getFlurstuecke().stream()
                .map(JpaFlurstueck::fromModel)
                .toList();
        jpa.nachweise = mappe.getNachweise().stream()
                .map(JpaNachweis::fromModel)
                .toList();
        return jpa;
    }

    public AntragsMappe toModel() {
        List<Flurstueck> domainFlurstuecke = flurstuecke.stream()
                .map(JpaFlurstueck::toModel)
                .toList();
        List<Nachweis> domainNachweise = nachweise.stream()
                .map(JpaNachweis::toModel)
                .toList();

        return AntragsMappe.rekonstruieren(
                id,
                new RegistrierungsNummer(registrierungsNummer),
                new Foerderbetrag(foerderbetrag, waehrung),
                new Foerderquote(foerderquote),
                status,
                domainFlurstuecke,
                domainNachweise
        );
    }
}
