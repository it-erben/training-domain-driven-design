package de.realestate.acquisition.domain.port;

import de.realestate.acquisition.domain.model.BrokerageContract;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for the BrokerageContract aggregate.
 */
public interface BrokerageContractRepository {

    Optional<BrokerageContract> findById(UUID id);

    BrokerageContract save(BrokerageContract brokerageContract);
}
