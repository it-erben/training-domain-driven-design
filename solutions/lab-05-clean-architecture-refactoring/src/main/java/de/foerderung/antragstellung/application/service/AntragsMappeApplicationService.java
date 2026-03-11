package de.foerderung.antragstellung.application.service;

import de.foerderung.antragstellung.domain.model.AntragId;
import de.foerderung.antragstellung.domain.model.AntragsMappe;
import de.foerderung.antragstellung.domain.model.Foerderbetrag;
import de.foerderung.antragstellung.domain.model.Foerderquote;
import de.foerderung.antragstellung.domain.model.RegistrierungsNummer;
import de.foerderung.antragstellung.domain.port.AntragsMappeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Application Service that orchestrates use cases for the AntragsMappe aggregate.
 */
@Service
@Transactional
public class AntragsMappeApplicationService {

    private final AntragsMappeRepository repository;

    public AntragsMappeApplicationService(AntragsMappeRepository repository) {
        this.repository = repository;
    }

    /**
     * Creates a new AntragsMappe and persists it.
     */
    public AntragsMappe erstellen(RegistrierungsNummer registrierungsNummer,
                                   Foerderbetrag foerderbetrag,
                                   Foerderquote foerderquote) {
        AntragsMappe mappe = AntragsMappe.erstellen(registrierungsNummer,
                foerderbetrag, foerderquote);
        return repository.save(mappe);
    }

    /**
     * Finds an AntragsMappe by its ID.
     */
    @Transactional(readOnly = true)
    public Optional<AntragsMappe> findById(AntragId id) {
        return repository.findById(id);
    }
}
