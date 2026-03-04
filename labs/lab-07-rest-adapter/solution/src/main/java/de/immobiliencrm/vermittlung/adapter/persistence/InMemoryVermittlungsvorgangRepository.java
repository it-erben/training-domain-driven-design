package de.immobiliencrm.vermittlung.adapter.persistence;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import de.immobiliencrm.vermittlung.domain.model.Vermittlungsvorgang;
import de.immobiliencrm.vermittlung.domain.port.VermittlungsvorgangRepository;

/**
 * In-memory implementation of VermittlungsvorgangRepository for demonstration purposes.
 */
@Repository
public class InMemoryVermittlungsvorgangRepository implements VermittlungsvorgangRepository {

    private final Map<UUID, Vermittlungsvorgang> store = new ConcurrentHashMap<>();

    @Override
    public Optional<Vermittlungsvorgang> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Vermittlungsvorgang save(Vermittlungsvorgang vermittlungsvorgang) {
        store.put(vermittlungsvorgang.getId(), vermittlungsvorgang);
        return vermittlungsvorgang;
    }

    @Override
    public void deleteById(UUID id) {
        store.remove(id);
    }
}
