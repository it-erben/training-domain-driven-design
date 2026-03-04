package de.immobiliencrm.vermittlung.infrastructure.persistence;

import de.immobiliencrm.vermittlung.domain.model.Besichtigung;
import jakarta.persistence.Embeddable;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Embeddable representing a Besichtigung for persistence.
 */
@Embeddable
public class JpaBesichtigung {

    private UUID besichtigungId;
    private String interessentName;
    private LocalDateTime zeitpunkt;
    private String notizen;
    private boolean durchgefuehrt;

    protected JpaBesichtigung() {
    }

    public JpaBesichtigung(UUID besichtigungId, String interessentName,
                           LocalDateTime zeitpunkt, String notizen, boolean durchgefuehrt) {
        this.besichtigungId = besichtigungId;
        this.interessentName = interessentName;
        this.zeitpunkt = zeitpunkt;
        this.notizen = notizen;
        this.durchgefuehrt = durchgefuehrt;
    }

    public Besichtigung toModel() {
        return new Besichtigung(besichtigungId, interessentName, zeitpunkt, notizen, durchgefuehrt);
    }

    public static JpaBesichtigung fromModel(Besichtigung besichtigung) {
        return new JpaBesichtigung(
                besichtigung.getId(),
                besichtigung.getInteressentName(),
                besichtigung.getZeitpunkt(),
                besichtigung.getNotizen(),
                besichtigung.isDurchgefuehrt()
        );
    }

    public UUID getBesichtigungId() {
        return besichtigungId;
    }

    public void setBesichtigungId(UUID besichtigungId) {
        this.besichtigungId = besichtigungId;
    }

    public String getInteressentName() {
        return interessentName;
    }

    public void setInteressentName(String interessentName) {
        this.interessentName = interessentName;
    }

    public LocalDateTime getZeitpunkt() {
        return zeitpunkt;
    }

    public void setZeitpunkt(LocalDateTime zeitpunkt) {
        this.zeitpunkt = zeitpunkt;
    }

    public String getNotizen() {
        return notizen;
    }

    public void setNotizen(String notizen) {
        this.notizen = notizen;
    }

    public boolean isDurchgefuehrt() {
        return durchgefuehrt;
    }

    public void setDurchgefuehrt(boolean durchgefuehrt) {
        this.durchgefuehrt = durchgefuehrt;
    }
}
