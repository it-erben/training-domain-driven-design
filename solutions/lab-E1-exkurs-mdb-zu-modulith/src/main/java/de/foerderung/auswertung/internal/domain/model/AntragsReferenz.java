package de.foerderung.auswertung.internal.domain.model;

import java.util.Objects;

/**
 * Value Object representing a reference to an AntragsMappe from the Antragstellung context.
 * This is the Auswertung BC's OWN type — independent from the Pruefung BC's AntragsReferenz.
 * Each BC defines its own vocabulary (Ubiquitous Language).
 */
public record AntragsReferenz(String registrierungsNummer) {

    public AntragsReferenz {
        Objects.requireNonNull(registrierungsNummer,
                "RegistrierungsNummer darf nicht null sein");
        if (registrierungsNummer.isBlank()) {
            throw new IllegalArgumentException(
                    "RegistrierungsNummer darf nicht leer sein");
        }
    }
}
