package de.foerderung.antragstellung.application.service;

import de.foerderung.antragstellung.application.command.FlurstueckHinzufuegenCommand;
import de.foerderung.antragstellung.application.command.FlurstueckHinzufuegenResult;
import de.foerderung.antragstellung.domain.model.AntragsMappe;
import de.foerderung.antragstellung.domain.model.AntragsmappeNichtGefundenException;
import de.foerderung.antragstellung.domain.model.FlurstueckId;
import de.foerderung.antragstellung.domain.port.AntragsMappeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        FlurstueckId flurstueckId = mappe.flurstueckHinzufuegen(
                command.flurstueckNummer(),
                command.flaeche(),
                null);

        repository.save(mappe);

        return new FlurstueckHinzufuegenResult(flurstueckId, mappe.getId(),
                command.flurstueckNummer(), command.flaeche());
    }
}
