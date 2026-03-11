package de.foerderung.antragstellung.internal.domain.port;

import de.foerderung.antragstellung.internal.domain.model.AntragId;
import de.foerderung.antragstellung.internal.domain.model.AntragsMappe;

import java.util.Optional;

/**
 * Repository port for AntragsMappe aggregates.
 * Pure Java interface - no Spring or JPA dependencies.
 */
public interface AntragsMappeRepository {

    Optional<AntragsMappe> findById(AntragId id);

    AntragsMappe save(AntragsMappe antragsMappe);

    void deleteById(AntragId id);
}
