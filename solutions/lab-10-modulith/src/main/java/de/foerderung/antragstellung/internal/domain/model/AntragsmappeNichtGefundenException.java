package de.foerderung.antragstellung.internal.domain.model;

import java.util.UUID;

public class AntragsmappeNichtGefundenException extends RuntimeException {

    private final UUID antragsmappeId;

    public AntragsmappeNichtGefundenException(UUID antragsmappeId) {
        super("Keine AntragsMappe mit ID %s gefunden".formatted(antragsmappeId));
        this.antragsmappeId = antragsmappeId;
    }

    public UUID getAntragsmappeId() {
        return antragsmappeId;
    }
}
