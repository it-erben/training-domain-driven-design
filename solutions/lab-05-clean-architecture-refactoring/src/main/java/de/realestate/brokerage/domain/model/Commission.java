package de.realestate.brokerage.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object representing a commission percentage.
 */
public record Commission(BigDecimal percentage) {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    public Commission {
        Objects.requireNonNull(percentage, "Percentage must not be null");

        if (percentage.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Percentage must be greater than 0");
        }
        if (percentage.compareTo(HUNDRED) > 0) {
            throw new IllegalArgumentException("Percentage must not be greater than 100");
        }

        percentage = percentage.stripTrailingZeros();
    }
}
