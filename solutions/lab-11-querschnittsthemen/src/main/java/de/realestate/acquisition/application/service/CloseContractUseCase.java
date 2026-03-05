package de.realestate.acquisition.application.service;

import de.realestate.acquisition.domain.event.ContractSigned;
import de.realestate.acquisition.domain.model.BrokerageContract;
import de.realestate.acquisition.domain.port.BrokerageContractRepository;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Application service for concluding a broker contract.
 * Publishes an integration event after successful completion.
 */
@Service
@Transactional
public class CloseContractUseCase {

    private final BrokerageContractRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    public CloseContractUseCase(BrokerageContractRepository repository,
                                             ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Concludes the broker contract and publishes a ContractSigned event.
     */
    public void closeContract(UUID contractId) {
        BrokerageContract contract = repository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "BrokerageContract with ID " + contractId + " not found"));

        contract.sign();
        repository.save(contract);

        eventPublisher.publishEvent(new ContractSigned(
                contract.getId(),
                contract.getPropertyId(),
                contract.getSignedAt()
        ));
    }

    /**
     * Creates a new BrokerageContract and returns it.
     */
    public BrokerageContract create(UUID ownerId, UUID propertyId) {
        BrokerageContract contract = BrokerageContract.create(ownerId, propertyId);
        return repository.save(contract);
    }
}
