package de.immobiliencrm.vermittlung.domain.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain Event: An offer has been received.
 */
public record AngebotEingegangen(
        UUID vermittlungsvorgangId,
        BigDecimal angebotsBetrag,
        LocalDateTime zeitpunkt
) {}
