package de.realestate.brokerage.application.listener;

import de.realestate.acquisition.domain.event.ContractSigned;
import de.realestate.brokerage.domain.model.Address;
import de.realestate.brokerage.domain.model.AskingPrice;
import de.realestate.brokerage.domain.model.Commission;
import de.realestate.brokerage.domain.model.BrokerageProcess;
import de.realestate.brokerage.domain.port.BrokerageProcessRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Listener that creates a new BrokerageProcess when a contract is signed.
 */
@Component
public class ContractSignedListener {

    private static final Logger log = LoggerFactory.getLogger(ContractSignedListener.class);

    private final BrokerageProcessRepository repository;

    public ContractSignedListener(BrokerageProcessRepository repository) {
        this.repository = repository;
    }

    @EventListener
    public void handle(ContractSigned event) {
        log.info("Received ContractSigned event for propertyId={}", event.propertyId());

        // Create a new BrokerageProcess based on the event data
        BrokerageProcess process = BrokerageProcess.create(
                event.propertyId(),
                new Address("Default Street 1", "00000", "Unknown"),
                new AskingPrice(new BigDecimal("100000"), "EUR"),
                new Commission(new BigDecimal("3.57"))
        );

        repository.save(process);

        log.info("Created BrokerageProcess with id={} for propertyId={}",
                process.getId(), event.propertyId());
    }
}
