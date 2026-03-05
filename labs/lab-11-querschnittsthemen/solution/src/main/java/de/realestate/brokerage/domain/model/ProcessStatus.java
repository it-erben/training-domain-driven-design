package de.realestate.brokerage.domain.model;

/**
 * Enum representing the lifecycle status of a brokerage process.
 */
public enum ProcessStatus {
    NEU,
    IN_VERMARKTUNG,
    BESICHTIGUNG,
    ANGEBOT_PHASE,
    NOTARTERMIN,
    ABGESCHLOSSEN
}
