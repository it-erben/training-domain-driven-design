package de.foerderung.betriebsinhaber;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.SoftDelete;

import java.math.BigDecimal;

@Entity
@Table(name = "betriebsinhaber")
@SoftDelete
public class Betriebsinhaber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @Valid
    @Embedded
    private Betriebsadresse adresse;

    private BigDecimal betriebsflaeche;

    private String betriebsnummer;

    public Betriebsinhaber() {
    }

    public Betriebsinhaber(String name, Betriebsadresse adresse,
                           BigDecimal betriebsflaeche, String betriebsnummer) {
        this.name = name;
        this.adresse = adresse;
        this.betriebsflaeche = betriebsflaeche;
        this.betriebsnummer = betriebsnummer;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Betriebsadresse getAdresse() {
        return adresse;
    }

    public void setAdresse(Betriebsadresse adresse) {
        this.adresse = adresse;
    }

    public BigDecimal getBetriebsflaeche() {
        return betriebsflaeche;
    }

    public void setBetriebsflaeche(BigDecimal betriebsflaeche) {
        this.betriebsflaeche = betriebsflaeche;
    }

    public String getBetriebsnummer() {
        return betriebsnummer;
    }

    public void setBetriebsnummer(String betriebsnummer) {
        this.betriebsnummer = betriebsnummer;
    }
}
