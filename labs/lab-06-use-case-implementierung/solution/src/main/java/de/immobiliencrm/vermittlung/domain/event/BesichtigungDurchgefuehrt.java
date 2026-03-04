package de.immobiliencrm.vermittlung.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event raised when a viewing has been completed.
 */
public record BesichtigungDurchgefuehrt(
        UUID vermittlungsvorgangId,
        UUID besichtigungId,
        LocalDateTime zeitpunkt
) {}
