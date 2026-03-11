package de.foerderung.betriebsinhaber.domain.model;

import java.util.Objects;

public record Betriebsadresse(
    String strasse,
    String plz,
    String ort
) {
    public Betriebsadresse {
        Objects.requireNonNull(strasse, "Strasse darf nicht null sein");
        Objects.requireNonNull(plz, "PLZ darf nicht null sein");
        Objects.requireNonNull(ort, "Ort darf nicht null sein");
    }
}
