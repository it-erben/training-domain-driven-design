package de.immobiliencrm.vermittlung.application.command;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Command representing the intention to schedule a new viewing.
 */
public record BesichtigungAnlegenCommand(
        UUID vermittlungsvorgangId,
        String interessentName,
        LocalDateTime zeitpunkt
) {}
