package de.realestate.brokerage;

import de.realestate.acquisition.ContractSigned;
import de.realestate.brokerage.domain.port.BrokerageProcessRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test using Spring Modulith's @ApplicationModuleTest.
 * Bootstraps only the Brokerage module and its declared dependencies.
 * Uses the Scenario API to test event-driven interactions.
 */
@ApplicationModuleTest
class BrokerageModuleTest {

    @Autowired
    private BrokerageProcessRepository repository;

    @Test
    void shouldCreateBrokerageProcessOnContractSigned(Scenario scenario) {
        UUID contractId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();

        scenario.publish(new ContractSigned(contractId, propertyId, LocalDateTime.now()))
                .andWaitForStateChange(
                        () -> repository.findAll().stream()
                                .filter(p -> p.getPropertyId().equals(propertyId))
                                .findFirst()
                                .orElse(null),
                        Objects::nonNull)
                .andVerify(process ->
                        assertThat(process.getPropertyId()).isEqualTo(propertyId));
    }
}
