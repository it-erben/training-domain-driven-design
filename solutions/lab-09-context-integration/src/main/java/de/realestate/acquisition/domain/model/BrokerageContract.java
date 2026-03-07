package de.realestate.acquisition.domain.model;

import de.realestate.acquisition.domain.event.ContractSigned;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root representing a brokerage contract in the Acquisition bounded context.
 */
public class BrokerageContract {

    private final UUID id;
    private final UUID ownerId;
    private final UUID propertyId;
    private final BigDecimal askingPrice;
    private final String currency;
    private final BigDecimal commissionPercentage;
    private LocalDateTime closedAt;
    private final transient List<Object> domainEvents = new ArrayList<>();

    private BrokerageContract(UUID id, UUID ownerId, UUID propertyId,
                              BigDecimal askingPrice, String currency,
                              BigDecimal commissionPercentage) {
        this.id = Objects.requireNonNull(id, "ID must not be null");
        this.ownerId = Objects.requireNonNull(ownerId, "OwnerId must not be null");
        this.propertyId = Objects.requireNonNull(propertyId, "PropertyId must not be null");
        this.askingPrice = Objects.requireNonNull(askingPrice, "AskingPrice must not be null");
        this.currency = Objects.requireNonNull(currency, "Currency must not be null");
        this.commissionPercentage = Objects.requireNonNull(commissionPercentage, "CommissionPercentage must not be null");
    }

    /**
     * Factory method to create a new BrokerageContract.
     */
    public static BrokerageContract create(UUID ownerId, UUID propertyId,
                                           BigDecimal askingPrice, String currency,
                                           BigDecimal commissionPercentage) {
        return new BrokerageContract(UUID.randomUUID(), ownerId, propertyId,
                askingPrice, currency, commissionPercentage);
    }

    /**
     * Reconstitutes an existing BrokerageContract from persistence.
     */
    public static BrokerageContract reconstruct(UUID id, UUID ownerId, UUID propertyId,
                                                BigDecimal askingPrice, String currency,
                                                BigDecimal commissionPercentage,
                                                LocalDateTime closedAt) {
        BrokerageContract contract = new BrokerageContract(id, ownerId, propertyId,
                askingPrice, currency, commissionPercentage);
        contract.closedAt = closedAt;
        return contract;
    }

    /**
     * Marks the contract as concluded and registers a ContractSigned domain event.
     */
    public void close() {
        if (this.closedAt != null) {
            throw new IllegalStateException("BrokerageContract is already closed");
        }
        this.closedAt = LocalDateTime.now();
        domainEvents.add(new ContractSigned(id, propertyId, closedAt,
                askingPrice, currency, commissionPercentage));
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

    public boolean isClosed() {
        return closedAt != null;
    }

    public List<Object> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }
}
