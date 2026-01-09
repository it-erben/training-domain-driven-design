package de.realestate.brokerage.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object representing a price expectation.
 */
public record AskingPrice(BigDecimal amount, String currency) {

    public AskingPrice {
        Objects.requireNonNull(amount, "Amount must not be null");
        Objects.requireNonNull(currency, "Currency must not be null");

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
        if (currency.isBlank()) {
            throw new IllegalArgumentException("Currency must not be blank");
        }

        amount = amount.stripTrailingZeros();
    }
}
