package de.realestate.brokerage.domain.model;

import de.realestate.brokerage.domain.event.OfferAccepted;
import de.realestate.brokerage.domain.event.OfferReceived;
import de.realestate.brokerage.domain.event.ViewingCompleted;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

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
    @DisplayName("New brokerage process should have status NEW")
    void should_haveStatusNew_when_created() {
        assertEquals(ProcessStatus.NEW, process.getStatus());
    }

    @Test
    @DisplayName("Invariant: status cannot be set to NOTARY_APPOINTMENT without an accepted offer")
    void should_throwException_when_settingNotaryAppointmentWithoutAcceptedOffer() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> process.setStatusToNotaryAppointment()
        );

        assertTrue(exception.getMessage().contains("accepted offer"));
    }

    @Test
    @DisplayName("Invariant: status can be set to NOTARY_APPOINTMENT with an accepted offer")
    void should_succeed_when_settingNotaryAppointmentWithAcceptedOffer() {
        // Arrange: add and accept an offer
        Offer offer = process.receiveOffer("Max Mustermann", new BigDecimal("340000"));
        process.acceptOffer(offer.getId());

        // Act
        process.setStatusToNotaryAppointment();

        // Assert
        assertEquals(ProcessStatus.NOTARY_APPOINTMENT, process.getStatus());
    }

    @Test
    @DisplayName("Complete flow: viewing, offer, notary appointment")
    void should_completeFullFlow() {
        // Step 1: Add a viewing
        Viewing viewing = process.addViewing(
                "Erika Musterfrau",
                LocalDateTime.now().plusDays(3),
                "Initial viewing"
        );
        assertEquals(ProcessStatus.VIEWING, process.getStatus());
        assertEquals(1, process.getViewings().size());

        // Step 2: Complete the viewing
        process.completeViewing(viewing.getId());
        assertTrue(process.getViewings().get(0).isCompleted());

        // Step 3: Receive an offer
        Offer offer = process.receiveOffer("Erika Musterfrau", new BigDecimal("345000"));
        assertEquals(ProcessStatus.OFFER_PHASE, process.getStatus());
        assertEquals(1, process.getOffers().size());

        // Step 4: Accept the offer
        process.acceptOffer(offer.getId());
        assertTrue(process.getOffers().get(0).isAccepted());

        // Step 5: Set status to NOTARY_APPOINTMENT
        process.setStatusToNotaryAppointment();
        assertEquals(ProcessStatus.NOTARY_APPOINTMENT, process.getStatus());

        // Verify domain events were collected
        assertEquals(3, process.getDomainEvents().size());
        assertInstanceOf(ViewingCompleted.class, process.getDomainEvents().get(0));
        assertInstanceOf(OfferReceived.class, process.getDomainEvents().get(1));
        assertInstanceOf(OfferAccepted.class, process.getDomainEvents().get(2));
    }

    @Test
    @DisplayName("Completing a viewing with unknown ID should throw exception")
    void should_throwException_when_completingViewingWithUnknownId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> process.completeViewing(UUID.randomUUID())
        );
    }

    @Test
    @DisplayName("Accepting an offer with unknown ID should throw exception")
    void should_throwException_when_acceptingOfferWithUnknownId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> process.acceptOffer(UUID.randomUUID())
        );
    }

    @Test
    @DisplayName("Domain events can be cleared")
    void should_clearDomainEvents() {
        process.receiveOffer("Max Mustermann", new BigDecimal("340000"));
        assertEquals(1, process.getDomainEvents().size());

        process.clearDomainEvents();
        assertEquals(0, process.getDomainEvents().size());
    }

    @Test
    @DisplayName("Returned domain events are a snapshot and stay unchanged after clearing")
    void should_returnSnapshotOfDomainEvents() {
        process.receiveOffer("Max Mustermann", new BigDecimal("340000"));

        List<?> snapshot = process.getDomainEvents();
        process.clearDomainEvents();

        assertEquals(1, snapshot.size());
        assertEquals(0, process.getDomainEvents().size());
    }

    @Test
    @DisplayName("Completing the same viewing twice should fail")
    void should_throwException_when_completingViewingTwice() {
        Viewing viewing = process.addViewing("Erika Musterfrau", LocalDateTime.now().plusDays(1), "Initial");
        process.completeViewing(viewing.getId());

        assertThrows(
                IllegalStateException.class,
                () -> process.completeViewing(viewing.getId())
        );
        assertEquals(1, process.getDomainEvents().size());
    }

    @Test
    @DisplayName("Accepting the same offer twice should fail")
    void should_throwException_when_acceptingOfferTwice() {
        Offer offer = process.receiveOffer("Max Mustermann", new BigDecimal("340000"));
        process.acceptOffer(offer.getId());

        assertThrows(
                IllegalStateException.class,
                () -> process.acceptOffer(offer.getId())
        );
        assertEquals(2, process.getDomainEvents().size());
    }

    @Test
    @DisplayName("Viewings are equal when they share the same identity")
    void should_compareViewingsById() {
        UUID viewingId = UUID.randomUUID();
        Viewing first = new Viewing(viewingId, "Erika Musterfrau", LocalDateTime.now(), "First");
        Viewing second = new Viewing(viewingId, "Max Mustermann", LocalDateTime.now().plusDays(1), "Second");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    @DisplayName("Offers are equal when they share the same identity")
    void should_compareOffersById() {
        UUID offerId = UUID.randomUUID();
        Offer first = new Offer(offerId, "Erika Musterfrau", new BigDecimal("300000"), LocalDateTime.now());
        Offer second = new Offer(offerId, "Max Mustermann", new BigDecimal("320000"), LocalDateTime.now().plusDays(1));

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
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
    @DisplayName("Adding a viewing with a blank prospect name should fail")
    void should_throwException_when_addingViewingWithBlankProspectName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> process.addViewing("   ", LocalDateTime.now().plusDays(1), "Initial")
        );
    }

    @Test
    @DisplayName("Aggregate child mutators should not be public")
    void should_hideEntityMutatorsBehindAggregate() throws NoSuchMethodException {
        assertFalse(Modifier.isPublic(Viewing.class.getDeclaredMethod("complete").getModifiers()));
        assertFalse(Modifier.isPublic(Offer.class.getDeclaredMethod("accept").getModifiers()));
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
}
