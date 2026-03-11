package de.foerderung.antragstellung.adapter.web;

import de.foerderung.antragstellung.domain.model.Flurstueck;

import java.math.BigDecimal;
import java.util.UUID;

public record FlurstueckResponse(UUID id, String nummer, BigDecimal flaeche, String bemerkung, boolean geprueft) {

    public static FlurstueckResponse from(Flurstueck f) {
        return new FlurstueckResponse(f.getId().value(), f.getNummer().wert(), f.getFlaeche(), f.getBemerkung(), f.isGeprueft());
    }
}
