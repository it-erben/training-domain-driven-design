package de.foerderung.antragstellung.application.command;

import de.foerderung.antragstellung.domain.model.AntragId;
import de.foerderung.antragstellung.domain.model.FlurstueckNummer;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Command to add a Flurstueck to an existing AntragsMappe.
 */
public record FlurstueckHinzufuegenCommand(
        AntragId antragsmappeId,
        FlurstueckNummer flurstueckNummer,
        BigDecimal flaeche
) {
    public FlurstueckHinzufuegenCommand {
        Objects.requireNonNull(antragsmappeId, "AntragsmappeId darf nicht null sein");
        Objects.requireNonNull(flurstueckNummer, "Flurstuecknummer ist erforderlich");
    }
}
