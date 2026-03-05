package de.realestate.brokerage.application.command;

import java.util.UUID;

/**
 * Result returned after successfully creating a new viewing appointment.
 */
public record CreateViewingResult(
        UUID viewingId,
        UUID processId
) {}
