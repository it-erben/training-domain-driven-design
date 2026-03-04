package de.immobiliencrm.vermittlung.domain.model;

import de.immobiliencrm.vermittlung.domain.event.AngebotAngenommen;
import de.immobiliencrm.vermittlung.domain.event.AngebotEingegangen;
import de.immobiliencrm.vermittlung.domain.event.BesichtigungDurchgefuehrt;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root for the brokerage process.
 * Enforces invariants and collects domain events.
 */
public class Vermittlungsvorgang {

    private final UUID id;
    private final UUID immobilieId;
    private final Adresse adresse;
    private final Preisvorstellung preisvorstellung;
    private final Provision provision;
    private VermittlungsvorgangStatus status;
    private final List<Besichtigung> besichtigungen;
    private final List<Angebot> angebote;
    private final transient List<Object> domainEvents;

    private Vermittlungsvorgang(UUID id, UUID immobilieId, Adresse adresse,
                                Preisvorstellung preisvorstellung, Provision provision) {
        this.id = Objects.requireNonNull(id, "ID darf nicht null sein");
        this.immobilieId = Objects.requireNonNull(immobilieId, "ImmobilieId darf nicht null sein");
        this.adresse = Objects.requireNonNull(adresse, "Adresse darf nicht null sein");
        this.preisvorstellung = Objects.requireNonNull(preisvorstellung, "Preisvorstellung darf nicht null sein");
        this.provision = Objects.requireNonNull(provision, "Provision darf nicht null sein");
        this.status = VermittlungsvorgangStatus.NEU;
        this.besichtigungen = new ArrayList<>();
        this.angebote = new ArrayList<>();
        this.domainEvents = new ArrayList<>();
    }

    /**
     * Factory method to create a new brokerage process.
     */
    public static Vermittlungsvorgang erstellen(UUID immobilieId, Adresse adresse,
                                                 Preisvorstellung preisvorstellung, Provision provision) {
        return new Vermittlungsvorgang(UUID.randomUUID(), immobilieId, adresse, preisvorstellung, provision);
    }

    /**
     * Reconstitution constructor for loading from persistence.
     */
    public static Vermittlungsvorgang reconstitute(UUID id, UUID immobilieId, Adresse adresse,
                                                    Preisvorstellung preisvorstellung, Provision provision,
                                                    VermittlungsvorgangStatus status,
                                                    List<Besichtigung> besichtigungen, List<Angebot> angebote) {
        Vermittlungsvorgang vorgang = new Vermittlungsvorgang(id, immobilieId, adresse, preisvorstellung, provision);
        vorgang.status = status;
        vorgang.besichtigungen.addAll(besichtigungen);
        vorgang.angebote.addAll(angebote);
        return vorgang;
    }

    /**
     * Adds a new viewing to this brokerage process and sets the status to BESICHTIGUNG.
     */
    public Besichtigung besichtigungHinzufuegen(String interessentName, LocalDateTime zeitpunkt, String notizen) {
        Besichtigung besichtigung = new Besichtigung(UUID.randomUUID(), interessentName, zeitpunkt, notizen);
        this.besichtigungen.add(besichtigung);
        this.status = VermittlungsvorgangStatus.BESICHTIGUNG;
        return besichtigung;
    }

    /**
     * Marks an existing viewing as completed and raises a domain event.
     */
    public void besichtigungDurchfuehren(UUID besichtigungId) {
        Besichtigung besichtigung = besichtigungen.stream()
                .filter(b -> b.getId().equals(besichtigungId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Besichtigung mit ID " + besichtigungId + " nicht gefunden"));

        besichtigung.durchfuehren();

        domainEvents.add(new BesichtigungDurchgefuehrt(this.id, besichtigungId, LocalDateTime.now()));
    }

    /**
     * Receives a new offer, sets the status to ANGEBOT_PHASE, and raises a domain event.
     */
    public Angebot angebotEntgegennehmen(String interessentName, BigDecimal betrag) {
        Angebot angebot = new Angebot(UUID.randomUUID(), interessentName, betrag, LocalDateTime.now());
        this.angebote.add(angebot);
        this.status = VermittlungsvorgangStatus.ANGEBOT_PHASE;

        domainEvents.add(new AngebotEingegangen(this.id, betrag, angebot.getEingegangen()));
        return angebot;
    }

    /**
     * Accepts an existing offer and raises a domain event.
     */
    public void angebotAnnehmen(UUID angebotId) {
        Angebot angebot = angebote.stream()
                .filter(a -> a.getId().equals(angebotId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Angebot mit ID " + angebotId + " nicht gefunden"));

        angebot.annehmen();

        domainEvents.add(new AngebotAngenommen(this.id, angebotId, LocalDateTime.now()));
    }

    /**
     * Sets the status to NOTARTERMIN.
     * Invariant: at least one accepted offer must exist.
     *
     * @throws IllegalStateException if no accepted offer exists
     */
    public void statusAufNotarterminSetzen() {
        boolean hatAngenommenesAngebot = angebote.stream().anyMatch(Angebot::isAngenommen);

        if (!hatAngenommenesAngebot) {
            throw new IllegalStateException(
                    "Status kann nur auf NOTARTERMIN gesetzt werden, wenn ein angenommenes Angebot vorliegt");
        }

        this.status = VermittlungsvorgangStatus.NOTARTERMIN;
    }

    // --- Getters ---

    public UUID getId() {
        return id;
    }

    public UUID getImmobilieId() {
        return immobilieId;
    }

    public Adresse getAdresse() {
        return adresse;
    }

    public Preisvorstellung getPreisvorstellung() {
        return preisvorstellung;
    }

    public Provision getProvision() {
        return provision;
    }

    public VermittlungsvorgangStatus getStatus() {
        return status;
    }

    public List<Besichtigung> getBesichtigungen() {
        return Collections.unmodifiableList(besichtigungen);
    }

    public List<Angebot> getAngebote() {
        return Collections.unmodifiableList(angebote);
    }

    public List<Object> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    /**
     * Clears all collected domain events (typically called after events have been published).
     */
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
}
