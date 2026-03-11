package de.foerderung.antragstellung.application.service;

import de.foerderung.antragstellung.application.command.FlurstueckPruefenCommand;
import de.foerderung.antragstellung.application.port.FlurstueckPruefen;
import de.foerderung.antragstellung.domain.model.AntragsmappeNichtGefundenException;
import de.foerderung.antragstellung.domain.port.AntragsMappeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service (use case) for verifying a Flurstueck within an AntragsMappe.
 */
@Service
@Transactional
public class FlurstueckPruefenService implements FlurstueckPruefen {

    private final AntragsMappeRepository repository;

    public FlurstueckPruefenService(AntragsMappeRepository repository) {
        this.repository = repository;
    }

    @Override
    public void pruefen(FlurstueckPruefenCommand cmd) {
        var mappe = repository.findById(cmd.antragsmappeId())
                .orElseThrow(() -> new AntragsmappeNichtGefundenException(cmd.antragsmappeId()));

        mappe.flurstueckPruefen(cmd.flurstueckId());

        repository.save(mappe);
    }
}
