package de.realestate.brokerage.domain.model;

import de.realestate.brokerage.domain.event.OfferAccepted;
import de.realestate.brokerage.domain.event.OfferReceived;
import de.realestate.brokerage.domain.event.ViewingCompleted;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root for the brokerage process.
 * Enforces invariants and collects domain events.
 */
public class BrokerageProcess {

    private final UUID id;
    private final UUID propertyId;
    private final Address address;
    private final AskingPrice askingPrice;
    private final Commission commission;
    private ProcessStatus status;
    private final List<Viewing> viewings;
    private final List<Offer> offers;
    private final transient List<Object> domainEvents;

    private BrokerageProcess(UUID id, UUID propertyId, Address address,
                             AskingPrice askingPrice, Commission commission) {
        this.id = Objects.requireNonNull(id, "ID must not be null");
        this.propertyId = Objects.requireNonNull(propertyId, "Property ID must not be null");
        this.address = Objects.requireNonNull(address, "Address must not be null");
        this.askingPrice = Objects.requireNonNull(askingPrice, "Asking price must not be null");
        this.commission = Objects.requireNonNull(commission, "Commission must not be null");
        this.status = ProcessStatus.NEW;
        this.viewings = new ArrayList<>();
        this.offers = new ArrayList<>();
        this.domainEvents = new ArrayList<>();
    }

    /**
     * Factory method to create a new brokerage process.
     */
    public static BrokerageProcess create(UUID propertyId, Address address,
                                           AskingPrice askingPrice, Commission commission) {
        return new BrokerageProcess(UUID.randomUUID(), propertyId, address, askingPrice, commission);
    }

    /**
     * Adds a new viewing to this brokerage process and sets the status to VIEWING.
     */
    public Viewing addViewing(String prospectName, LocalDateTime timestamp, String notes) {
        Viewing viewing = new Viewing(UUID.randomUUID(), prospectName, timestamp, notes);
        this.viewings.add(viewing);
        this.status = ProcessStatus.VIEWING;
        return viewing;
    }

    /**
     * Marks an existing viewing as completed and raises a domain event.
     */
    public void completeViewing(UUID viewingId) {
        Viewing viewing = viewings.stream()
                .filter(v -> v.getId().equals(viewingId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Viewing with ID " + viewingId + " not found"));

        viewing.complete();

        domainEvents.add(new ViewingCompleted(this.id, viewingId, LocalDateTime.now()));
    }

    /**
     * Receives a new offer, sets the status to OFFER_PHASE, and raises a domain event.
     */
    public Offer receiveOffer(String prospectName, BigDecimal amount) {
        Offer offer = new Offer(UUID.randomUUID(), prospectName, amount, LocalDateTime.now());
        this.offers.add(offer);
        this.status = ProcessStatus.OFFER_PHASE;

        domainEvents.add(new OfferReceived(this.id, amount, offer.getReceivedAt()));
        return offer;
    }

    /**
     * Accepts an existing offer and raises a domain event.
     */
    public void acceptOffer(UUID offerId) {
        Offer offer = offers.stream()
                .filter(o -> o.getId().equals(offerId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Offer with ID " + offerId + " not found"));

        offer.accept();

        domainEvents.add(new OfferAccepted(this.id, offerId, LocalDateTime.now()));
    }

    /**
     * Sets the status to NOTARY_APPOINTMENT.
     * Invariant: at least one accepted offer must exist.
     *
     * @throws IllegalStateException if no accepted offer exists
     */
    public void setStatusToNotaryAppointment() {
        boolean hasAcceptedOffer = offers.stream().anyMatch(Offer::isAccepted);

        if (!hasAcceptedOffer) {
            throw new IllegalStateException(
                    "Status can only be set to NOTARY_APPOINTMENT when an accepted offer exists");
        }

        this.status = ProcessStatus.NOTARY_APPOINTMENT;
    }

    // --- Getters ---

    public UUID getId() {
        return id;
    }

    public UUID getPropertyId() {
        return propertyId;
    }

    public Address getAddress() {
        return address;
    }

    public AskingPrice getAskingPrice() {
        return askingPrice;
    }

    public Commission getCommission() {
        return commission;
    }

    public ProcessStatus getStatus() {
        return status;
    }

    public List<Viewing> getViewings() {
        return Collections.unmodifiableList(viewings);
    }

    public List<Offer> getOffers() {
        return Collections.unmodifiableList(offers);
    }

    public List<Object> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    /**
     * Clears all collected domain events (typically called after events have been published).
     */
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
}
