package de.foerderung.antragstellung.domain.model;

/**
 * Exception thrown when an AntragsMappe cannot be found by its ID.
 */
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
