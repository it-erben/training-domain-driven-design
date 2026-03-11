package de.foerderung.antragstellung.adapter.web;

import de.foerderung.antragstellung.application.command.FlurstueckHinzufuegenResult;

import java.util.UUID;

public record FlurstueckHinzufuegenResponse(UUID flurstueckId, UUID antragsmappeId) {

    public static FlurstueckHinzufuegenResponse from(FlurstueckHinzufuegenResult result) {
        return new FlurstueckHinzufuegenResponse(result.flurstueckId().value(), result.antragsmappeId().value());
    }
}
