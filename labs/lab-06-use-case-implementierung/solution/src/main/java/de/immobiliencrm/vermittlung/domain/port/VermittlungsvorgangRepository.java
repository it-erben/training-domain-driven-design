package de.immobiliencrm.vermittlung.domain.port;

import java.util.Optional;
import java.util.UUID;

import de.immobiliencrm.vermittlung.domain.model.Vermittlungsvorgang;

/**
 * Repository port for Vermittlungsvorgang persistence.
 * This is a pure Java interface without any framework dependencies.
 */
public interface VermittlungsvorgangRepository {

    Optional<Vermittlungsvorgang> findById(UUID id);

    Vermittlungsvorgang save(Vermittlungsvorgang vermittlungsvorgang);

    void deleteById(UUID id);
}
