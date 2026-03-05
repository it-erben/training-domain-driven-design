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
    private final LocalDateTime appointmentDate;
    private final String notes;
    private boolean completed;

    public Viewing(UUID id, String prospectName, LocalDateTime appointmentDate, String notes) {
        this.id = Objects.requireNonNull(id, "ID must not be null");
        this.prospectName = Objects.requireNonNull(prospectName, "Prospect name must not be null");
        this.appointmentDate = Objects.requireNonNull(appointmentDate, "Appointment date must not be null");
        this.notes = notes;
        this.completed = false;
    }

    /**
     * Marks this viewing as completed.
     */
    public void complete() {
        this.completed = true;
    }

    public UUID getId() {
        return id;
    }

    public String getProspectName() {
        return prospectName;
    }

    public LocalDateTime getAppointmentDate() {
        return appointmentDate;
    }

    public String getNotes() {
        return notes;
    }

    public boolean isCompleted() {
        return completed;
    }
}
