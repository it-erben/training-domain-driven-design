package de.foerderung.antragstellung.application.command;

import de.foerderung.antragstellung.domain.model.AntragId;
import de.foerderung.antragstellung.domain.model.FlurstueckId;
import de.foerderung.antragstellung.domain.model.FlurstueckNummer;

import java.math.BigDecimal;

/**
 * Result returned after successfully adding a Flurstueck to an AntragsMappe.
 */
public record FlurstueckHinzufuegenResult(
        FlurstueckId flurstueckId,
        AntragId antragsmappeId,
        FlurstueckNummer flurstueckNummer,
        BigDecimal flaeche
) {}
