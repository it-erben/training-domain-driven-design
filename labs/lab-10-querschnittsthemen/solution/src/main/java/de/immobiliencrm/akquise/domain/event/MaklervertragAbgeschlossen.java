package de.immobiliencrm.akquise.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Integration event published when a broker contract has been concluded.
 * Consumed by the Vermittlung bounded context to initiate a new brokerage process.
 */
public record MaklervertragAbgeschlossen(
        UUID maklerauftragId,
        UUID immobilieId,
        LocalDateTime abgeschlossenAm
) {}
