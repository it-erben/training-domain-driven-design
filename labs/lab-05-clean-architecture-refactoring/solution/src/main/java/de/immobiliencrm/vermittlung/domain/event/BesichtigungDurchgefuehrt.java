package de.immobiliencrm.vermittlung.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain Event: A viewing has been conducted.
 */
public record BesichtigungDurchgefuehrt(
        UUID vermittlungsvorgangId,
        UUID besichtigungId,
        LocalDateTime zeitpunkt
) {}
