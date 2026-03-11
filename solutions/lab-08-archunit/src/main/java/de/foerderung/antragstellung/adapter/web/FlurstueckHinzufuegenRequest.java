package de.foerderung.antragstellung.adapter.web;

import java.math.BigDecimal;
import java.util.UUID;

import de.foerderung.antragstellung.application.command.FlurstueckHinzufuegenCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record FlurstueckHinzufuegenRequest(
        @NotBlank String flurstueckNummer,
        @NotNull @Positive BigDecimal flaeche,
        String bemerkung
) {
    public FlurstueckHinzufuegenCommand toCommand(UUID antragsmappeId) {
        return new FlurstueckHinzufuegenCommand(antragsmappeId, flurstueckNummer, flaeche, bemerkung);
    }
}
