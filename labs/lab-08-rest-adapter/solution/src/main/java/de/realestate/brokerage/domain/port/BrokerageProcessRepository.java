package de.realestate.brokerage.domain.port;

import java.util.Optional;
import java.util.UUID;

import de.realestate.brokerage.domain.model.BrokerageProcess;

/**
 * Repository port for BrokerageProcess persistence.
 * This is a pure Java interface without any framework dependencies.
 */
public interface BrokerageProcessRepository {

    Optional<BrokerageProcess> findById(UUID id);

    BrokerageProcess save(BrokerageProcess brokerageProcess);

    void deleteById(UUID id);
}
