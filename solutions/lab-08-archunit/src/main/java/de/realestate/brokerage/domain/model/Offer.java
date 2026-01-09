package de.realestate.brokerage.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing an offer within the BrokerageProcess aggregate.
 */
public class Offer {

    private final UUID id;
    private final String prospectName;
    private final BigDecimal amount;
    private final LocalDateTime receivedAt;
    private boolean accepted;

    public Offer(UUID id, String prospectName, BigDecimal amount, LocalDateTime receivedAt) {
        Objects.requireNonNull(id, "ID must not be null");
        Objects.requireNonNull(prospectName, "Prospect name must not be null");
        Objects.requireNonNull(amount, "Amount must not be null");
        Objects.requireNonNull(receivedAt, "ReceivedAt must not be null");
        if (prospectName.isBlank()) {
            throw new IllegalArgumentException("Prospect name must not be blank");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        this.id = id;
        this.prospectName = prospectName;
        this.amount = amount;
        this.receivedAt = receivedAt;
        this.accepted = false;
    }

    public static Offer reconstitute(UUID id, String prospectName,
                                     BigDecimal amount, LocalDateTime receivedAt, boolean accepted) {
        Offer offer = new Offer(id, prospectName, amount, receivedAt);
        offer.accepted = accepted;
        return offer;
    }

    /**
     * Accepts this offer.
     */
    void accept() {
        this.accepted = true;
    }

    public UUID getId() {
        return id;
    }

    public String getProspectName() {
        return prospectName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public boolean isAccepted() {
        return accepted;
    }
}
