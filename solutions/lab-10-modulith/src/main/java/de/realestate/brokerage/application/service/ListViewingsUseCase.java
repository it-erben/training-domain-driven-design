package de.realestate.brokerage.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.realestate.brokerage.domain.model.BrokerageProcess;
import de.realestate.brokerage.domain.model.ProcessNotFoundException;
import de.realestate.brokerage.domain.model.Viewing;
import de.realestate.brokerage.domain.port.BrokerageProcessRepository;

@Service
public class ListViewingsUseCase {

    private final BrokerageProcessRepository repository;

    public ListViewingsUseCase(BrokerageProcessRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Viewing> list(UUID processId) {
        BrokerageProcess process = repository.findById(processId)
                .orElseThrow(() -> new ProcessNotFoundException(processId));
        return process.getViewings();
    }
}
