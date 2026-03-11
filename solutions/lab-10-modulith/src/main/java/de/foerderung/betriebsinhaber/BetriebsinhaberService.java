package de.foerderung.betriebsinhaber;

import org.springframework.stereotype.Service;

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

    public Betriebsinhaber save(Betriebsinhaber betriebsinhaber) {
        return repository.save(betriebsinhaber);
    }

    public Optional<Betriebsinhaber> update(Long id, Betriebsinhaber betriebsinhaber) {
        return repository.findById(id)
                .map(existing -> {
                    existing.setName(betriebsinhaber.getName());
                    existing.setAnschrift(betriebsinhaber.getAnschrift());
                    existing.setBetriebsnummer(betriebsinhaber.getBetriebsnummer());
                    return repository.save(existing);
                });
    }

    public boolean delete(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }
}
