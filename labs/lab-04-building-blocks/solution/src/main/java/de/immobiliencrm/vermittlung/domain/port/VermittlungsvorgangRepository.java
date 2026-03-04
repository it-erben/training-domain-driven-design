package de.immobiliencrm.vermittlung.domain.port;

import de.immobiliencrm.vermittlung.domain.model.Vermittlungsvorgang;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for the Vermittlungsvorgang aggregate.
 * This is a pure domain interface with no framework dependencies.
 */
public interface VermittlungsvorgangRepository {

    Optional<Vermittlungsvorgang> findById(UUID id);

    Vermittlungsvorgang save(Vermittlungsvorgang vermittlungsvorgang);

    void deleteById(UUID id);
}
