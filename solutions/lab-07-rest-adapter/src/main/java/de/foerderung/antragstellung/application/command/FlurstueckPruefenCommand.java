package de.foerderung.antragstellung.application.command;

import de.foerderung.antragstellung.domain.model.AntragId;
import de.foerderung.antragstellung.domain.model.FlurstueckId;

/**
 * Command representing the intention to mark a Flurstueck as verified.
 */
public record FlurstueckPruefenCommand(
        AntragId antragsmappeId,
        FlurstueckId flurstueckId
) {}
