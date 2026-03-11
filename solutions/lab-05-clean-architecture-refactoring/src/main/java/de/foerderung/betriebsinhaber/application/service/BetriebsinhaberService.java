package de.foerderung.betriebsinhaber.application.service;

import de.foerderung.betriebsinhaber.domain.model.Betriebsinhaber;
import de.foerderung.betriebsinhaber.domain.port.BetriebsinhaberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BetriebsinhaberService {

    private final BetriebsinhaberRepository repository;

    public BetriebsinhaberService(BetriebsinhaberRepository repository) {
        this.repository = repository;
    }

    public List<Betriebsinhaber> findAll() {
        return repository.findAll();
    }

    public Optional<Betriebsinhaber> findById(Long id) {
        return repository.findById(id);
    }

    @Transactional
    public Betriebsinhaber save(Betriebsinhaber betriebsinhaber) {
        return repository.save(betriebsinhaber);
    }

    @Transactional
    public Optional<Betriebsinhaber> update(Long id, Betriebsinhaber updated) {
        return repository.findById(id)
                .map(existing -> {
                    existing.update(
                            updated.getName(),
                            updated.getAdresse(),
                            updated.getBetriebsflaeche(),
                            updated.getFoerdersumme()
                    );
                    return repository.save(existing);
                });
    }

    @Transactional
    public boolean delete(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }
}
