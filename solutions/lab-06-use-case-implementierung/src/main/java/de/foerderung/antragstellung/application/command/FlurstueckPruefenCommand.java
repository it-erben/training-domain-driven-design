package de.foerderung.antragstellung.application.command;

import de.foerderung.antragstellung.domain.model.AntragId;
import de.foerderung.antragstellung.domain.model.FlurstueckId;

import java.util.Objects;

/**
 * Command to mark a Flurstueck as verified within an AntragsMappe.
 */
public record FlurstueckPruefenCommand(
        AntragId antragsmappeId,
        FlurstueckId flurstueckId
) {
    public FlurstueckPruefenCommand {
        Objects.requireNonNull(antragsmappeId, "AntragsmappeId darf nicht null sein");
        Objects.requireNonNull(flurstueckId, "FlurstueckId darf nicht null sein");
    }
}
