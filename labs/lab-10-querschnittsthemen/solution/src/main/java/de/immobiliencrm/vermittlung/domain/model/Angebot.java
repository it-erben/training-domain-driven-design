package de.immobiliencrm.vermittlung.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing an offer within the brokerage aggregate.
 */
public class Angebot {

    private final UUID id;
    private final String interessentName;
    private final BigDecimal betrag;
    private final LocalDateTime eingegangen;
    private boolean angenommen;

    public Angebot(UUID id, String interessentName, BigDecimal betrag, LocalDateTime eingegangen) {
        this.id = Objects.requireNonNull(id, "ID darf nicht null sein");
        this.interessentName = Objects.requireNonNull(interessentName, "InteressentName darf nicht null sein");
        this.betrag = Objects.requireNonNull(betrag, "Betrag darf nicht null sein");
        this.eingegangen = Objects.requireNonNull(eingegangen, "Eingegangen darf nicht null sein");
        this.angenommen = false;
    }

    /**
     * Accepts this offer.
     */
    public void annehmen() {
        this.angenommen = true;
    }

    public UUID getId() {
        return id;
    }

    public String getInteressentName() {
        return interessentName;
    }

    public BigDecimal getBetrag() {
        return betrag;
    }

    public LocalDateTime getEingegangen() {
        return eingegangen;
    }

    public boolean isAngenommen() {
        return angenommen;
    }
}
