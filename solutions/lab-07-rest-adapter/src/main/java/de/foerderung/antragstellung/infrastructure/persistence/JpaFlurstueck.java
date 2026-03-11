package de.foerderung.antragstellung.infrastructure.persistence;

import de.foerderung.antragstellung.domain.model.Flurstueck;
import de.foerderung.antragstellung.domain.model.FlurstueckId;
import de.foerderung.antragstellung.domain.model.FlurstueckNummer;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.util.UUID;

@Embeddable
public class JpaFlurstueck {

    private UUID flurstueckId;
    private String nummer;
    private BigDecimal flaeche;
    private String bemerkung;
    private boolean geprueft;

    protected JpaFlurstueck() {
    }

    public static JpaFlurstueck fromModel(Flurstueck flurstueck) {
        JpaFlurstueck jpa = new JpaFlurstueck();
        jpa.flurstueckId = flurstueck.getId().value();
        jpa.nummer = flurstueck.getNummer().wert();
        jpa.flaeche = flurstueck.getFlaeche();
        jpa.bemerkung = flurstueck.getBemerkung();
        jpa.geprueft = flurstueck.isGeprueft();
        return jpa;
    }

    public Flurstueck toModel() {
        return Flurstueck.rekonstruieren(
                new FlurstueckId(flurstueckId),
                new FlurstueckNummer(nummer),
                flaeche,
                bemerkung,
                geprueft);
    }
}
