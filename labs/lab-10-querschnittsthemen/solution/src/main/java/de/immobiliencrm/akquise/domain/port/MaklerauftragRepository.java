package de.immobiliencrm.akquise.domain.port;

import de.immobiliencrm.akquise.domain.model.Maklerauftrag;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for the Maklerauftrag aggregate.
 * This is a pure domain interface with no framework dependencies.
 */
public interface MaklerauftragRepository {

    Optional<Maklerauftrag> findById(UUID id);

    Maklerauftrag save(Maklerauftrag maklerauftrag);
}
