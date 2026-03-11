package de.foerderung.antragstellung.internal.domain.model;

import java.util.Objects;

public record RegistrierungsNummer(String wert) {

    public RegistrierungsNummer {
        Objects.requireNonNull(wert, "RegistrierungsNummer darf nicht null sein");
        if (wert.isBlank()) {
            throw new IllegalArgumentException(
                "RegistrierungsNummer darf nicht leer sein");
        }
    }
}
