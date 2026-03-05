package de.realestate.acquisition.domain.port;

import de.realestate.acquisition.domain.model.BrokerageContract;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for the BrokerageContract aggregate.
 * This is a pure domain interface with no framework dependencies.
 */
public interface BrokerageContractRepository {

    Optional<BrokerageContract> findById(UUID id);

    BrokerageContract save(BrokerageContract brokerageContract);
}
