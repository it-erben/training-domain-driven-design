package de.foerderung.pruefung.internal.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object representing the unique identity of a Pruefvorgang.
 */
public record PruefvorgangId(UUID wert) {

    public PruefvorgangId {
        Objects.requireNonNull(wert, "PruefvorgangId darf nicht null sein");
    }

    public static PruefvorgangId generate() {
        return new PruefvorgangId(UUID.randomUUID());
    }
}
