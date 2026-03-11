package de.foerderung.antragstellung.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.foerderung.antragstellung.application.command.FlurstueckHinzufuegenCommand;
import de.foerderung.antragstellung.application.command.FlurstueckHinzufuegenResult;
import de.foerderung.antragstellung.domain.model.AntragsMappe;
import de.foerderung.antragstellung.domain.model.AntragsmappeNichtGefundenException;
import de.foerderung.antragstellung.domain.model.Flurstueck;
import de.foerderung.antragstellung.domain.model.FlurstueckNummer;
import de.foerderung.antragstellung.domain.port.AntragsMappeRepository;

/**
 * Application service implementing the "add Flurstueck" use case.
 * Orchestrates the domain logic and persistence without containing business rules.
 */
@Service
public class FlurstueckHinzufuegenService {

    private final AntragsMappeRepository repository;

    public FlurstueckHinzufuegenService(AntragsMappeRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public FlurstueckHinzufuegenResult hinzufuegen(FlurstueckHinzufuegenCommand command) {
        AntragsMappe mappe = repository.findById(command.antragsmappeId())
                .orElseThrow(() -> new AntragsmappeNichtGefundenException(
                        command.antragsmappeId()));

        Flurstueck flurstueck = mappe.flurstueckHinzufuegen(
                new FlurstueckNummer(command.flurstueckNummer()),
                command.flaeche(),
                command.bemerkung());

        repository.save(mappe);

        return new FlurstueckHinzufuegenResult(flurstueck.getId(), mappe.getId());
    }
}
