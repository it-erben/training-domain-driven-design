package de.foerderung.antragstellung.internal.domain.port;

import de.foerderung.antragstellung.internal.domain.model.AntragsMappe;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository port for AntragsMappe aggregates.
 * Pure Java interface - no Spring or JPA dependencies.
 */
public interface AntragsMappeRepository {

    Optional<AntragsMappe> findById(UUID id);

    AntragsMappe save(AntragsMappe antragsMappe);

    void deleteById(UUID id);
}
