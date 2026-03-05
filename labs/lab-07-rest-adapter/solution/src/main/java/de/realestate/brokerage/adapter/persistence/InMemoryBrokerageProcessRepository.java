package de.realestate.brokerage.adapter.persistence;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import de.realestate.brokerage.domain.model.BrokerageProcess;
import de.realestate.brokerage.domain.port.BrokerageProcessRepository;

/**
 * In-memory implementation of BrokerageProcessRepository for demonstration purposes.
 */
@Repository
public class InMemoryBrokerageProcessRepository implements BrokerageProcessRepository {

    private final Map<UUID, BrokerageProcess> store = new ConcurrentHashMap<>();

    @Override
    public Optional<BrokerageProcess> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public BrokerageProcess save(BrokerageProcess brokerageProcess) {
        store.put(brokerageProcess.getId(), brokerageProcess);
        return brokerageProcess;
    }

    @Override
    public void deleteById(UUID id) {
        store.remove(id);
    }
}
