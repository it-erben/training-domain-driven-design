package de.realestate.acquisition.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Integration event raised when a broker mandate has been finalized.
 */
public record ContractSigned(
        UUID contractId,
        UUID propertyId,
        LocalDateTime signedAt
) {}
