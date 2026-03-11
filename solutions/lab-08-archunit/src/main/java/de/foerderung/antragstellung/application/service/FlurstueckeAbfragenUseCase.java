package de.foerderung.antragstellung.application.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.foerderung.antragstellung.domain.model.AntragsMappe;
import de.foerderung.antragstellung.domain.model.AntragsmappeNichtGefundenException;
import de.foerderung.antragstellung.domain.model.Flurstueck;
import de.foerderung.antragstellung.domain.port.AntragsMappeRepository;

/**
 * Application service for querying Flurstuecke of an AntragsMappe.
 * Returns DTOs to avoid leaking domain model types to the adapter layer.
 */
@Service
public class FlurstueckeAbfragenUseCase {

    private final AntragsMappeRepository repository;

    public FlurstueckeAbfragenUseCase(AntragsMappeRepository repository) {
        this.repository = repository;
    }

    /**
     * DTO returned by this use case - keeps domain model types out of the adapter layer.
     */
    public record FlurstueckInfo(
            UUID id,
            String nummer,
            BigDecimal flaeche,
            String bemerkung,
            boolean geprueft
    ) {}

    @Transactional(readOnly = true)
    public List<FlurstueckInfo> abfragen(UUID antragsmappeId) {
        AntragsMappe mappe = repository.findById(antragsmappeId)
                .orElseThrow(() -> new AntragsmappeNichtGefundenException(antragsmappeId));

        return mappe.getFlurstuecke().stream()
                .map(this::toInfo)
                .toList();
    }

    private FlurstueckInfo toInfo(Flurstueck flurstueck) {
        return new FlurstueckInfo(
                flurstueck.getId(),
                flurstueck.getNummer().wert(),
                flurstueck.getFlaeche(),
                flurstueck.getBemerkung(),
                flurstueck.isGeprueft());
    }
}
