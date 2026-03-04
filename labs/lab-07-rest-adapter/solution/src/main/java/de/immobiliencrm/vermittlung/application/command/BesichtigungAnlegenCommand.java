package de.immobiliencrm.vermittlung.application.command;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Command representing the intention to create a new viewing appointment.
 */
public record BesichtigungAnlegenCommand(
        UUID vermittlungsvorgangId,
        String interessentName,
        LocalDateTime zeitpunkt
) {}
