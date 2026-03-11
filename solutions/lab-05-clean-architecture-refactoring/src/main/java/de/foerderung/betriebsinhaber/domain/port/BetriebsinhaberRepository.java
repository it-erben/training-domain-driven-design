package de.foerderung.betriebsinhaber.domain.port;

import de.foerderung.betriebsinhaber.domain.model.Betriebsinhaber;

import java.util.List;
import java.util.Optional;

public interface BetriebsinhaberRepository {

    List<Betriebsinhaber> findAll();

    Optional<Betriebsinhaber> findById(Long id);

    Betriebsinhaber save(Betriebsinhaber betriebsinhaber);

    boolean existsById(Long id);

    void deleteById(Long id);
}
