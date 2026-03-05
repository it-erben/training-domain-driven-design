package de.realestate.brokerage.infrastructure.persistence;

import de.realestate.brokerage.domain.model.Address;
import de.realestate.brokerage.domain.model.Viewing;
import de.realestate.brokerage.domain.model.AskingPrice;
import de.realestate.brokerage.domain.model.Commission;
import de.realestate.brokerage.domain.model.BrokerageProcess;
import de.realestate.brokerage.domain.model.ProcessStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Repository integration test using @DataJpaTest with an embedded H2 database.
 * Only the JPA layer is started -- no full application context.
 */
@DataJpaTest
@Import(BrokerageProcessRepositoryAdapter.class)
class BrokerageProcessRepositoryAdapterTest {

    @Autowired
    private BrokerageProcessRepositoryAdapter repository;

    @Test
    @DisplayName("save and findById round-trip works correctly")
    void test_saveAndFindById() {
        // Arrange
        BrokerageProcess process = BrokerageProcess.create(
                UUID.randomUUID(),
                new Address("Teststrasse 10", "50667", "Koeln"),
                new AskingPrice(new BigDecimal("250000"), "EUR"),
                new Commission(new BigDecimal("3.57"))
        );

        // Act
        BrokerageProcess saved = repository.save(process);
        Optional<BrokerageProcess> loaded = repository.findById(saved.getId());

        // Assert
        assertTrue(loaded.isPresent());
        BrokerageProcess result = loaded.get();
        assertEquals(saved.getId(), result.getId());
        assertEquals("Teststrasse 10", result.getAddress().street());
        assertEquals("50667", result.getAddress().postalCode());
        assertEquals("Koeln", result.getAddress().city());
        assertEquals(ProcessStatus.NEU, result.getStatus());
    }

    @Test
    @DisplayName("Viewings are correctly persisted and loaded")
    void test_viewingsArePersisted() {
        // Arrange
        BrokerageProcess process = BrokerageProcess.create(
                UUID.randomUUID(),
                new Address("Rheinufer 5", "50667", "Koeln"),
                new AskingPrice(new BigDecimal("450000"), "EUR"),
                new Commission(new BigDecimal("5.00"))
        );
        Viewing viewing = process.addViewing(
                "Hans Mueller",
                LocalDateTime.of(2025, 6, 15, 14, 0),
                "Initial viewing"
        );

        // Act
        BrokerageProcess saved = repository.save(process);
        Optional<BrokerageProcess> loaded = repository.findById(saved.getId());

        // Assert
        assertTrue(loaded.isPresent());
        BrokerageProcess result = loaded.get();
        assertNotNull(result.getViewings());
        assertEquals(1, result.getViewings().size());

        Viewing loadedViewing = result.getViewings().get(0);
        assertEquals(viewing.getId(), loadedViewing.getId());
        assertEquals("Hans Mueller", loadedViewing.getProspectName());
        assertFalse(loadedViewing.isCompleted());
        assertEquals(ProcessStatus.BESICHTIGUNG, result.getStatus());
    }
}
