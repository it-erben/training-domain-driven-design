package de.realestate.brokerage.infrastructure.persistence;

import de.realestate.brokerage.domain.model.Offer;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Embeddable representing an Offer for persistence.
 */
@Embeddable
public class JpaOffer {

    private UUID offerId;
    private String prospectName;
    private BigDecimal amount;
    private LocalDateTime receivedAt;
    private boolean accepted;

    protected JpaOffer() {
    }

    public JpaOffer(UUID offerId, String prospectName, BigDecimal amount,
                    LocalDateTime receivedAt, boolean accepted) {
        this.offerId = offerId;
        this.prospectName = prospectName;
        this.amount = amount;
        this.receivedAt = receivedAt;
        this.accepted = accepted;
    }

    public Offer toModel() {
        return new Offer(offerId, prospectName, amount, receivedAt, accepted);
    }

    public static JpaOffer fromModel(Offer offer) {
        return new JpaOffer(
                offer.getId(),
                offer.getProspectName(),
                offer.getAmount(),
                offer.getReceivedAt(),
                offer.isAccepted()
        );
    }

    public UUID getOfferId() {
        return offerId;
    }

    public void setOfferId(UUID offerId) {
        this.offerId = offerId;
    }

    public String getProspectName() {
        return prospectName;
    }

    public void setProspectName(String prospectName) {
        this.prospectName = prospectName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }
}
