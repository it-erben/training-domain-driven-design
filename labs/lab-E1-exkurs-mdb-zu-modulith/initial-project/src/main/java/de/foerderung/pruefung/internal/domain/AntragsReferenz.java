package de.foerderung.pruefung.internal.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object representing a reference to an AntragsMappe from the Antragstellung context.
 * Part of the Anti-Corruption Layer: the Pruefung context does not depend on the
 * Antragstellung domain model directly.
 */
public record AntragsReferenz(UUID antragsmappeId) {

    public AntragsReferenz {
        Objects.requireNonNull(antragsmappeId, "AntragsmappeId darf nicht null sein");
    }
}
