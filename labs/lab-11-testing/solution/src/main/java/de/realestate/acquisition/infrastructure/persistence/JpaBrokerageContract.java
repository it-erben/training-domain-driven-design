package de.realestate.acquisition.infrastructure.persistence;

import de.realestate.acquisition.domain.model.BrokerageContract;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity for persisting the BrokerageContract aggregate.
 */
@Entity
@Table(name = "brokerage_contract")
public class JpaBrokerageContract {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID ownerId;

    @Column(nullable = false)
    private UUID propertyId;

    private LocalDateTime signedAt;

    protected JpaBrokerageContract() {
        // Required by JPA
    }

    public static JpaBrokerageContract fromModel(BrokerageContract contract) {
        JpaBrokerageContract jpa = new JpaBrokerageContract();
        jpa.id = contract.getId();
        jpa.ownerId = contract.getOwnerId();
        jpa.propertyId = contract.getPropertyId();
        jpa.signedAt = contract.getSignedAt();
        return jpa;
    }

    public BrokerageContract toModel() {
        return BrokerageContract.reconstitute(id, ownerId, propertyId, signedAt);
    }
}
