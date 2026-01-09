package de.realestate.brokerage.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.realestate.brokerage.application.command.CompleteViewingCommand;
import de.realestate.brokerage.domain.model.BrokerageProcess;
import de.realestate.brokerage.domain.model.ProcessNotFoundException;
import de.realestate.brokerage.domain.port.BrokerageProcessRepository;

@Service
public class CompleteViewingUseCase {

    private final BrokerageProcessRepository repository;

    public CompleteViewingUseCase(BrokerageProcessRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void complete(CompleteViewingCommand command) {
        BrokerageProcess process = repository.findById(command.processId())
                .orElseThrow(() -> new ProcessNotFoundException(command.processId()));

        process.completeViewing(command.viewingId());

        repository.save(process);
    }
}
