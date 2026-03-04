package de.immobiliencrm.vermittlung.application.service;

import de.immobiliencrm.vermittlung.application.command.ErstelleVermittlungsvorgangCommand;
import de.immobiliencrm.vermittlung.domain.model.Adresse;
import de.immobiliencrm.vermittlung.domain.model.Preisvorstellung;
import de.immobiliencrm.vermittlung.domain.model.Provision;
import de.immobiliencrm.vermittlung.domain.model.Vermittlungsvorgang;
import de.immobiliencrm.vermittlung.domain.port.VermittlungsvorgangRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Application service orchestrating use cases for the brokerage process.
 */
@Service
@Transactional
public class VermittlungsvorgangService {

    private final VermittlungsvorgangRepository repository;

    public VermittlungsvorgangService(VermittlungsvorgangRepository repository) {
        this.repository = repository;
    }

    /**
     * Creates a new brokerage process from the given command.
     */
    public Vermittlungsvorgang erstellen(ErstelleVermittlungsvorgangCommand command) {
        Adresse adresse = new Adresse(command.strasse(), command.plz(), command.ort());
        Preisvorstellung preis = new Preisvorstellung(command.preisBetrag(), command.preisWaehrung());
        Provision provision = new Provision(command.provisionProzentsatz());

        Vermittlungsvorgang vorgang = Vermittlungsvorgang.erstellen(
                command.immobilieId(), adresse, preis, provision);

        return repository.save(vorgang);
    }

    /**
     * Finds a brokerage process by its ID.
     */
    @Transactional(readOnly = true)
    public Optional<Vermittlungsvorgang> findById(UUID id) {
        return repository.findById(id);
    }

    /**
     * Returns all brokerage processes.
     */
    @Transactional(readOnly = true)
    public List<Vermittlungsvorgang> findAll() {
        return repository.findAll();
    }

    /**
     * Deletes a brokerage process by its ID.
     */
    public void loeschen(UUID id) {
        repository.deleteById(id);
    }
}
