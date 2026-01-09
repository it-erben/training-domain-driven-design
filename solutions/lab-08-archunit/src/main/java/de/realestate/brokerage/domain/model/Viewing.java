package de.realestate.brokerage.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing a property viewing appointment within the BrokerageProcess aggregate.
 */
public class Viewing {

    private final UUID id;
    private final String prospectName;
    private final LocalDateTime timestamp;
    private String notes;
    private boolean completed;

    public Viewing(UUID id, String prospectName, LocalDateTime timestamp) {
        Objects.requireNonNull(id, "ID must not be null");
        Objects.requireNonNull(prospectName, "Prospect name must not be null");
        Objects.requireNonNull(timestamp, "Timestamp must not be null");
        if (prospectName.isBlank()) {
            throw new IllegalArgumentException("Prospect name must not be blank");
        }

        this.id = id;
        this.prospectName = prospectName;
        this.timestamp = timestamp;
        this.notes = "";
        this.completed = false;
    }

    public static Viewing reconstitute(UUID id, String prospectName,
                                       LocalDateTime timestamp, String notes, boolean completed) {
        Viewing viewing = new Viewing(id, prospectName, timestamp);
        viewing.notes = notes == null ? "" : notes;
        viewing.completed = completed;
        return viewing;
    }

    /**
     * Marks this viewing as completed.
     */
    void complete() {
        this.completed = true;
    }

    public UUID getId() {
        return id;
    }

    public String getProspectName() {
        return prospectName;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getNotes() {
        return notes;
    }

    void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isCompleted() {
        return completed;
    }
}
