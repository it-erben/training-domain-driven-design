package de.foerderung.betriebsinhaber.infrastructure.persistence;

import de.foerderung.betriebsinhaber.domain.model.Betriebsadresse;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;

@Embeddable
public record JpaBetriebsadresse(
    @NotBlank String strasse,
    @NotBlank String plz,
    @NotBlank String ort
) {

    public static JpaBetriebsadresse fromModel(Betriebsadresse adresse) {
        if (adresse == null) {
            return null;
        }
        return new JpaBetriebsadresse(adresse.strasse(), adresse.plz(), adresse.ort());
    }

    public Betriebsadresse toModel() {
        return new Betriebsadresse(strasse, plz, ort);
    }
}
