package de.immobiliencrm.vermittlung.domain.model;

import java.util.UUID;

/**
 * Exception thrown when a brokerage process cannot be found by its ID.
 */
public class VermittlungsvorgangNichtGefundenException extends RuntimeException {

    public VermittlungsvorgangNichtGefundenException(UUID id) {
        super("Vermittlungsvorgang mit ID " + id + " nicht gefunden");
    }
}
