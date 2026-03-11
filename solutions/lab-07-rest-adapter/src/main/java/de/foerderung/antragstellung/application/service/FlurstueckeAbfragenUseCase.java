package de.foerderung.antragstellung.application.service;

import de.foerderung.antragstellung.domain.model.AntragsMappe;
import de.foerderung.antragstellung.domain.model.AntragsmappeNichtGefundenException;
import de.foerderung.antragstellung.domain.model.Flurstueck;
import de.foerderung.antragstellung.domain.port.AntragsMappeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Application service implementing the "list Flurstuecke" query use case.
 */
@Service
public class FlurstueckeAbfragenUseCase {

    private final AntragsMappeRepository repository;

    public FlurstueckeAbfragenUseCase(AntragsMappeRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Flurstueck> abfragen(UUID antragsmappeId) {
        AntragsMappe mappe = repository.findById(antragsmappeId)
                .orElseThrow(() -> new AntragsmappeNichtGefundenException(antragsmappeId));
        return mappe.getFlurstuecke();
    }
}
