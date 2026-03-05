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
 * Pure domain object -- no framework dependencies.
 */
public class BrokerageProcess {

    private final UUID id;
    private final UUID propertyId;
    private Address address;
    private AskingPrice askingPrice;
    private Commission commission;
    private ProcessStatus status;
    private final List<Viewing> viewings;
    private final List<Offer> offers;
    private final transient List<Object> domainEvents;

    public BrokerageProcess(UUID id, UUID propertyId, Address address,
                            AskingPrice askingPrice, Commission commission,
                            ProcessStatus status,
                            List<Viewing> viewings, List<Offer> offers) {
        Objects.requireNonNull(id, "ID must not be null");
        Objects.requireNonNull(propertyId, "Property ID must not be null");
        Objects.requireNonNull(address, "Address must not be null");
        Objects.requireNonNull(askingPrice, "Asking price must not be null");
        Objects.requireNonNull(commission, "Commission must not be null");
        Objects.requireNonNull(status, "Status must not be null");

        this.id = id;
        this.propertyId = propertyId;
        this.address = address;
        this.askingPrice = askingPrice;
        this.commission = commission;
        this.status = status;
        this.viewings = new ArrayList<>(viewings != null ? viewings : List.of());
        this.offers = new ArrayList<>(offers != null ? offers : List.of());
        this.domainEvents = new ArrayList<>();
    }

    /**
     * Factory method to create a new BrokerageProcess with status NEW.
     */
    public static BrokerageProcess create(UUID propertyId, Address address,
                                           AskingPrice askingPrice,
                                           Commission commission) {
        return new BrokerageProcess(
                UUID.randomUUID(),
                propertyId,
                address,
                askingPrice,
                commission,
                ProcessStatus.NEW,
                new ArrayList<>(),
                new ArrayList<>()
        );
    }

    public void addViewing(String prospectName, LocalDateTime scheduledAt, String notes) {
        Viewing viewing = new Viewing(UUID.randomUUID(), prospectName, scheduledAt, notes);
        this.viewings.add(viewing);
        this.status = ProcessStatus.VIEWING;
    }

    public void completeViewing(UUID viewingId) {
        Viewing viewing = viewings.stream()
                .filter(v -> v.getId().equals(viewingId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Viewing with ID " + viewingId + " not found"));

        viewing.complete();

        domainEvents.add(new ViewingCompleted(
                this.id,
                viewingId,
                LocalDateTime.now()
        ));
    }

    public void receiveOffer(String prospectName, BigDecimal amount) {
        Offer offer = new Offer(UUID.randomUUID(), prospectName, amount, LocalDateTime.now());
        this.offers.add(offer);
        this.status = ProcessStatus.OFFER_PHASE;

        domainEvents.add(new OfferReceived(
                this.id,
                amount,
                LocalDateTime.now()
        ));
    }

    public void acceptOffer(UUID offerId) {
        Offer offer = offers.stream()
                .filter(o -> o.getId().equals(offerId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Offer with ID " + offerId + " not found"));

        offer.accept();

        domainEvents.add(new OfferAccepted(
                this.id,
                offerId,
                LocalDateTime.now()
        ));
    }

    public void scheduleNotaryAppointment() {
        boolean hasAcceptedOffer = offers.stream().anyMatch(Offer::isAccepted);

        if (!hasAcceptedOffer) {
            throw new IllegalStateException(
                    "Notary appointment can only be scheduled when at least one accepted offer exists");
        }

        this.status = ProcessStatus.NOTARY_APPOINTMENT;
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
