package de.realestate.brokerage.application.service;

import de.realestate.brokerage.application.command.CreateViewingCommand;
import de.realestate.brokerage.application.command.CreateViewingResult;
import de.realestate.brokerage.domain.model.Viewing;
import de.realestate.brokerage.domain.model.BrokerageProcess;
import de.realestate.brokerage.domain.model.ProcessNotFoundException;
import de.realestate.brokerage.domain.port.BrokerageProcessRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service that orchestrates the "schedule a viewing" use case.
 */
@Service
public class CreateViewingUseCase {

    private final BrokerageProcessRepository repository;

    public CreateViewingUseCase(BrokerageProcessRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public CreateViewingResult createViewing(CreateViewingCommand command) {
        // 1. Load the BrokerageProcess from the repository
        BrokerageProcess process = repository.findById(command.processId())
                .orElseThrow(() -> new ProcessNotFoundException(command.processId()));

        // 2. Call the domain method on the aggregate root
        Viewing viewing = process.addViewing(
                command.prospectName(),
                command.appointmentDate(),
                null
        );

        // 3. Save the updated BrokerageProcess
        repository.save(process);

        // 4. Return the result
        return new CreateViewingResult(viewing.getId(), process.getId());
    }
}
