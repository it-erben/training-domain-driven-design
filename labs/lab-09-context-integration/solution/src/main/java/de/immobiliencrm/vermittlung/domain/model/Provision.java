package de.immobiliencrm.vermittlung.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object representing a commission percentage.
 */
public record Provision(BigDecimal prozentsatz) {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    public Provision {
        Objects.requireNonNull(prozentsatz, "Prozentsatz darf nicht null sein");

        if (prozentsatz.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Prozentsatz muss groesser als 0 sein");
        }
        if (prozentsatz.compareTo(HUNDRED) > 0) {
            throw new IllegalArgumentException("Prozentsatz darf nicht groesser als 100 sein");
        }
    }
}
