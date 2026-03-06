package de.realestate.brokerage.application.command;

import java.util.UUID;

/**
 * Result returned after successfully scheduling a viewing.
 */
public record CreateViewingResult(
        UUID viewingId,
        UUID processId
) {}
