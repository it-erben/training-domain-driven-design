package de.foerderung.pruefung.internal.domain.port;

import de.foerderung.pruefung.internal.domain.AntragsReferenz;
import de.foerderung.pruefung.internal.domain.Pruefvorgang;

import java.util.Optional;

public interface PruefvorgangRepository {

    Pruefvorgang save(Pruefvorgang pruefvorgang);

    Optional<Pruefvorgang> findByAntragsReferenz(AntragsReferenz antragsReferenz);

    boolean existsByAntragsReferenz(AntragsReferenz antragsReferenz);
}
