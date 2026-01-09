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

    Offer(UUID id, String prospectName, BigDecimal amount, LocalDateTime receivedAt) {
        this.id = Objects.requireNonNull(id, "ID must not be null");
        this.prospectName = Objects.requireNonNull(prospectName, "Prospect name must not be null");
        this.amount = Objects.requireNonNull(amount, "Amount must not be null");
        this.receivedAt = Objects.requireNonNull(receivedAt, "ReceivedAt must not be null");
        this.accepted = false;

        if (prospectName.isBlank()) {
            throw new IllegalArgumentException("Prospect name must not be blank");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
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
