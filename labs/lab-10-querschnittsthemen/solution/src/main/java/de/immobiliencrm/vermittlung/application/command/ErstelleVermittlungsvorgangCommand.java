package de.immobiliencrm.vermittlung.application.command;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Command to create a new brokerage process.
 */
public record ErstelleVermittlungsvorgangCommand(
        UUID immobilieId,
        String strasse,
        String plz,
        String ort,
        BigDecimal preisBetrag,
        String preisWaehrung,
        BigDecimal provisionProzentsatz
) {}
