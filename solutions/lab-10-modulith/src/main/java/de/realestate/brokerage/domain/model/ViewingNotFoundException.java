package de.realestate.brokerage.domain.model;

import java.util.UUID;

public class ViewingNotFoundException extends RuntimeException {

    private final UUID processId;
    private final UUID viewingId;

    public ViewingNotFoundException(UUID processId, UUID viewingId) {
        super("Viewing with ID " + viewingId + " not found in BrokerageProcess " + processId);
        this.processId = processId;
        this.viewingId = viewingId;
    }

    public UUID getProcessId() {
        return processId;
    }

    public UUID getViewingId() {
        return viewingId;
    }
}
