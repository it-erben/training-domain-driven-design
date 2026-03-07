package de.realestate.acquisition;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Integration event published when a brokerage contract has been concluded.
 * Located in the module root package to be part of the public API.
 * Consumed by the Brokerage module to initiate a new brokerage process.
 */
public record ContractSigned(
        UUID contractId,
        UUID propertyId,
        LocalDateTime closedAt,
        BigDecimal askingPrice,
        String currency,
        BigDecimal commissionPercentage
) {}
