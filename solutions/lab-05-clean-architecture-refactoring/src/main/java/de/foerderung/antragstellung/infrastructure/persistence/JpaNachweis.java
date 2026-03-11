package de.foerderung.antragstellung.infrastructure.persistence;

import de.foerderung.antragstellung.domain.model.Nachweis;
import de.foerderung.antragstellung.domain.model.NachweisId;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA Embeddable representing a Nachweis for persistence.
 */
@Embeddable
public class JpaNachweis {

    @Column(name = "nachweis_id")
    private UUID id;

    private String dokumentTyp;
    private String eingereichtVon;
    private Instant eingereichtAm;
    private boolean akzeptiert;

    protected JpaNachweis() {
    }

    public JpaNachweis(UUID id, String dokumentTyp, String eingereichtVon,
                       Instant eingereichtAm, boolean akzeptiert) {
        this.id = id;
        this.dokumentTyp = dokumentTyp;
        this.eingereichtVon = eingereichtVon;
        this.eingereichtAm = eingereichtAm;
        this.akzeptiert = akzeptiert;
    }

    public Nachweis toModel() {
        return Nachweis.rekonstruieren(new NachweisId(id), dokumentTyp, eingereichtVon,
                eingereichtAm, akzeptiert);
    }

    public static JpaNachweis fromModel(Nachweis nachweis) {
        return new JpaNachweis(
                nachweis.getId().value(),
                nachweis.getDokumentTyp(),
                nachweis.getEingereichtVon(),
                nachweis.getEingereichtAm(),
                nachweis.isAkzeptiert()
        );
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getDokumentTyp() {
        return dokumentTyp;
    }

    public void setDokumentTyp(String dokumentTyp) {
        this.dokumentTyp = dokumentTyp;
    }

    public String getEingereichtVon() {
        return eingereichtVon;
    }

    public void setEingereichtVon(String eingereichtVon) {
        this.eingereichtVon = eingereichtVon;
    }

    public Instant getEingereichtAm() {
        return eingereichtAm;
    }

    public void setEingereichtAm(Instant eingereichtAm) {
        this.eingereichtAm = eingereichtAm;
    }

    public boolean isAkzeptiert() {
        return akzeptiert;
    }

    public void setAkzeptiert(boolean akzeptiert) {
        this.akzeptiert = akzeptiert;
    }
}
