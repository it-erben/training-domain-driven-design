package de.foerderung.pruefung.internal.domain.port;

import de.foerderung.pruefung.internal.domain.AntragsReferenz;
import de.foerderung.pruefung.internal.domain.Pruefvorgang;

import java.util.Optional;

/**
 * Repository port for Pruefvorgang aggregates.
 * Pure Java interface - no Spring or JPA dependencies.
 */
public interface PruefvorgangRepository {

    Pruefvorgang save(Pruefvorgang pruefvorgang);

    Optional<Pruefvorgang> findByAntragsReferenz(AntragsReferenz antragsReferenz);

    boolean existsByAntragsReferenz(AntragsReferenz antragsReferenz);
}
