package de.realestate.brokerage.application.service;

import de.realestate.brokerage.domain.model.AskingPrice;
import de.realestate.brokerage.domain.model.Commission;
import de.realestate.brokerage.domain.model.BrokerageProcess;
import de.realestate.brokerage.domain.port.BrokerageProcessRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Application Service that orchestrates use cases for the BrokerageProcess aggregate.
 */
@Service
@Transactional
public class BrokerageProcessApplicationService {

    private final BrokerageProcessRepository repository;

    public BrokerageProcessApplicationService(BrokerageProcessRepository repository) {
        this.repository = repository;
    }

    /**
     * Creates a new BrokerageProcess and persists it.
     */
    public BrokerageProcess create(UUID propertyId,
                                    AskingPrice askingPrice, Commission commission) {
        BrokerageProcess process = BrokerageProcess.create(propertyId, askingPrice, commission);
        return repository.save(process);
    }

    /**
     * Finds a BrokerageProcess by its ID.
     */
    @Transactional(readOnly = true)
    public Optional<BrokerageProcess> findById(UUID id) {
        return repository.findById(id);
    }
}
