package de.foerderung.antragstellung.internal.domain.model;

import java.util.Objects;

public record FlurstueckNummer(String wert) {

    public FlurstueckNummer {
        Objects.requireNonNull(wert, "FlurstueckNummer darf nicht null sein");
        if (wert.isBlank()) {
            throw new IllegalArgumentException("FlurstueckNummer darf nicht leer sein");
        }
    }
}
