package de.realestate.integration;

import de.realestate.acquisition.application.service.CloseContractUseCase;
import de.realestate.acquisition.domain.model.BrokerageContract;
import de.realestate.brokerage.domain.model.BrokerageProcess;
import de.realestate.brokerage.domain.port.BrokerageProcessRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        BigDecimal askingPrice = new BigDecimal("450000.00");
        String currency = "EUR";
        BigDecimal commissionPercentage = new BigDecimal("5.95");

        BrokerageContract contract = closeContractUseCase.create(
                ownerId, propertyId, askingPrice, currency, commissionPercentage);

        // When: the BrokerageContract is closed
        closeContractUseCase.close(contract.getId());

        // Then: a BrokerageProcess should have been created with the correct property and pricing
        Optional<BrokerageProcess> process = brokerageProcessRepository.findByPropertyId(propertyId);
        assertTrue(process.isPresent(),
                "A BrokerageProcess should have been created via event listener");
        assertEquals(propertyId, process.get().getPropertyId());
        assertEquals(askingPrice, process.get().getAskingPrice().amount());
        assertEquals(currency, process.get().getAskingPrice().currency());
        assertEquals(commissionPercentage, process.get().getCommission().percentage());
    }
}
