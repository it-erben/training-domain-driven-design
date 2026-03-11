package de.foerderung.antragstellung.internal.domain.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing a cadastral parcel (Flurstueck) within the AntragsMappe aggregate.
 */
public class Flurstueck {

    private final UUID id;
    private final FlurstueckNummer nummer;
    private final BigDecimal flaeche;
    private final String bemerkung;
    private boolean geprueft;

    Flurstueck(UUID id, FlurstueckNummer nummer, BigDecimal flaeche, String bemerkung) {
        this.id = Objects.requireNonNull(id, "ID darf nicht null sein");
        this.nummer = Objects.requireNonNull(nummer, "FlurstueckNummer darf nicht null sein");
        this.flaeche = Objects.requireNonNull(flaeche, "Flaeche darf nicht null sein");
        this.bemerkung = bemerkung;
        this.geprueft = false;

        if (flaeche.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Flaeche muss groesser als 0 sein");
        }
    }

    public static Flurstueck rekonstruieren(UUID id, FlurstueckNummer nummer,
                                             BigDecimal flaeche, String bemerkung,
                                             boolean geprueft) {
        Flurstueck flurstueck = new Flurstueck(id, nummer, flaeche, bemerkung);
        flurstueck.geprueft = geprueft;
        return flurstueck;
    }

    void pruefen() {
        this.geprueft = true;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Flurstueck flurstueck)) {
            return false;
        }
        return id.equals(flurstueck.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public UUID getId() {
        return id;
    }

    public FlurstueckNummer getNummer() {
        return nummer;
    }

    public BigDecimal getFlaeche() {
        return flaeche;
    }

    public String getBemerkung() {
        return bemerkung;
    }

    public boolean isGeprueft() {
        return geprueft;
    }
}
