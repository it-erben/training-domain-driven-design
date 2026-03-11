package de.foerderung.antragstellung.domain.model;

public class FlurstueckNichtGefundenException extends RuntimeException {

    private final AntragId antragsmappeId;
    private final FlurstueckId flurstueckId;

    public FlurstueckNichtGefundenException(AntragId antragsmappeId, FlurstueckId flurstueckId) {
        super("Flurstueck mit ID " + flurstueckId + " nicht gefunden in AntragsMappe " + antragsmappeId);
        this.antragsmappeId = antragsmappeId;
        this.flurstueckId = flurstueckId;
    }

    public AntragId getAntragsmappeId() {
        return antragsmappeId;
    }

    public FlurstueckId getFlurstueckId() {
        return flurstueckId;
    }
}
