package de.realestate.brokerage.domain.port;

import de.realestate.brokerage.domain.model.BrokerageProcess;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound Port for persisting BrokerageProcess aggregates.
 * Pure Java interface -- no framework dependencies.
 */
public interface BrokerageProcessRepository {

    Optional<BrokerageProcess> findById(UUID id);

    BrokerageProcess save(BrokerageProcess brokerageProcess);

    void deleteById(UUID id);
}
