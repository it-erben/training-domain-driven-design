package de.realestate.brokerage.infrastructure.persistence;

import de.realestate.brokerage.domain.model.BrokerageProcess;
import de.realestate.brokerage.domain.port.BrokerageProcessRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class BrokerageProcessRepositoryAdapter implements BrokerageProcessRepository {

    private final JpaBrokerageProcessRepository jpaRepository;

    public BrokerageProcessRepositoryAdapter(JpaBrokerageProcessRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<BrokerageProcess> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(JpaBrokerageProcess::toModel);
    }

    @Override
    public List<BrokerageProcess> findAll() {
        return jpaRepository.findAll().stream()
                .map(JpaBrokerageProcess::toModel)
                .toList();
    }

    @Override
    public BrokerageProcess save(BrokerageProcess brokerageProcess) {
        JpaBrokerageProcess jpaEntity = JpaBrokerageProcess.fromModel(brokerageProcess);
        JpaBrokerageProcess saved = jpaRepository.save(jpaEntity);
        return saved.toModel();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
