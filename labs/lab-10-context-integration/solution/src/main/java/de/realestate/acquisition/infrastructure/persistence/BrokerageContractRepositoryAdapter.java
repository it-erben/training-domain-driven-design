package de.realestate.acquisition.infrastructure.persistence;

import de.realestate.acquisition.domain.model.BrokerageContract;
import de.realestate.acquisition.domain.port.BrokerageContractRepository;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementing the domain repository port using JPA persistence.
 */
@Component
public class BrokerageContractRepositoryAdapter implements BrokerageContractRepository {

    private final JpaBrokerageContractRepository jpaRepository;

    public BrokerageContractRepositoryAdapter(JpaBrokerageContractRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<BrokerageContract> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public BrokerageContract save(BrokerageContract brokerageContract) {
        JpaBrokerageContract entity = toJpa(brokerageContract);
        JpaBrokerageContract saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    private BrokerageContract toDomain(JpaBrokerageContract entity) {
        return BrokerageContract.reconstruct(
                entity.getId(), entity.getOwnerId(), entity.getPropertyId(), entity.getClosedAt());
    }

    private JpaBrokerageContract toJpa(BrokerageContract contract) {
        return new JpaBrokerageContract(
                contract.getId(),
                contract.getOwnerId(),
                contract.getPropertyId(),
                contract.getClosedAt()
        );
    }
}
