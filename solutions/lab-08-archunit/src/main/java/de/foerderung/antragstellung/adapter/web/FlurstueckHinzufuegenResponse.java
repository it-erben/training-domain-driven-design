package de.foerderung.antragstellung.adapter.web;

import java.util.UUID;

import de.foerderung.antragstellung.application.command.FlurstueckHinzufuegenResult;

public record FlurstueckHinzufuegenResponse(
        UUID flurstueckId,
        UUID antragsmappeId,
        String flurstueckNummer,
        String status
) {
    public static FlurstueckHinzufuegenResponse from(FlurstueckHinzufuegenResult result) {
        return new FlurstueckHinzufuegenResponse(
                result.flurstueckId().value(),
                result.antragsmappeId().value(),
                result.flurstueckNummer().wert(),
                "IN_BEARBEITUNG");
    }
}
