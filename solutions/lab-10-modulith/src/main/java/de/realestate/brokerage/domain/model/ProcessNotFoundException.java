package de.realestate.brokerage.domain.model;

import java.util.UUID;

/**
 * Exception thrown when a BrokerageProcess cannot be found by its ID.
 */
public class ProcessNotFoundException extends RuntimeException {

    private final UUID processId;

    public ProcessNotFoundException(UUID id) {
        super("BrokerageProcess with ID " + id + " not found");
        this.processId = id;
    }

    public UUID getProcessId() {
        return processId;
    }
}
