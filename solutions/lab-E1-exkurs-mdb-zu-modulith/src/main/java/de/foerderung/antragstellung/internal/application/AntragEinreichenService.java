package de.foerderung.antragstellung.internal.application;

import de.foerderung.antragstellung.AenderungsArt;
import de.foerderung.antragstellung.AntragsmappeEingereicht;
import de.foerderung.antragstellung.AntragsmappeGeaendert;
import de.foerderung.antragstellung.internal.domain.model.AntragsMappe;
import de.foerderung.antragstellung.internal.domain.model.AntragsmappeNichtGefundenException;
import de.foerderung.antragstellung.internal.domain.model.FlurstueckNummer;
import de.foerderung.antragstellung.internal.domain.model.Foerderbetrag;
import de.foerderung.antragstellung.internal.domain.model.Foerderquote;
import de.foerderung.antragstellung.internal.domain.model.RegistrierungsNummer;
import de.foerderung.antragstellung.internal.domain.port.AntragsMappeRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@Transactional
public class AntragEinreichenService {

    private final AntragsMappeRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    public AntragEinreichenService(AntragsMappeRepository repository,
                                    ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    public AntragsMappe create(String registrierungsNummer) {
        var mappe = AntragsMappe.erstellen(
                new RegistrierungsNummer(registrierungsNummer),
                new Foerderbetrag(new BigDecimal("10000"), "EUR"),
                new Foerderquote(new BigDecimal("0.35")));
        mappe.flurstueckHinzufuegen(new FlurstueckNummer("001/0001"), new BigDecimal("10.5"), null);
        var saved = repository.save(mappe);

        // Publish AntragsmappeGeaendert — the new Published Language event
        // This replaces what was previously an implicit JMS topic/AntragGeaendert
        eventPublisher.publishEvent(new AntragsmappeGeaendert(
                saved.getRegistrierungsNummer().wert(),
                AenderungsArt.AKTUALISIERT,
                Instant.now()));

        return saved;
    }

    public void execute(AntragEinreichenCommand cmd) {
        var mappe = repository.findById(cmd.antragsmappeId())
                .orElseThrow(() -> new AntragsmappeNichtGefundenException(cmd.antragsmappeId()));
        mappe.einreichen();
        repository.save(mappe);

        // Publish integration event for Pruefung BC
        eventPublisher.publishEvent(new AntragsmappeEingereicht(
                mappe.getId().value(), mappe.getRegistrierungsNummer().wert()));

        // Publish integration event for Auswertung BC (Einreichen = fachliche Aenderung)
        eventPublisher.publishEvent(new AntragsmappeGeaendert(
                mappe.getRegistrierungsNummer().wert(),
                AenderungsArt.AKTUALISIERT,
                Instant.now()));

        mappe.clearDomainEvents();
    }
}
