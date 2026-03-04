package de.immobiliencrm.vermittlung.infrastructure.persistence;

import de.immobiliencrm.vermittlung.domain.model.Besichtigung;
import jakarta.persistence.Embeddable;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA embeddable for persisting viewing data.
 */
@Embeddable
public class JpaBesichtigung {

    private UUID besichtigungId;
    private String interessentName;
    private LocalDateTime zeitpunkt;
    private String notizen;
    private boolean durchgefuehrt;

    protected JpaBesichtigung() {
        // Required by JPA
    }

    /**
     * Converts a domain Besichtigung to its JPA representation.
     */
    public static JpaBesichtigung fromModel(Besichtigung besichtigung) {
        JpaBesichtigung jpa = new JpaBesichtigung();
        jpa.besichtigungId = besichtigung.getId();
        jpa.interessentName = besichtigung.getInteressentName();
        jpa.zeitpunkt = besichtigung.getZeitpunkt();
        jpa.notizen = besichtigung.getNotizen();
        jpa.durchgefuehrt = besichtigung.isDurchgefuehrt();
        return jpa;
    }

    /**
     * Converts this JPA representation back to a domain Besichtigung.
     */
    public Besichtigung toModel() {
        Besichtigung besichtigung = new Besichtigung(besichtigungId, interessentName, zeitpunkt, notizen);
        if (durchgefuehrt) {
            besichtigung.durchfuehren();
        }
        return besichtigung;
    }
}
