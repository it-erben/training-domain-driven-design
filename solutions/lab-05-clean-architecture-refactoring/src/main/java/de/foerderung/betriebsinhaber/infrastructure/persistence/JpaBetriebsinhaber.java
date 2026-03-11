package de.foerderung.betriebsinhaber.infrastructure.persistence;

import de.foerderung.betriebsinhaber.domain.model.Betriebsinhaber;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.SoftDelete;

import java.math.BigDecimal;

@Entity
@Table(name = "betriebsinhaber")
@SoftDelete
public class JpaBetriebsinhaber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @Valid
    @Embedded
    private JpaBetriebsadresse adresse;

    private BigDecimal betriebsflaeche;

    private BigDecimal foerdersumme;

    protected JpaBetriebsinhaber() {
    }

    public static JpaBetriebsinhaber fromModel(Betriebsinhaber betriebsinhaber) {
        JpaBetriebsinhaber jpa = new JpaBetriebsinhaber();
        jpa.id = betriebsinhaber.getId();
        jpa.name = betriebsinhaber.getName();
        jpa.adresse = JpaBetriebsadresse.fromModel(betriebsinhaber.getAdresse());
        jpa.betriebsflaeche = betriebsinhaber.getBetriebsflaeche();
        jpa.foerdersumme = betriebsinhaber.getFoerdersumme();
        return jpa;
    }

    public Betriebsinhaber toModel() {
        return Betriebsinhaber.rekonstruieren(
                id,
                name,
                adresse != null ? adresse.toModel() : null,
                betriebsflaeche,
                foerdersumme
        );
    }
}
