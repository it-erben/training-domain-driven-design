package de.foerderung.antragstellung.internal.domain.port;

import de.foerderung.antragstellung.internal.domain.model.AntragId;
import de.foerderung.antragstellung.internal.domain.model.AntragsMappe;

import java.util.Optional;

public interface AntragsMappeRepository {

    Optional<AntragsMappe> findById(AntragId id);

    AntragsMappe save(AntragsMappe antragsMappe);

    void deleteById(AntragId id);
}
