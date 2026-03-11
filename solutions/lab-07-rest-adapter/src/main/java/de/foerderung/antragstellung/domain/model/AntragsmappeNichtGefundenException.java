package de.foerderung.antragstellung.domain.model;

public class AntragsmappeNichtGefundenException extends RuntimeException {

    private final AntragId antragsmappeId;

    public AntragsmappeNichtGefundenException(AntragId antragsmappeId) {
        super("AntragsMappe mit ID " + antragsmappeId + " nicht gefunden");
        this.antragsmappeId = antragsmappeId;
    }

    public AntragId getAntragsmappeId() {
        return antragsmappeId;
    }
}
