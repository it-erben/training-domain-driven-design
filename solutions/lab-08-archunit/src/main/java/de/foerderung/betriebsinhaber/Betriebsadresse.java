package de.foerderung.betriebsinhaber;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;

@Embeddable
public record Betriebsadresse(
    @NotBlank String strasse,
    @NotBlank String plz,
    @NotBlank String ort
) {}
