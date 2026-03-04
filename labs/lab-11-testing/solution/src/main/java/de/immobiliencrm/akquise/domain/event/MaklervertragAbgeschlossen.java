package de.immobiliencrm.akquise.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Integration event raised when a broker mandate has been finalized.
 */
public record MaklervertragAbgeschlossen(
        UUID maklerauftragId,
        UUID immobilieId,
        LocalDateTime abgeschlossenAm
) {}
