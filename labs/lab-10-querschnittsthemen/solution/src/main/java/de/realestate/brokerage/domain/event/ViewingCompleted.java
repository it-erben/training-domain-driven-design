package de.realestate.brokerage.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event raised when a property viewing has been completed.
 */
public record ViewingCompleted(
        UUID processId,
        UUID viewingId,
        LocalDateTime timestamp
) {}
