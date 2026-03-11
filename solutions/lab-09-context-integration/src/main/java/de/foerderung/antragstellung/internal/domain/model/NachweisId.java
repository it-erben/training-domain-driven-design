package de.foerderung.antragstellung.internal.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Typed Identity for the Nachweis entity.
 */
public record NachweisId(UUID value) {

    public NachweisId {
        Objects.requireNonNull(value, "NachweisId darf nicht null sein");
    }

    public static NachweisId generate() {
        return new NachweisId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
