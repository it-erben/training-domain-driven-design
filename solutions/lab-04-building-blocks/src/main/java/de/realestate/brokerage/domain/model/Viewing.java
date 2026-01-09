package de.realestate.brokerage.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing a property viewing within the brokerage aggregate.
 */
public class Viewing {

    private final UUID id;
    private final String prospectName;
    private final LocalDateTime scheduledAt;
    private final String notes;
    private boolean completed;

    Viewing(UUID id, String prospectName, LocalDateTime timestamp, String notes) {
        this.id = Objects.requireNonNull(id, "ID must not be null");
        this.prospectName = Objects.requireNonNull(prospectName, "Prospect name must not be null");
        this.scheduledAt = Objects.requireNonNull(timestamp, "Timestamp must not be null");
        this.notes = notes;
        this.completed = false;

        if (prospectName.isBlank()) {
            throw new IllegalArgumentException("Prospect name must not be blank");
        }
    }

    public static Viewing reconstitute(UUID id, String prospectName,
                                       LocalDateTime timestamp, String notes, boolean completed) {
        Viewing viewing = new Viewing(id, prospectName, timestamp, notes);
        viewing.completed = completed;
        return viewing;
    }

    /**
     * Marks this viewing as completed.
     */
    void complete() {
        this.completed = true;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Viewing viewing)) {
            return false;
        }
        return id.equals(viewing.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
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
