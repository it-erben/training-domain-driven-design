package de.realestate.acquisition.infrastructure.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity mapped to the database table for BrokerageContract.
 */
@Entity
@Table(name = "brokerage_contract")
public class JpaBrokerageContract {

    @Id
    private UUID id;

    private UUID ownerId;

    private UUID propertyId;

    private BigDecimal askingPrice;

    private String currency;

    private BigDecimal commissionPercentage;

    private LocalDateTime closedAt;

    // Default constructor required by JPA
    protected JpaBrokerageContract() {
    }

    public JpaBrokerageContract(UUID id, UUID ownerId, UUID propertyId,
                                BigDecimal askingPrice, String currency,
                                BigDecimal commissionPercentage,
                                LocalDateTime closedAt) {
        this.id = id;
        this.ownerId = ownerId;
        this.propertyId = propertyId;
        this.askingPrice = askingPrice;
        this.currency = currency;
        this.commissionPercentage = commissionPercentage;
        this.closedAt = closedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public UUID getPropertyId() {
        return propertyId;
    }

    public BigDecimal getAskingPrice() {
        return askingPrice;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getCommissionPercentage() {
        return commissionPercentage;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }
}
