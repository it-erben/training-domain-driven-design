package de.immobiliencrm.akquise.domain.port;

import de.immobiliencrm.akquise.domain.model.Maklerauftrag;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for the Maklerauftrag aggregate.
 */
public interface MaklerauftragRepository {

    Optional<Maklerauftrag> findById(UUID id);

    Maklerauftrag save(Maklerauftrag maklerauftrag);
}
