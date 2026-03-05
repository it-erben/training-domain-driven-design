package de.realestate.brokerage.domain.model;

import java.util.UUID;

/**
 * Exception thrown when a brokerage process cannot be found by its ID.
 */
public class ProcessNotFoundException extends RuntimeException {

    public ProcessNotFoundException(UUID id) {
        super("BrokerageProcess with ID " + id + " not found");
    }
}
