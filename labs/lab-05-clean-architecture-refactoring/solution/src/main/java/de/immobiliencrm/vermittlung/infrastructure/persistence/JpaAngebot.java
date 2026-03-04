package de.immobiliencrm.vermittlung.infrastructure.persistence;

import de.immobiliencrm.vermittlung.domain.model.Angebot;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Embeddable representing an Angebot for persistence.
 */
@Embeddable
public class JpaAngebot {

    private UUID angebotId;
    private String interessentName;
    private BigDecimal betrag;
    private LocalDateTime eingegangen;
    private boolean angenommen;

    protected JpaAngebot() {
    }

    public JpaAngebot(UUID angebotId, String interessentName, BigDecimal betrag,
                      LocalDateTime eingegangen, boolean angenommen) {
        this.angebotId = angebotId;
        this.interessentName = interessentName;
        this.betrag = betrag;
        this.eingegangen = eingegangen;
        this.angenommen = angenommen;
    }

    public Angebot toModel() {
        return new Angebot(angebotId, interessentName, betrag, eingegangen, angenommen);
    }

    public static JpaAngebot fromModel(Angebot angebot) {
        return new JpaAngebot(
                angebot.getId(),
                angebot.getInteressentName(),
                angebot.getBetrag(),
                angebot.getEingegangen(),
                angebot.isAngenommen()
        );
    }

    public UUID getAngebotId() {
        return angebotId;
    }

    public void setAngebotId(UUID angebotId) {
        this.angebotId = angebotId;
    }

    public String getInteressentName() {
        return interessentName;
    }

    public void setInteressentName(String interessentName) {
        this.interessentName = interessentName;
    }

    public BigDecimal getBetrag() {
        return betrag;
    }

    public void setBetrag(BigDecimal betrag) {
        this.betrag = betrag;
    }

    public LocalDateTime getEingegangen() {
        return eingegangen;
    }

    public void setEingegangen(LocalDateTime eingegangen) {
        this.eingegangen = eingegangen;
    }

    public boolean isAngenommen() {
        return angenommen;
    }

    public void setAngenommen(boolean angenommen) {
        this.angenommen = angenommen;
    }
}
