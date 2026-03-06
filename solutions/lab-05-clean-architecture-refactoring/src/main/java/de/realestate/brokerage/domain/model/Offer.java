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
        Objects.requireNonNull(receivedAt, "Received time must not be null");

        this.id = id;
        this.prospectName = prospectName;
        this.amount = amount;
        this.receivedAt = receivedAt;
        this.accepted = false;
    }

    public Offer(UUID id, String prospectName, BigDecimal amount, LocalDateTime receivedAt, boolean accepted) {
        Objects.requireNonNull(id, "ID must not be null");
        Objects.requireNonNull(prospectName, "Prospect name must not be null");
        Objects.requireNonNull(amount, "Amount must not be null");
        Objects.requireNonNull(receivedAt, "Received time must not be null");

        this.id = id;
        this.prospectName = prospectName;
        this.amount = amount;
        this.receivedAt = receivedAt;
        this.accepted = accepted;
    }

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
