package de.realestate.brokerage.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pure domain unit tests -- no Spring context required.
 * Tests the invariants and behavior of the BrokerageProcess aggregate root.
 */
class BrokerageProcessTest {

    private BrokerageProcess process;

    @BeforeEach
    void setUp() {
        process = BrokerageProcess.create(
                UUID.randomUUID(),
                new Address("Musterstrasse 1", "50667", "Koeln"),
                new AskingPrice(new BigDecimal("350000"), "EUR"),
                new Commission(new BigDecimal("3.57"))
        );
    }

    @Test
    @DisplayName("setStatusToNotaryAppointment() throws IllegalStateException when no accepted offer exists")
    void test_setStatusToNotaryAppointment_withoutAcceptedOffer_throwsException() {
        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> process.setStatusToNotaryAppointment()
        );

        assertTrue(exception.getMessage().contains("accepted offer"));
    }

    @Test
    @DisplayName("setStatusToNotaryAppointment() succeeds when an accepted offer exists")
    void test_setStatusToNotaryAppointment_withAcceptedOffer_succeeds() {
        // Arrange: add and accept an offer
        Offer offer = process.receiveOffer("Max Mustermann", new BigDecimal("340000"));
        process.acceptOffer(offer.getId());

        // Act
        process.setStatusToNotaryAppointment();

        // Assert
        assertEquals(ProcessStatus.NOTARTERMIN, process.getStatus());
    }

    @Test
    @DisplayName("addViewing() creates a Viewing and updates the status")
    void test_addViewing_createsViewing() {
        // Act
        Viewing viewing = process.addViewing(
                "Erika Musterfrau",
                LocalDateTime.now().plusDays(3),
                "Initial viewing"
        );

        // Assert
        assertNotNull(viewing);
        assertNotNull(viewing.getId());
        assertEquals("Erika Musterfrau", viewing.getProspectName());
        assertEquals(1, process.getViewings().size());
        assertEquals(ProcessStatus.BESICHTIGUNG, process.getStatus());
    }

    @Test
    @DisplayName("acceptOffer() sets accepted to true")
    void test_acceptOffer_setsAcceptedToTrue() {
        // Arrange
        Offer offer = process.receiveOffer("Max Mustermann", new BigDecimal("340000"));

        // Act
        process.acceptOffer(offer.getId());

        // Assert
        assertTrue(process.getOffers().get(0).isAccepted());
    }
}
