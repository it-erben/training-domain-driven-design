package de.realestate.brokerage.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing a property viewing within the BrokerageProcess aggregate.
 */
public class Viewing {

    private final UUID id;
    private final String prospectName;
    private final LocalDateTime scheduledAt;
    private final String notes;
    private boolean completed;

    public Viewing(UUID id, String prospectName, LocalDateTime scheduledAt, String notes) {
        Objects.requireNonNull(id, "ID must not be null");
        Objects.requireNonNull(prospectName, "Prospect name must not be null");
        Objects.requireNonNull(scheduledAt, "Scheduled time must not be null");

        this.id = id;
        this.prospectName = prospectName;
        this.scheduledAt = scheduledAt;
        this.notes = notes;
        this.completed = false;
    }

    public Viewing(UUID id, String prospectName, LocalDateTime scheduledAt, String notes, boolean completed) {
        Objects.requireNonNull(id, "ID must not be null");
        Objects.requireNonNull(prospectName, "Prospect name must not be null");
        Objects.requireNonNull(scheduledAt, "Scheduled time must not be null");

        this.id = id;
        this.prospectName = prospectName;
        this.scheduledAt = scheduledAt;
        this.notes = notes;
        this.completed = completed;
    }

    public void complete() {
        this.completed = true;
    }

    public UUID getId() {
        return id;
    }

    public String getProspectName() {
        return prospectName;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public String getNotes() {
        return notes;
    }

    public boolean isCompleted() {
        return completed;
    }
}
