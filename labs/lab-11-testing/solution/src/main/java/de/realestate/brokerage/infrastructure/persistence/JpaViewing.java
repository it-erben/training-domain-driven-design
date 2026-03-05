package de.realestate.brokerage.infrastructure.persistence;

import de.realestate.brokerage.domain.model.Viewing;
import jakarta.persistence.Embeddable;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA embeddable for persisting viewing data.
 */
@Embeddable
public class JpaViewing {

    private UUID viewingId;
    private String prospectName;
    private LocalDateTime appointmentDate;
    private String notes;
    private boolean completed;

    protected JpaViewing() {
        // Required by JPA
    }

    /**
     * Converts a domain Viewing to its JPA representation.
     */
    public static JpaViewing fromModel(Viewing viewing) {
        JpaViewing jpa = new JpaViewing();
        jpa.viewingId = viewing.getId();
        jpa.prospectName = viewing.getProspectName();
        jpa.appointmentDate = viewing.getAppointmentDate();
        jpa.notes = viewing.getNotes();
        jpa.completed = viewing.isCompleted();
        return jpa;
    }

    /**
     * Converts this JPA representation back to a domain Viewing.
     */
    public Viewing toModel() {
        Viewing viewing = new Viewing(viewingId, prospectName, appointmentDate, notes);
        if (completed) {
            viewing.complete();
        }
        return viewing;
    }
}
