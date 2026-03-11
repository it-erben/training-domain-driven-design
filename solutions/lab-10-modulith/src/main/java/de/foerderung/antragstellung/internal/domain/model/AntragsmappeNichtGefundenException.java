package de.foerderung.antragstellung.internal.domain.model;

public class AntragsmappeNichtGefundenException extends RuntimeException {

    private final AntragId antragsmappeId;

    public AntragsmappeNichtGefundenException(AntragId antragsmappeId) {
        super("Keine AntragsMappe mit ID %s gefunden".formatted(antragsmappeId));
        this.antragsmappeId = antragsmappeId;
    }

    public AntragId getAntragsmappeId() {
        return antragsmappeId;
    }
}
