package de.realestate.acquisition.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Integration event published when a broker contract has been concluded.
 * Consumed by the Brokerage bounded context to initiate a new brokerage process.
 */
public record ContractSigned(
        UUID contractId,
        UUID propertyId,
        LocalDateTime signedAt
) {}
