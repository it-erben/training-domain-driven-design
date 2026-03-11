package de.foerderung.antragstellung.application.command;

import de.foerderung.antragstellung.domain.model.AntragId;
import de.foerderung.antragstellung.domain.model.FlurstueckNummer;

import java.math.BigDecimal;

/**
 * Command representing the intention to add a Flurstueck to an AntragsMappe.
 */
public record FlurstueckHinzufuegenCommand(
        AntragId antragsmappeId,
        FlurstueckNummer flurstueckNummer,
        BigDecimal flaeche
) {}
