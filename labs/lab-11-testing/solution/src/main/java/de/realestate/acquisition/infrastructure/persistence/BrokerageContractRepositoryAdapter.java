package de.realestate.acquisition.infrastructure.persistence;

import de.realestate.acquisition.domain.model.BrokerageContract;
import de.realestate.acquisition.domain.port.BrokerageContractRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter that bridges the domain repository port with the JPA repository implementation.
 */
@Component
public class BrokerageContractRepositoryAdapter implements BrokerageContractRepository {

    private final JpaBrokerageContractRepository jpaRepository;

    public BrokerageContractRepositoryAdapter(JpaBrokerageContractRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<BrokerageContract> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(JpaBrokerageContract::toModel);
    }

    @Override
    public BrokerageContract save(BrokerageContract brokerageContract) {
        JpaBrokerageContract jpaEntity = JpaBrokerageContract.fromModel(brokerageContract);
        JpaBrokerageContract saved = jpaRepository.save(jpaEntity);
        return saved.toModel();
    }
}
