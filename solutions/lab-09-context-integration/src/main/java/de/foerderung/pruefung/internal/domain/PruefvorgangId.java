package de.foerderung.pruefung.internal.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Typed Identity for the Pruefvorgang aggregate root.
 */
public record PruefvorgangId(UUID value) {

    public PruefvorgangId {
        Objects.requireNonNull(value, "PruefvorgangId darf nicht null sein");
    }

    public static PruefvorgangId generate() {
        return new PruefvorgangId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
