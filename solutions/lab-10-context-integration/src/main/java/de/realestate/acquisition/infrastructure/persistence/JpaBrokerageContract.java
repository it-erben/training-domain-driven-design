package de.realestate.acquisition.infrastructure.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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

    private LocalDateTime closedAt;

    // Default constructor required by JPA
    protected JpaBrokerageContract() {
    }

    public JpaBrokerageContract(UUID id, UUID ownerId, UUID propertyId, LocalDateTime closedAt) {
        this.id = id;
        this.ownerId = ownerId;
        this.propertyId = propertyId;
        this.closedAt = closedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public UUID getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(UUID propertyId) {
        this.propertyId = propertyId;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }
}
