package de.foerderung.betriebsinhaber;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.SoftDelete;

import java.math.BigDecimal;

@Entity
@Table(name = "betriebsinhaber")
@SoftDelete
public class Betriebsinhaber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String betriebsnummer;

    @Valid
    @NotNull
    @Embedded
    private Betriebsadresse adresse;

    private BigDecimal betriebsflaeche;

    private BigDecimal foerdersumme;

    @Enumerated(EnumType.STRING)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private BetriebsinhaberStatus status = BetriebsinhaberStatus.ENTWURF;

    public Betriebsinhaber() {
    }

    public Betriebsinhaber(String name, String betriebsnummer, Betriebsadresse adresse,
                           BigDecimal betriebsflaeche, BigDecimal foerdersumme) {
        this.name = name;
        this.betriebsnummer = betriebsnummer;
        this.adresse = adresse;
        this.betriebsflaeche = betriebsflaeche;
        this.foerdersumme = foerdersumme;
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

    public String getBetriebsnummer() {
        return betriebsnummer;
    }

    public void setBetriebsnummer(String betriebsnummer) {
        this.betriebsnummer = betriebsnummer;
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

    public BigDecimal getFoerdersumme() {
        return foerdersumme;
    }

    public void setFoerdersumme(BigDecimal foerdersumme) {
        this.foerdersumme = foerdersumme;
    }

    public BetriebsinhaberStatus getStatus() {
        return status;
    }

    public void setStatus(BetriebsinhaberStatus status) {
        this.status = status;
    }
}
