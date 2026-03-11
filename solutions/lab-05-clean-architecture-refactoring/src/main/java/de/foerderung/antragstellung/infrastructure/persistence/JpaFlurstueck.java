package de.foerderung.antragstellung.infrastructure.persistence;

import de.foerderung.antragstellung.domain.model.Flurstueck;
import de.foerderung.antragstellung.domain.model.FlurstueckId;
import de.foerderung.antragstellung.domain.model.FlurstueckNummer;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * JPA Embeddable representing a Flurstueck for persistence.
 */
@Embeddable
public class JpaFlurstueck {

    @Column(name = "flurstueck_id")
    private UUID id;

    private String nummer;
    private BigDecimal flaeche;
    private String bemerkung;
    private boolean geprueft;

    protected JpaFlurstueck() {
    }

    public JpaFlurstueck(UUID id, String nummer, BigDecimal flaeche,
                         String bemerkung, boolean geprueft) {
        this.id = id;
        this.nummer = nummer;
        this.flaeche = flaeche;
        this.bemerkung = bemerkung;
        this.geprueft = geprueft;
    }

    public Flurstueck toModel() {
        return Flurstueck.rekonstruieren(new FlurstueckId(id), new FlurstueckNummer(nummer),
                flaeche, bemerkung, geprueft);
    }

    public static JpaFlurstueck fromModel(Flurstueck flurstueck) {
        return new JpaFlurstueck(
                flurstueck.getId().value(),
                flurstueck.getNummer().wert(),
                flurstueck.getFlaeche(),
                flurstueck.getBemerkung(),
                flurstueck.isGeprueft()
        );
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNummer() {
        return nummer;
    }

    public void setNummer(String nummer) {
        this.nummer = nummer;
    }

    public BigDecimal getFlaeche() {
        return flaeche;
    }

    public void setFlaeche(BigDecimal flaeche) {
        this.flaeche = flaeche;
    }

    public String getBemerkung() {
        return bemerkung;
    }

    public void setBemerkung(String bemerkung) {
        this.bemerkung = bemerkung;
    }

    public boolean isGeprueft() {
        return geprueft;
    }

    public void setGeprueft(boolean geprueft) {
        this.geprueft = geprueft;
    }
}
