package de.foerderung.betriebsinhaber.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public class Betriebsinhaber {

    private final Long id;
    private String name;
    private Betriebsadresse adresse;
    private BigDecimal betriebsflaeche;
    private BigDecimal foerdersumme;

    private Betriebsinhaber(Long id, String name, Betriebsadresse adresse,
                            BigDecimal betriebsflaeche, BigDecimal foerdersumme) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "Name darf nicht null sein");
        this.adresse = adresse;
        this.betriebsflaeche = betriebsflaeche;
        this.foerdersumme = foerdersumme;
    }

    public static Betriebsinhaber erstellen(String name, Betriebsadresse adresse,
                                              BigDecimal betriebsflaeche, BigDecimal foerdersumme) {
        return new Betriebsinhaber(null, name, adresse, betriebsflaeche, foerdersumme);
    }

    public static Betriebsinhaber rekonstruieren(Long id, String name, Betriebsadresse adresse,
                                                  BigDecimal betriebsflaeche, BigDecimal foerdersumme) {
        return new Betriebsinhaber(id, name, adresse, betriebsflaeche, foerdersumme);
    }

    public void update(String name, Betriebsadresse adresse,
                       BigDecimal betriebsflaeche, BigDecimal foerdersumme) {
        this.name = Objects.requireNonNull(name, "Name darf nicht null sein");
        this.adresse = adresse;
        this.betriebsflaeche = betriebsflaeche;
        this.foerdersumme = foerdersumme;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Betriebsadresse getAdresse() {
        return adresse;
    }

    public BigDecimal getBetriebsflaeche() {
        return betriebsflaeche;
    }

    public BigDecimal getFoerdersumme() {
        return foerdersumme;
    }
}
