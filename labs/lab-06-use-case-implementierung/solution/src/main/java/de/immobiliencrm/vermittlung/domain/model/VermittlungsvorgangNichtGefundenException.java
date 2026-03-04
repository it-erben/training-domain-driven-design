package de.immobiliencrm.vermittlung.domain.model;

import java.util.UUID;

/**
 * Exception thrown when a Vermittlungsvorgang cannot be found by its ID.
 */
public class VermittlungsvorgangNichtGefundenException extends RuntimeException {

    private final UUID vermittlungsvorgangId;

    public VermittlungsvorgangNichtGefundenException(UUID id) {
        super("Vermittlungsvorgang mit ID " + id + " nicht gefunden");
        this.vermittlungsvorgangId = id;
    }

    public UUID getVermittlungsvorgangId() {
        return vermittlungsvorgangId;
    }
}
