package de.foerderung.antragstellung.adapter.web;

import de.foerderung.antragstellung.application.command.FlurstueckHinzufuegenCommand;
import de.foerderung.antragstellung.domain.model.FlurstueckNummer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record FlurstueckHinzufuegenRequest(
    @NotBlank String flurstueckNummer,
    @NotNull @Positive BigDecimal flaeche
) {
    public FlurstueckHinzufuegenCommand toCommand(UUID antragsmappeId) {
        return new FlurstueckHinzufuegenCommand(
            antragsmappeId, new FlurstueckNummer(flurstueckNummer), flaeche);
    }
}
