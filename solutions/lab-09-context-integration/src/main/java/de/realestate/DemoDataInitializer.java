package de.realestate;

import de.realestate.acquisition.application.service.CloseContractUseCase;
import de.realestate.acquisition.domain.model.BrokerageContract;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Initializes demo data by creating and closing brokerage contracts.
 * The BrokerageProcesses are created automatically via the ContractSigned event.
 */
@Component
public class DemoDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);

    private final CloseContractUseCase closeContractUseCase;

    public DemoDataInitializer(CloseContractUseCase closeContractUseCase) {
        this.closeContractUseCase = closeContractUseCase;
    }

    @Override
    public void run(String... args) {
        BrokerageContract contract1 = closeContractUseCase.create(
                UUID.randomUUID(),
                UUID.fromString("33333333-3333-3333-3333-333333333333"),
                new BigDecimal("500000.00"), "EUR", new BigDecimal("3.57"));
        closeContractUseCase.close(contract1.getId());

        BrokerageContract contract2 = closeContractUseCase.create(
                UUID.randomUUID(),
                UUID.fromString("44444444-4444-4444-4444-444444444444"),
                new BigDecimal("725000.00"), "EUR", new BigDecimal("3.57"));
        closeContractUseCase.close(contract2.getId());

        log.info("Demo data ready. Two contracts closed, two BrokerageProcesses created via event flow.");
    }
}
