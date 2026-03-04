package de.immobiliencrm.vermittlung.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing a property viewing within the Vermittlungsvorgang aggregate.
 */
public class Besichtigung {

    private final UUID id;
    private final String interessentName;
    private final LocalDateTime zeitpunkt;
    private final String notizen;
    private boolean durchgefuehrt;

    public Besichtigung(UUID id, String interessentName, LocalDateTime zeitpunkt, String notizen) {
        Objects.requireNonNull(id, "ID darf nicht null sein");
        Objects.requireNonNull(interessentName, "InteressentName darf nicht null sein");
        Objects.requireNonNull(zeitpunkt, "Zeitpunkt darf nicht null sein");

        this.id = id;
        this.interessentName = interessentName;
        this.zeitpunkt = zeitpunkt;
        this.notizen = notizen;
        this.durchgefuehrt = false;
    }

    public Besichtigung(UUID id, String interessentName, LocalDateTime zeitpunkt, String notizen, boolean durchgefuehrt) {
        Objects.requireNonNull(id, "ID darf nicht null sein");
        Objects.requireNonNull(interessentName, "InteressentName darf nicht null sein");
        Objects.requireNonNull(zeitpunkt, "Zeitpunkt darf nicht null sein");

        this.id = id;
        this.interessentName = interessentName;
        this.zeitpunkt = zeitpunkt;
        this.notizen = notizen;
        this.durchgefuehrt = durchgefuehrt;
    }

    public void durchfuehren() {
        this.durchgefuehrt = true;
    }

    public UUID getId() {
        return id;
    }

    public String getInteressentName() {
        return interessentName;
    }

    public LocalDateTime getZeitpunkt() {
        return zeitpunkt;
    }

    public String getNotizen() {
        return notizen;
    }

    public boolean isDurchgefuehrt() {
        return durchgefuehrt;
    }
}
