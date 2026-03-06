package de.realestate.brokerage.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain Event: A viewing has been completed.
 */
public record ViewingCompleted(
        UUID brokerageProcessId,
        UUID viewingId,
        LocalDateTime timestamp
) {}
