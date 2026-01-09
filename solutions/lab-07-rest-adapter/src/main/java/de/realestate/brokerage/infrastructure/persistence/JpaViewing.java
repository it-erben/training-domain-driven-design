package de.realestate.brokerage.infrastructure.persistence;

import de.realestate.brokerage.domain.model.Viewing;
import jakarta.persistence.Embeddable;

import java.time.LocalDateTime;
import java.util.UUID;

@Embeddable
public class JpaViewing {

    private UUID viewingId;
    private String prospectName;
    private LocalDateTime timestamp;
    private String notes;
    private boolean completed;

    protected JpaViewing() {
    }

    public static JpaViewing fromModel(Viewing viewing) {
        JpaViewing jpa = new JpaViewing();
        jpa.viewingId = viewing.getId();
        jpa.prospectName = viewing.getProspectName();
        jpa.timestamp = viewing.getTimestamp();
        jpa.notes = viewing.getNotes();
        jpa.completed = viewing.isCompleted();
        return jpa;
    }

    public Viewing toModel() {
        Viewing viewing = new Viewing(viewingId, prospectName, timestamp);
        viewing.setNotes(notes);
        if (completed) {
            viewing.complete();
        }
        return viewing;
    }
}
