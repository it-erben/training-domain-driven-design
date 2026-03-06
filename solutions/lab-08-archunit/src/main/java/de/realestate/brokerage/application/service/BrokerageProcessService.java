package de.realestate.brokerage.application.service;

import de.realestate.brokerage.application.command.BrokerageProcessResponse;
import de.realestate.brokerage.application.command.CreateBrokerageProcessCommand;
import de.realestate.brokerage.domain.model.Address;
import de.realestate.brokerage.domain.model.AskingPrice;
import de.realestate.brokerage.domain.model.BrokerageProcess;
import de.realestate.brokerage.domain.model.Commission;
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
    public BrokerageProcessResponse create(CreateBrokerageProcessCommand command) {
        Address address = new Address(command.street(), command.zipCode(), command.city());
        AskingPrice askingPrice = new AskingPrice(command.priceAmount(), command.priceCurrency());
        Commission commission = new Commission(command.commissionPercentage());

        BrokerageProcess process = BrokerageProcess.create(
                command.propertyId(), address, askingPrice, commission);

        return toResponse(repository.save(process));
    }

    /**
     * Finds a brokerage process by its ID.
     */
    @Transactional(readOnly = true)
    public Optional<BrokerageProcessResponse> findById(UUID id) {
        return repository.findById(id).map(this::toResponse);
    }

    /**
     * Returns all brokerage processes.
     */
    @Transactional(readOnly = true)
    public List<BrokerageProcessResponse> findAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    /**
     * Deletes a brokerage process by its ID.
     */
    public void delete(UUID id) {
        repository.deleteById(id);
    }

    private BrokerageProcessResponse toResponse(BrokerageProcess process) {
        return new BrokerageProcessResponse(
                process.getId(),
                process.getPropertyId(),
                process.getAddress().street(),
                process.getAddress().zipCode(),
                process.getAddress().city(),
                process.getAskingPrice().amount(),
                process.getAskingPrice().currency(),
                process.getCommission().percentage(),
                process.getStatus().name()
        );
    }
}
