package de.realestate.brokerage.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing an offer within the brokerage aggregate.
 */
public class Offer {

    private final UUID id;
    private final String prospectName;
    private final BigDecimal amount;
    private final LocalDateTime receivedAt;
    private boolean accepted;

    public Offer(UUID id, String prospectName, BigDecimal amount, LocalDateTime receivedAt) {
        this.id = Objects.requireNonNull(id, "ID must not be null");
        this.prospectName = Objects.requireNonNull(prospectName, "Prospect name must not be null");
        this.amount = Objects.requireNonNull(amount, "Amount must not be null");
        this.receivedAt = Objects.requireNonNull(receivedAt, "Received date must not be null");
        this.accepted = false;
    }

    /**
     * Accepts this offer.
     */
    public void accept() {
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
