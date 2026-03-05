package de.realestate.brokerage.infrastructure.persistence;

import de.realestate.brokerage.domain.model.Offer;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA embeddable for persisting offer data.
 */
@Embeddable
public class JpaOffer {

    private UUID offerId;
    private String prospectName;
    private BigDecimal amount;
    private LocalDateTime receivedAt;
    private boolean accepted;

    protected JpaOffer() {
        // Required by JPA
    }

    /**
     * Converts a domain Offer to its JPA representation.
     */
    public static JpaOffer fromModel(Offer offer) {
        JpaOffer jpa = new JpaOffer();
        jpa.offerId = offer.getId();
        jpa.prospectName = offer.getProspectName();
        jpa.amount = offer.getAmount();
        jpa.receivedAt = offer.getReceivedAt();
        jpa.accepted = offer.isAccepted();
        return jpa;
    }

    /**
     * Converts this JPA representation back to a domain Offer.
     */
    public Offer toModel() {
        Offer offer = new Offer(offerId, prospectName, amount, receivedAt);
        if (accepted) {
            offer.accept();
        }
        return offer;
    }
}
