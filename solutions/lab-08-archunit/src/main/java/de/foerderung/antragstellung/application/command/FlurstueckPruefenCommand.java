package de.foerderung.antragstellung.application.command;

import de.foerderung.antragstellung.domain.model.AntragId;
import de.foerderung.antragstellung.domain.model.FlurstueckId;

import java.util.UUID;

/**
 * Command representing the intention to mark a Flurstueck as verified.
 */
public record FlurstueckPruefenCommand(
        AntragId antragsmappeId,
        FlurstueckId flurstueckId
) {
    /**
     * Factory method that accepts raw UUIDs, so adapter layers do not need
     * to depend on domain model types (AntragId, FlurstueckId).
     */
    public static FlurstueckPruefenCommand of(UUID antragsmappeId, UUID flurstueckId) {
        return new FlurstueckPruefenCommand(new AntragId(antragsmappeId), new FlurstueckId(flurstueckId));
    }
}
