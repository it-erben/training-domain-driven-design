package de.foerderung.antragstellung.internal.domain.model;

import java.util.Objects;
import java.util.UUID;

public record FlurstueckId(UUID value) {

    public FlurstueckId {
        Objects.requireNonNull(value, "FlurstueckId darf nicht null sein");
    }

    public static FlurstueckId generate() {
        return new FlurstueckId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
