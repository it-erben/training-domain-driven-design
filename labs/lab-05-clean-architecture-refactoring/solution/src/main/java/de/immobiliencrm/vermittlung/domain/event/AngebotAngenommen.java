package de.immobiliencrm.vermittlung.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain Event: An offer has been accepted.
 */
public record AngebotAngenommen(
        UUID vermittlungsvorgangId,
        UUID angebotId,
        LocalDateTime zeitpunkt
) {}
