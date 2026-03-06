package de.realestate.brokerage.application.command;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Command representing the intention to schedule a new viewing.
 */
public record CreateViewingCommand(
        UUID processId,
        String prospectName,
        LocalDateTime appointmentDate
) {}
