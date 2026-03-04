package de.immobiliencrm.vermittlung.application.service;

import de.immobiliencrm.vermittlung.application.command.BesichtigungAnlegenCommand;
import de.immobiliencrm.vermittlung.application.command.BesichtigungAnlegenResult;
import de.immobiliencrm.vermittlung.domain.model.Besichtigung;
import de.immobiliencrm.vermittlung.domain.model.Vermittlungsvorgang;
import de.immobiliencrm.vermittlung.domain.model.VermittlungsvorgangNichtGefundenException;
import de.immobiliencrm.vermittlung.domain.port.VermittlungsvorgangRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service that orchestrates the "schedule a viewing" use case.
 */
@Service
public class BesichtigungAnlegenUseCase {

    private final VermittlungsvorgangRepository repository;

    public BesichtigungAnlegenUseCase(VermittlungsvorgangRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public BesichtigungAnlegenResult anlegen(BesichtigungAnlegenCommand command) {
        // 1. Load the Vermittlungsvorgang from the repository
        Vermittlungsvorgang vorgang = repository.findById(command.vermittlungsvorgangId())
                .orElseThrow(() -> new VermittlungsvorgangNichtGefundenException(command.vermittlungsvorgangId()));

        // 2. Call the domain method on the aggregate root
        Besichtigung besichtigung = vorgang.besichtigungHinzufuegen(
                command.interessentName(),
                command.zeitpunkt(),
                null
        );

        // 3. Save the updated Vermittlungsvorgang
        repository.save(vorgang);

        // 4. Return the result
        return new BesichtigungAnlegenResult(besichtigung.getId(), vorgang.getId());
    }
}
