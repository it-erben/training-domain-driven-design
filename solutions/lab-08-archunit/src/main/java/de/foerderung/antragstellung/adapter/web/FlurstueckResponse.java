package de.foerderung.antragstellung.adapter.web;

import java.math.BigDecimal;
import java.util.UUID;

import de.foerderung.antragstellung.application.service.FlurstueckeAbfragenUseCase.FlurstueckInfo;

public record FlurstueckResponse(
        UUID id,
        String nummer,
        BigDecimal flaeche,
        String bemerkung,
        boolean geprueft
) {
    public static FlurstueckResponse from(FlurstueckInfo info) {
        return new FlurstueckResponse(
                info.id(),
                info.nummer(),
                info.flaeche(),
                info.bemerkung(),
                info.geprueft());
    }
}
