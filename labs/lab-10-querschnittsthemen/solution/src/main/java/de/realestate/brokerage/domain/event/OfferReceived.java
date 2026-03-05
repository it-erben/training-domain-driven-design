package de.realestate.brokerage.domain.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event raised when a new offer has been received.
 */
public record OfferReceived(
        UUID processId,
        BigDecimal offerAmount,
        LocalDateTime timestamp
) {}
