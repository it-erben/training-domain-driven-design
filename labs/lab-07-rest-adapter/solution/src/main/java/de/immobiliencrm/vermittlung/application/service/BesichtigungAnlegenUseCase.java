package de.immobiliencrm.vermittlung.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.immobiliencrm.vermittlung.application.command.BesichtigungAnlegenCommand;
import de.immobiliencrm.vermittlung.application.command.BesichtigungAnlegenResult;
import de.immobiliencrm.vermittlung.domain.model.Vermittlungsvorgang;
import de.immobiliencrm.vermittlung.domain.model.VermittlungsvorgangNichtGefundenException;
import de.immobiliencrm.vermittlung.domain.port.VermittlungsvorgangRepository;

/**
 * Application service implementing the "create viewing" use case.
 * Orchestrates the domain logic and persistence without containing business rules.
 */
@Service
public class BesichtigungAnlegenUseCase {

    private final VermittlungsvorgangRepository repository;

    public BesichtigungAnlegenUseCase(VermittlungsvorgangRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public BesichtigungAnlegenResult anlegen(BesichtigungAnlegenCommand command) {
        // Load the Vermittlungsvorgang from the repository
        Vermittlungsvorgang vorgang = repository.findById(command.vermittlungsvorgangId())
                .orElseThrow(() -> new VermittlungsvorgangNichtGefundenException(
                        command.vermittlungsvorgangId()));

        // Delegate to domain method
        UUID besichtigungId = vorgang.besichtigungHinzufuegen(
                command.interessentName(),
                command.zeitpunkt());

        // Persist the updated aggregate
        repository.save(vorgang);

        // Return the result
        return new BesichtigungAnlegenResult(besichtigungId, vorgang.getId());
    }
}
