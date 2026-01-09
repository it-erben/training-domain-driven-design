package de.realestate.brokerage.application.command;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Command representing the intention to create a new viewing appointment.
 */
public record CreateViewingCommand(
        UUID processId,
        String prospectName,
        LocalDateTime appointmentDate
) {}
