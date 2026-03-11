package de.foerderung.antragstellung.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Typed Identity for the AntragsMappe aggregate root.
 */
public record AntragId(UUID value) {

    public AntragId {
        Objects.requireNonNull(value, "AntragId darf nicht null sein");
    }

    public static AntragId generate() {
        return new AntragId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
