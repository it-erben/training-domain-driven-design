package de.foerderung.antragstellung.internal.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public record Foerderbetrag(BigDecimal betrag, String waehrung) {

    public Foerderbetrag {
        Objects.requireNonNull(betrag, "Betrag muss angegeben werden");
        Objects.requireNonNull(waehrung, "Waehrung muss angegeben werden");

        if (betrag.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                "Foerderbetrag darf nicht negativ sein: " + betrag);
        }
        if (waehrung.isBlank()) {
            throw new IllegalArgumentException("Waehrung darf nicht leer sein");
        }
    }
}
