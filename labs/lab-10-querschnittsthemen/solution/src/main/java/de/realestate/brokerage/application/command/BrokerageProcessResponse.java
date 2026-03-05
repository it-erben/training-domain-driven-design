package de.realestate.brokerage.application.command;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response DTO representing a brokerage process, used by adapters
 * to avoid direct dependency on domain model classes.
 */
public record BrokerageProcessResponse(
        UUID id,
        UUID propertyId,
        String street,
        String postalCode,
        String city,
        BigDecimal priceAmount,
        String priceCurrency,
        BigDecimal commissionPercentage,
        String status
) {}
