package de.realestate.brokerage.application.listener;

import de.realestate.acquisition.domain.event.ContractSigned;
import de.realestate.brokerage.domain.model.AskingPrice;
import de.realestate.brokerage.domain.model.Commission;
import de.realestate.brokerage.domain.model.BrokerageProcess;
import de.realestate.brokerage.domain.port.BrokerageProcessRepository;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

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
        AskingPrice askingPrice = new AskingPrice(event.askingPrice(), event.currency());
        Commission commission = new Commission(event.commissionPercentage());

        BrokerageProcess process = BrokerageProcess.create(
                event.propertyId(), askingPrice, commission);

        repository.save(process);
    }
}
