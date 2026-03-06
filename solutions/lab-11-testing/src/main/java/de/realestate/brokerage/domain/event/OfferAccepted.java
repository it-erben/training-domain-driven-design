package de.realestate.brokerage.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event raised when an offer has been accepted.
 */
public record OfferAccepted(
        UUID processId,
        UUID offerId,
        LocalDateTime timestamp
) {}
