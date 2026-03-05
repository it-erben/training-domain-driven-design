package de.realestate.brokerage.application.listener;

import de.realestate.acquisition.domain.event.ContractSigned;
import de.realestate.brokerage.domain.model.Address;
import de.realestate.brokerage.domain.model.AskingPrice;
import de.realestate.brokerage.domain.model.Commission;
import de.realestate.brokerage.domain.model.BrokerageProcess;
import de.realestate.brokerage.domain.port.BrokerageProcessRepository;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Listener that reacts to ContractSigned events from the Acquisition BC
 * and creates a new BrokerageProcess in the Brokerage BC.
 */
@Component
public class ContractSignedListener {

    private final BrokerageProcessRepository repository;

    public ContractSignedListener(BrokerageProcessRepository repository) {
        this.repository = repository;
    }

    @EventListener
    public void handle(ContractSigned event) {
        // Create a new BrokerageProcess with default values for the address and pricing.
        // In a real application, these would be resolved from additional context or services.
        Address address = new Address("To be entered", "00000", "Unknown");
        AskingPrice askingPrice = new AskingPrice(new BigDecimal("1"), "EUR");
        Commission commission = new Commission(new BigDecimal("3.57"));

        BrokerageProcess process = BrokerageProcess.create(
                event.propertyId(), address, askingPrice, commission);

        repository.save(process);
    }
}
