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
    private Anschrift anschrift;

    private String betriebsnummer;

    public Betriebsinhaber() {
    }

    public Betriebsinhaber(String name, Anschrift anschrift, String betriebsnummer) {
        this.name = name;
        this.anschrift = anschrift;
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

    public Anschrift getAnschrift() {
        return anschrift;
    }

    public void setAnschrift(Anschrift anschrift) {
        this.anschrift = anschrift;
    }

    public String getBetriebsnummer() {
        return betriebsnummer;
    }

    public void setBetriebsnummer(String betriebsnummer) {
        this.betriebsnummer = betriebsnummer;
    }
}
