package de.realestate.brokerage.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.realestate.brokerage.application.command.CreateViewingCommand;
import de.realestate.brokerage.application.command.CreateViewingResult;
import de.realestate.brokerage.domain.model.BrokerageProcess;
import de.realestate.brokerage.domain.model.ProcessNotFoundException;
import de.realestate.brokerage.domain.port.BrokerageProcessRepository;

/**
 * Application service implementing the "create viewing" use case.
 * Orchestrates the domain logic and persistence without containing business rules.
 */
@Service
public class CreateViewingUseCase {

    private final BrokerageProcessRepository repository;

    public CreateViewingUseCase(BrokerageProcessRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public CreateViewingResult create(CreateViewingCommand command) {
        // Load the BrokerageProcess from the repository
        BrokerageProcess process = repository.findById(command.processId())
                .orElseThrow(() -> new ProcessNotFoundException(
                        command.processId()));

        // Delegate to domain method
        UUID viewingId = process.addViewing(
                command.prospectName(),
                command.appointmentDate());

        // Persist the updated aggregate
        repository.save(process);

        // Return the result
        return new CreateViewingResult(viewingId, process.getId());
    }
}
