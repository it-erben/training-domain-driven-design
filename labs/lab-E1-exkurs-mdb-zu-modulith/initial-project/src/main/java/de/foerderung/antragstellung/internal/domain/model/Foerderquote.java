package de.foerderung.antragstellung.internal.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object representing the funding quota as a decimal factor (0.0 - 1.0).
 */
public record Foerderquote(BigDecimal prozentsatz) {

    public Foerderquote {
        Objects.requireNonNull(prozentsatz, "Prozentsatz muss angegeben werden");

        if (prozentsatz.compareTo(BigDecimal.ZERO) < 0
                || prozentsatz.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException(
                "Foerderquote muss zwischen 0 und 1 liegen: " + prozentsatz);
        }
    }
}
