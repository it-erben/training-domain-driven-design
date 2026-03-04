package de.immobiliencrm.vermittlung.application.service;

import de.immobiliencrm.vermittlung.domain.model.Adresse;
import de.immobiliencrm.vermittlung.domain.model.Preisvorstellung;
import de.immobiliencrm.vermittlung.domain.model.Provision;
import de.immobiliencrm.vermittlung.domain.model.Vermittlungsvorgang;
import de.immobiliencrm.vermittlung.domain.port.VermittlungsvorgangRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Application Service that orchestrates use cases for the Vermittlungsvorgang aggregate.
 */
@Service
@Transactional
public class VermittlungsvorgangApplicationService {

    private final VermittlungsvorgangRepository repository;

    public VermittlungsvorgangApplicationService(VermittlungsvorgangRepository repository) {
        this.repository = repository;
    }

    /**
     * Creates a new Vermittlungsvorgang and persists it.
     */
    public Vermittlungsvorgang erstellen(UUID immobilieId, Adresse adresse,
                                          Preisvorstellung preis, Provision provision) {
        Vermittlungsvorgang vorgang = Vermittlungsvorgang.erstellen(immobilieId, adresse, preis, provision);
        return repository.save(vorgang);
    }

    /**
     * Finds a Vermittlungsvorgang by its ID.
     */
    @Transactional(readOnly = true)
    public Optional<Vermittlungsvorgang> findById(UUID id) {
        return repository.findById(id);
    }
}
