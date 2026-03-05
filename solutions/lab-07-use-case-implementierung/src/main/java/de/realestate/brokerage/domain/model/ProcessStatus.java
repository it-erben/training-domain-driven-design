package de.realestate.brokerage.domain.model;

/**
 * Enum representing the lifecycle status of a BrokerageProcess.
 */
public enum ProcessStatus {
    NEW,
    IN_MARKETING,
    VIEWING,
    OFFER_PHASE,
    NOTARY_APPOINTMENT,
    COMPLETED
}
