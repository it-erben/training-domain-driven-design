package de.foerderung.betriebsinhaber;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;

@Embeddable
public record Anschrift(
    @NotBlank String strasse,
    @NotBlank String postleitzahl,
    @NotBlank String ort
) {}
