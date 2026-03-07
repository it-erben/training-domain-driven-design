package de.realestate.acquisition.application.service;

import de.realestate.acquisition.domain.model.BrokerageContract;
import de.realestate.acquisition.domain.port.BrokerageContractRepository;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Application service for concluding a brokerage contract.
 * Publishes integration events registered by the aggregate after successful completion.
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
     * Concludes the brokerage contract and publishes the domain events registered by the aggregate.
     */
    public void close(UUID contractId) {
        BrokerageContract contract = repository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "BrokerageContract with ID " + contractId + " not found"));

        contract.close();
        repository.save(contract);

        contract.getDomainEvents().forEach(eventPublisher::publishEvent);
        contract.clearDomainEvents();
    }

    /**
     * Creates a new BrokerageContract and returns it.
     */
    public BrokerageContract create(UUID ownerId, UUID propertyId,
                                    BigDecimal askingPrice, String currency,
                                    BigDecimal commissionPercentage) {
        BrokerageContract contract = BrokerageContract.create(
                ownerId, propertyId, askingPrice, currency, commissionPercentage);
        return repository.save(contract);
    }
}
