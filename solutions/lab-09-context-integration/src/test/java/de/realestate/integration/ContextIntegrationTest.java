package de.realestate.integration;

import de.realestate.acquisition.application.service.CloseContractUseCase;
import de.realestate.acquisition.domain.model.BrokerageContract;
import de.realestate.brokerage.domain.port.BrokerageProcessRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Integration test verifying that closing a BrokerageContract in the Acquisition BC
 * automatically creates a BrokerageProcess in the Brokerage BC via events.
 */
@SpringBootTest
class ContextIntegrationTest {

    @Autowired
    private CloseContractUseCase closeContractUseCase;

    @Autowired
    private BrokerageProcessRepository brokerageProcessRepository;

    @Test
    void shouldCreateBrokerageProcessWhenContractIsSigned() {
        // Given: a new BrokerageContract is created
        UUID ownerId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        BrokerageContract contract = closeContractUseCase.create(ownerId, propertyId);

        // When: the BrokerageContract is closed
        closeContractUseCase.close(contract.getId());

        // Then: a BrokerageProcess should have been created automatically
        assertFalse(brokerageProcessRepository.findAll().isEmpty(),
                "A BrokerageProcess should have been created via event listener");
    }
}
