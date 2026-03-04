package de.immobiliencrm.vermittlung.domain.model;

import java.util.Objects;

/**
 * Value Object representing a postal address.
 */
public record Adresse(String strasse, String plz, String ort) {

    public Adresse {
        Objects.requireNonNull(strasse, "Strasse darf nicht null sein");
        Objects.requireNonNull(plz, "PLZ darf nicht null sein");
        Objects.requireNonNull(ort, "Ort darf nicht null sein");

        if (strasse.isBlank()) {
            throw new IllegalArgumentException("Strasse darf nicht leer sein");
        }
        if (plz.isBlank()) {
            throw new IllegalArgumentException("PLZ darf nicht leer sein");
        }
        if (ort.isBlank()) {
            throw new IllegalArgumentException("Ort darf nicht leer sein");
        }
    }
}
