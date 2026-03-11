package de.foerderung.antragstellung.application.port;

import de.foerderung.antragstellung.application.command.FlurstueckHinzufuegenCommand;
import de.foerderung.antragstellung.application.command.FlurstueckHinzufuegenResult;

/**
 * Inbound port for the "Flurstueck hinzufuegen" use case.
 */
public interface FlurstueckHinzufuegen {

    FlurstueckHinzufuegenResult hinzufuegen(FlurstueckHinzufuegenCommand command);
}
