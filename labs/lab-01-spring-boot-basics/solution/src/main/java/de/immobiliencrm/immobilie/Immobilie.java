package de.immobiliencrm.immobilie;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

@Entity
public class Immobilie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String bezeichnung;

    @NotBlank
    private String strasse;

    @NotBlank
    private String plz;

    @NotBlank
    private String ort;

    private BigDecimal wohnflaeche;

    private BigDecimal kaufpreis;

    public Immobilie() {
    }

    public Immobilie(String bezeichnung, String strasse, String plz, String ort,
                     BigDecimal wohnflaeche, BigDecimal kaufpreis) {
        this.bezeichnung = bezeichnung;
        this.strasse = strasse;
        this.plz = plz;
        this.ort = ort;
        this.wohnflaeche = wohnflaeche;
        this.kaufpreis = kaufpreis;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBezeichnung() {
        return bezeichnung;
    }

    public void setBezeichnung(String bezeichnung) {
        this.bezeichnung = bezeichnung;
    }

    public String getStrasse() {
        return strasse;
    }

    public void setStrasse(String strasse) {
        this.strasse = strasse;
    }

    public String getPlz() {
        return plz;
    }

    public void setPlz(String plz) {
        this.plz = plz;
    }

    public String getOrt() {
        return ort;
    }

    public void setOrt(String ort) {
        this.ort = ort;
    }

    public BigDecimal getWohnflaeche() {
        return wohnflaeche;
    }

    public void setWohnflaeche(BigDecimal wohnflaeche) {
        this.wohnflaeche = wohnflaeche;
    }

    public BigDecimal getKaufpreis() {
        return kaufpreis;
    }

    public void setKaufpreis(BigDecimal kaufpreis) {
        this.kaufpreis = kaufpreis;
    }

}
