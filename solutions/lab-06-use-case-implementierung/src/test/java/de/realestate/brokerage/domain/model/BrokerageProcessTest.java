package de.realestate.brokerage.domain.model;

import de.realestate.brokerage.domain.event.OfferAccepted;
import de.realestate.brokerage.domain.event.OfferReceived;
import de.realestate.brokerage.domain.event.ViewingCompleted;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BrokerageProcessTest {

    private BrokerageProcess process;

    @BeforeEach
    void setUp() {
        process = BrokerageProcess.create(
                UUID.randomUUID(),
                new AskingPrice(new BigDecimal("350000"), "EUR"),
                new Commission(new BigDecimal("3.57"))
        );
    }

    @Test
    @DisplayName("Aggregate child mutators should not be public")
    void should_hideEntityMutatorsBehindAggregate() throws NoSuchMethodException {
        assertFalse(Modifier.isPublic(Viewing.class.getDeclaredMethod("complete").getModifiers()));
        assertFalse(Modifier.isPublic(Offer.class.getDeclaredMethod("accept").getModifiers()));
    }

    @Test
    @DisplayName("Adding a viewing with a blank prospect name should fail")
    void should_throwException_when_addingViewingWithBlankProspectName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> process.addViewing("   ", LocalDateTime.now().plusDays(1))
        );
    }

    @Test
    @DisplayName("Receiving an offer with a non-positive amount should fail")
    void should_throwException_when_receivingNonPositiveOffer() {
        assertThrows(
                IllegalArgumentException.class,
                () -> process.receiveOffer("Max Mustermann", BigDecimal.ZERO)
        );
    }

    @Test
    @DisplayName("Completing the same viewing twice should fail")
    void should_throwException_when_completingViewingTwice() {
        UUID viewingId = process.addViewing("Erika Musterfrau", LocalDateTime.now().plusDays(1));
        process.completeViewing(viewingId);

        assertThrows(
                IllegalStateException.class,
                () -> process.completeViewing(viewingId)
        );
        assertEquals(1, process.getDomainEvents().size());
        assertInstanceOf(ViewingCompleted.class, process.getDomainEvents().get(0));
    }

    @Test
    @DisplayName("Accepting the same offer twice should fail")
    void should_throwException_when_acceptingOfferTwice() {
        process.receiveOffer("Max Mustermann", new BigDecimal("340000"));
        UUID offerId = process.getOffers().get(0).getId();
        process.acceptOffer(offerId);

        assertThrows(
                IllegalStateException.class,
                () -> process.acceptOffer(offerId)
        );
        assertEquals(2, process.getDomainEvents().size());
        assertInstanceOf(OfferReceived.class, process.getDomainEvents().get(0));
        assertInstanceOf(OfferAccepted.class, process.getDomainEvents().get(1));
    }

    @Test
    @DisplayName("Reconstituting a process should restore state without domain events")
    void should_reconstituteProcessWithoutDomainEvents() {
        Viewing viewing = Viewing.reconstitute(
                UUID.randomUUID(),
                "Erika Musterfrau",
                LocalDateTime.now().minusDays(2),
                "Completed",
                true
        );
        Offer offer = Offer.reconstitute(
                UUID.randomUUID(),
                "Max Mustermann",
                new BigDecimal("345000"),
                LocalDateTime.now().minusDays(1),
                true
        );

        BrokerageProcess restored = BrokerageProcess.reconstitute(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new AskingPrice(new BigDecimal("350000"), "EUR"),
                new Commission(new BigDecimal("3.57")),
                ProcessStatus.NOTARY_APPOINTMENT,
                List.of(viewing),
                List.of(offer)
        );

        assertEquals(ProcessStatus.NOTARY_APPOINTMENT, restored.getStatus());
        assertTrue(restored.getViewings().get(0).isCompleted());
        assertTrue(restored.getOffers().get(0).isAccepted());
        assertTrue(restored.getDomainEvents().isEmpty());
    }

    @Test
    @DisplayName("Returned domain events are a snapshot")
    void should_returnSnapshotOfDomainEvents() {
        process.receiveOffer("Max Mustermann", new BigDecimal("340000"));

        var snapshot = process.getDomainEvents();
        process.clearDomainEvents();

        assertEquals(1, snapshot.size());
        assertTrue(process.getDomainEvents().isEmpty());
    }
}
