package de.realestate.brokerage.domain.port;

import de.realestate.brokerage.domain.model.BrokerageProcess;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for the BrokerageProcess aggregate.
 * This is a pure domain interface with no framework dependencies.
 */
public interface BrokerageProcessRepository {

    Optional<BrokerageProcess> findById(UUID id);

    List<BrokerageProcess> findAll();

    BrokerageProcess save(BrokerageProcess brokerageProcess);

    void deleteById(UUID id);
}
