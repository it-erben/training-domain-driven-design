package de.realestate.brokerage.application.service;

import de.realestate.brokerage.application.command.CreateBrokerageProcessCommand;
import de.realestate.brokerage.domain.model.Address;
import de.realestate.brokerage.domain.model.AskingPrice;
import de.realestate.brokerage.domain.model.Commission;
import de.realestate.brokerage.domain.model.BrokerageProcess;
import de.realestate.brokerage.domain.port.BrokerageProcessRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Application service orchestrating use cases for the brokerage process.
 */
@Service
@Transactional
public class BrokerageProcessService {

    private final BrokerageProcessRepository repository;

    public BrokerageProcessService(BrokerageProcessRepository repository) {
        this.repository = repository;
    }

    /**
     * Creates a new brokerage process from the given command.
     */
    public BrokerageProcess create(CreateBrokerageProcessCommand command) {
        Address address = new Address(command.street(), command.zipCode(), command.city());
        AskingPrice askingPrice = new AskingPrice(command.priceAmount(), command.priceCurrency());
        Commission commission = new Commission(command.commissionPercentage());

        BrokerageProcess process = BrokerageProcess.create(
                command.propertyId(), address, askingPrice, commission);

        return repository.save(process);
    }

    /**
     * Finds a brokerage process by its ID.
     */
    @Transactional(readOnly = true)
    public Optional<BrokerageProcess> findById(UUID id) {
        return repository.findById(id);
    }

    /**
     * Returns all brokerage processes.
     */
    @Transactional(readOnly = true)
    public List<BrokerageProcess> findAll() {
        return repository.findAll();
    }

    /**
     * Deletes a brokerage process by its ID.
     */
    public void delete(UUID id) {
        repository.deleteById(id);
    }
}
