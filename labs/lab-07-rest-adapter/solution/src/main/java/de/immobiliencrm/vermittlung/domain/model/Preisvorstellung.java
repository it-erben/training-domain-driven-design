package de.immobiliencrm.vermittlung.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object representing a price expectation for a property.
 */
public record Preisvorstellung(BigDecimal betrag, String waehrung) {

    public Preisvorstellung {
        Objects.requireNonNull(betrag, "Betrag darf nicht null sein");
        Objects.requireNonNull(waehrung, "Waehrung darf nicht null sein");

        if (betrag.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Betrag muss groesser als 0 sein");
        }
        if (waehrung.isBlank()) {
            throw new IllegalArgumentException("Waehrung darf nicht leer sein");
        }
    }
}
