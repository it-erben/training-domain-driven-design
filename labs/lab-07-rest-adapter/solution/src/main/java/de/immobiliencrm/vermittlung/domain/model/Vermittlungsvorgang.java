package de.immobiliencrm.vermittlung.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import de.immobiliencrm.vermittlung.domain.event.AngebotAngenommen;
import de.immobiliencrm.vermittlung.domain.event.AngebotEingegangen;
import de.immobiliencrm.vermittlung.domain.event.BesichtigungDurchgefuehrt;

/**
 * Aggregate Root representing a property brokerage process.
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
        this.id = id;
        this.immobilieId = immobilieId;
        this.adresse = adresse;
        this.preisvorstellung = preisvorstellung;
        this.provision = provision;
        this.status = VermittlungsvorgangStatus.NEU;
        this.besichtigungen = new ArrayList<>();
        this.angebote = new ArrayList<>();
        this.domainEvents = new ArrayList<>();
    }

    /**
     * Factory method to create a new Vermittlungsvorgang.
     */
    public static Vermittlungsvorgang erstellen(UUID immobilieId, Adresse adresse,
                                                 Preisvorstellung preisvorstellung,
                                                 Provision provision) {
        Objects.requireNonNull(immobilieId, "ImmobilieId darf nicht null sein");
        Objects.requireNonNull(adresse, "Adresse darf nicht null sein");
        Objects.requireNonNull(preisvorstellung, "Preisvorstellung darf nicht null sein");
        Objects.requireNonNull(provision, "Provision darf nicht null sein");

        return new Vermittlungsvorgang(UUID.randomUUID(), immobilieId, adresse,
                preisvorstellung, provision);
    }

    /**
     * Adds a new viewing appointment and returns the generated Besichtigung ID.
     */
    public UUID besichtigungHinzufuegen(String interessentName, LocalDateTime zeitpunkt) {
        Objects.requireNonNull(interessentName, "InteressentName darf nicht null sein");
        Objects.requireNonNull(zeitpunkt, "Zeitpunkt darf nicht null sein");

        UUID besichtigungId = UUID.randomUUID();
        Besichtigung besichtigung = new Besichtigung(besichtigungId, interessentName, zeitpunkt);
        besichtigungen.add(besichtigung);
        status = VermittlungsvorgangStatus.BESICHTIGUNG;
        return besichtigungId;
    }

    /**
     * Marks a viewing as completed and raises a domain event.
     */
    public void besichtigungDurchfuehren(UUID besichtigungId) {
        Besichtigung besichtigung = besichtigungen.stream()
                .filter(b -> b.getId().equals(besichtigungId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Besichtigung mit ID " + besichtigungId + " nicht gefunden"));

        besichtigung.durchfuehren();
        domainEvents.add(new BesichtigungDurchgefuehrt(id, besichtigungId, LocalDateTime.now()));
    }

    /**
     * Receives a new offer and raises a domain event.
     */
    public void angebotEntgegennehmen(String interessentName, BigDecimal betrag) {
        UUID angebotId = UUID.randomUUID();
        LocalDateTime jetzt = LocalDateTime.now();
        Angebot angebot = new Angebot(angebotId, interessentName, betrag, jetzt);
        angebote.add(angebot);
        status = VermittlungsvorgangStatus.ANGEBOT_PHASE;
        domainEvents.add(new AngebotEingegangen(id, betrag, jetzt));
    }

    /**
     * Accepts an offer and raises a domain event.
     */
    public void angebotAnnehmen(UUID angebotId) {
        Angebot angebot = angebote.stream()
                .filter(a -> a.getId().equals(angebotId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Angebot mit ID " + angebotId + " nicht gefunden"));

        angebot.annehmen();
        domainEvents.add(new AngebotAngenommen(id, angebotId, LocalDateTime.now()));
    }

    /**
     * Sets the status to NOTARTERMIN. Requires at least one accepted offer.
     */
    public void statusAufNotarterminSetzen() {
        boolean hatAngenommenesAngebot = angebote.stream().anyMatch(Angebot::isAngenommen);
        if (!hatAngenommenesAngebot) {
            throw new IllegalStateException(
                    "Status kann nicht auf NOTARTERMIN gesetzt werden: kein angenommenes Angebot vorhanden");
        }
        status = VermittlungsvorgangStatus.NOTARTERMIN;
    }

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

    public void clearDomainEvents() {
        domainEvents.clear();
    }
}
