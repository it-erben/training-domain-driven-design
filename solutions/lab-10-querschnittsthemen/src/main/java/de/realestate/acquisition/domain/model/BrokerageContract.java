package de.realestate.acquisition.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing a broker contract in the Acquisition bounded context.
 */
public class BrokerageContract {

    private final UUID id;
    private final UUID ownerId;
    private final UUID propertyId;
    private LocalDateTime signedAt;

    private BrokerageContract(UUID id, UUID ownerId, UUID propertyId) {
        this.id = Objects.requireNonNull(id, "ID must not be null");
        this.ownerId = Objects.requireNonNull(ownerId, "OwnerId must not be null");
        this.propertyId = Objects.requireNonNull(propertyId, "PropertyId must not be null");
    }

    /**
     * Factory method to create a new BrokerageContract.
     */
    public static BrokerageContract create(UUID ownerId, UUID propertyId) {
        return new BrokerageContract(UUID.randomUUID(), ownerId, propertyId);
    }

    /**
     * Reconstructs an existing BrokerageContract from persisted data.
     */
    public static BrokerageContract reconstruct(UUID id, UUID ownerId, UUID propertyId, LocalDateTime closedAt) {
        BrokerageContract contract = new BrokerageContract(id, ownerId, propertyId);
        contract.signedAt = closedAt;
        return contract;
    }

    /**
     * Marks the contract as concluded by setting the completion timestamp.
     */
    public void sign() {
        if (this.signedAt != null) {
            throw new IllegalStateException("BrokerageContract has already been signed");
        }
        this.signedAt = LocalDateTime.now();
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

    public LocalDateTime getSignedAt() {
        return signedAt;
    }

    public boolean isSigned() {
        return signedAt != null;
    }
}
