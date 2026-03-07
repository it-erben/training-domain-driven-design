package de.realestate.acquisition.adapter.web;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateContractRequest(
        UUID ownerId,
        UUID propertyId,
        BigDecimal askingPrice,
        String currency,
        BigDecimal commissionPercentage
) {}
