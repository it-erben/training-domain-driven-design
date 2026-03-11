package de.foerderung.antragstellung.application.service;

import de.foerderung.antragstellung.application.command.FlurstueckHinzufuegenCommand;
import de.foerderung.antragstellung.application.command.FlurstueckHinzufuegenResult;
import de.foerderung.antragstellung.application.port.FlurstueckHinzufuegen;
import de.foerderung.antragstellung.domain.model.AntragsmappeNichtGefundenException;
import de.foerderung.antragstellung.domain.port.AntragsMappeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service (use case) for adding a Flurstueck to an AntragsMappe.
 */
@Service
@Transactional
public class FlurstueckHinzufuegenService implements FlurstueckHinzufuegen {

    private final AntragsMappeRepository repository;

    public FlurstueckHinzufuegenService(AntragsMappeRepository repository) {
        this.repository = repository;
    }

    @Override
    public FlurstueckHinzufuegenResult hinzufuegen(FlurstueckHinzufuegenCommand cmd) {
        var mappe = repository.findById(cmd.antragsmappeId())
                .orElseThrow(() -> new AntragsmappeNichtGefundenException(cmd.antragsmappeId()));

        var flurstueckId = mappe.flurstueckHinzufuegen(
                cmd.flurstueckNummer(), cmd.flaeche(), null);

        repository.save(mappe);

        return new FlurstueckHinzufuegenResult(
                flurstueckId, mappe.getId(),
                cmd.flurstueckNummer(), cmd.flaeche());
    }
}
