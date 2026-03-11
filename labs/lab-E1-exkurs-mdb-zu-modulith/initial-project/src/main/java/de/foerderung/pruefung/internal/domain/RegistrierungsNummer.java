package de.foerderung.pruefung.internal.domain;

import java.util.Objects;

/**
 * Value Object representing a registration number within the Pruefung context.
 * This is a separate type from the Antragstellung context's RegistrierungsNummer
 * to maintain bounded context independence.
 */
public record RegistrierungsNummer(String wert) {

    public RegistrierungsNummer {
        Objects.requireNonNull(wert, "RegistrierungsNummer darf nicht null sein");
        if (wert.isBlank()) {
            throw new IllegalArgumentException("RegistrierungsNummer darf nicht leer sein");
        }
    }
}
