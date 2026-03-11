package de.foerderung.antragstellung.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.foerderung.antragstellung.application.command.FlurstueckPruefenCommand;
import de.foerderung.antragstellung.domain.model.AntragsMappe;
import de.foerderung.antragstellung.domain.model.AntragsmappeNichtGefundenException;
import de.foerderung.antragstellung.domain.port.AntragsMappeRepository;

@Service
public class FlurstueckPruefenService {

    private final AntragsMappeRepository repository;

    public FlurstueckPruefenService(AntragsMappeRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void pruefen(FlurstueckPruefenCommand command) {
        AntragsMappe mappe = repository.findById(command.antragsmappeId())
                .orElseThrow(() -> new AntragsmappeNichtGefundenException(
                        command.antragsmappeId()));

        mappe.flurstueckPruefen(command.flurstueckId());

        repository.save(mappe);
    }
}
