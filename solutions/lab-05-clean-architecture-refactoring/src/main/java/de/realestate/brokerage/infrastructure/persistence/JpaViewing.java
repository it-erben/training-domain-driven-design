package de.realestate.brokerage.infrastructure.persistence;

import de.realestate.brokerage.domain.model.Viewing;
import jakarta.persistence.Embeddable;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Embeddable representing a Viewing for persistence.
 */
@Embeddable
public class JpaViewing {

    private UUID viewingId;
    private String prospectName;
    private LocalDateTime scheduledAt;
    private String notes;
    private boolean completed;

    protected JpaViewing() {
    }

    public JpaViewing(UUID viewingId, String prospectName,
                      LocalDateTime scheduledAt, String notes, boolean completed) {
        this.viewingId = viewingId;
        this.prospectName = prospectName;
        this.scheduledAt = scheduledAt;
        this.notes = notes;
        this.completed = completed;
    }

    public Viewing toModel() {
        return Viewing.reconstitute(viewingId, prospectName, scheduledAt, notes, completed);
    }

    public static JpaViewing fromModel(Viewing viewing) {
        return new JpaViewing(
                viewing.getId(),
                viewing.getProspectName(),
                viewing.getScheduledAt(),
                viewing.getNotes(),
                viewing.isCompleted()
        );
    }

    public UUID getViewingId() {
        return viewingId;
    }

    public void setViewingId(UUID viewingId) {
        this.viewingId = viewingId;
    }

    public String getProspectName() {
        return prospectName;
    }

    public void setProspectName(String prospectName) {
        this.prospectName = prospectName;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
