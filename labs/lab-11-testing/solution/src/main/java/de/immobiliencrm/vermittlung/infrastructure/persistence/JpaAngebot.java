package de.immobiliencrm.vermittlung.infrastructure.persistence;

import de.immobiliencrm.vermittlung.domain.model.Angebot;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA embeddable for persisting offer data.
 */
@Embeddable
public class JpaAngebot {

    private UUID angebotId;
    private String interessentName;
    private BigDecimal betrag;
    private LocalDateTime eingegangen;
    private boolean angenommen;

    protected JpaAngebot() {
        // Required by JPA
    }

    /**
     * Converts a domain Angebot to its JPA representation.
     */
    public static JpaAngebot fromModel(Angebot angebot) {
        JpaAngebot jpa = new JpaAngebot();
        jpa.angebotId = angebot.getId();
        jpa.interessentName = angebot.getInteressentName();
        jpa.betrag = angebot.getBetrag();
        jpa.eingegangen = angebot.getEingegangen();
        jpa.angenommen = angebot.isAngenommen();
        return jpa;
    }

    /**
     * Converts this JPA representation back to a domain Angebot.
     */
    public Angebot toModel() {
        Angebot angebot = new Angebot(angebotId, interessentName, betrag, eingegangen);
        if (angenommen) {
            angebot.annehmen();
        }
        return angebot;
    }
}
