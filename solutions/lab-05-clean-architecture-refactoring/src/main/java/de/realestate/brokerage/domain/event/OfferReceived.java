package de.realestate.brokerage.domain.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain Event: An offer has been received.
 */
public record OfferReceived(
        UUID brokerageProcessId,
        BigDecimal offerAmount,
        LocalDateTime timestamp
) {}
