package de.foerderung.antragstellung.application.port;

import de.foerderung.antragstellung.application.command.FlurstueckPruefenCommand;

/**
 * Inbound port for the "Flurstueck pruefen" use case.
 */
public interface FlurstueckPruefen {

    void pruefen(FlurstueckPruefenCommand command);
}
