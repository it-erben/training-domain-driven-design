package de.realestate.brokerage.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import de.realestate.brokerage.domain.event.OfferAccepted;
import de.realestate.brokerage.domain.event.OfferReceived;
import de.realestate.brokerage.domain.event.ViewingCompleted;

/**
 * Aggregate Root representing a property brokerage process.
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
        this.id = id;
        this.propertyId = propertyId;
        this.address = address;
        this.askingPrice = askingPrice;
        this.commission = commission;
        this.status = ProcessStatus.NEW;
        this.viewings = new ArrayList<>();
        this.offers = new ArrayList<>();
        this.domainEvents = new ArrayList<>();
    }

    /**
     * Factory method to create a new BrokerageProcess.
     */
    public static BrokerageProcess create(UUID propertyId, Address address,
                                                 AskingPrice askingPrice,
                                                 Commission commission) {
        Objects.requireNonNull(propertyId, "PropertyId must not be null");
        Objects.requireNonNull(address, "Address must not be null");
        Objects.requireNonNull(askingPrice, "AskingPrice must not be null");
        Objects.requireNonNull(commission, "Commission must not be null");

        return new BrokerageProcess(UUID.randomUUID(), propertyId, address,
                askingPrice, commission);
    }

    /**
     * Adds a new viewing appointment and returns the generated Viewing ID.
     */
    public UUID addViewing(String prospectName, LocalDateTime timestamp) {
        Objects.requireNonNull(prospectName, "Prospect name must not be null");
        Objects.requireNonNull(timestamp, "Timestamp must not be null");

        UUID viewingId = UUID.randomUUID();
        Viewing viewing = new Viewing(viewingId, prospectName, timestamp);
        viewings.add(viewing);
        status = ProcessStatus.VIEWING;
        return viewingId;
    }

    /**
     * Marks a viewing as completed and raises a domain event.
     */
    public void completeViewing(UUID viewingId) {
        Viewing viewing = viewings.stream()
                .filter(b -> b.getId().equals(viewingId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Viewing with ID " + viewingId + " not found"));

        viewing.complete();
        domainEvents.add(new ViewingCompleted(id, viewingId, LocalDateTime.now()));
    }

    /**
     * Receives a new offer and raises a domain event.
     */
    public void receiveOffer(String prospectName, BigDecimal amount) {
        UUID offerId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Offer offer = new Offer(offerId, prospectName, amount, now);
        offers.add(offer);
        status = ProcessStatus.OFFER_PHASE;
        domainEvents.add(new OfferReceived(id, amount, now));
    }

    /**
     * Accepts an offer and raises a domain event.
     */
    public void acceptOffer(UUID offerId) {
        Offer offer = offers.stream()
                .filter(a -> a.getId().equals(offerId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Offer with ID " + offerId + " not found"));

        offer.accept();
        domainEvents.add(new OfferAccepted(id, offerId, LocalDateTime.now()));
    }

    /**
     * Sets the status to NOTARY_APPOINTMENT. Requires at least one accepted offer.
     */
    public void setStatusToNotaryAppointment() {
        boolean hasAcceptedOffer = offers.stream().anyMatch(Offer::isAccepted);
        if (!hasAcceptedOffer) {
            throw new IllegalStateException(
                    "Status cannot be set to NOTARY_APPOINTMENT: no accepted offer present");
        }
        status = ProcessStatus.NOTARY_APPOINTMENT;
    }

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

    public void clearDomainEvents() {
        domainEvents.clear();
    }
}
