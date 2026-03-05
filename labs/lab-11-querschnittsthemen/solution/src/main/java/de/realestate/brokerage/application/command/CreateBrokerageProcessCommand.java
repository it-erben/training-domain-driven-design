package de.realestate.brokerage.application.command;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Command to create a new brokerage process.
 */
public record CreateBrokerageProcessCommand(
        UUID propertyId,
        String street,
        String postalCode,
        String city,
        BigDecimal priceAmount,
        String priceCurrency,
        BigDecimal commissionPercentage
) {}
