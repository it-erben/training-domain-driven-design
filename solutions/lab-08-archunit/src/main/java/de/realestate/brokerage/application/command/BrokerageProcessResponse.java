package de.realestate.brokerage.application.command;

import java.math.BigDecimal;
import java.util.UUID;

public record BrokerageProcessResponse(
        UUID id,
        UUID propertyId,
        String street,
        String zipCode,
        String city,
        BigDecimal priceAmount,
        String priceCurrency,
        BigDecimal commissionPercentage,
        String status
) {}
