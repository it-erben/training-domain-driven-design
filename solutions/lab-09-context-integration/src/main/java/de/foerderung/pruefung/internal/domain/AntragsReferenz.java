package de.foerderung.pruefung.internal.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object representing a reference to an AntragsMappe from the Antragstellung context.
 * This is the Pruefung context's own representation - it does NOT depend on the Antragstellung domain model.
 */
public record AntragsReferenz(UUID value) {

    public AntragsReferenz {
        Objects.requireNonNull(value, "AntragsReferenz darf nicht null sein");
    }
}
