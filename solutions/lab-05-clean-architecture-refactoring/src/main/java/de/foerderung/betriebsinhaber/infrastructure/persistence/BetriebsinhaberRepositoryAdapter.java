package de.foerderung.betriebsinhaber.infrastructure.persistence;

import de.foerderung.betriebsinhaber.domain.model.Betriebsinhaber;
import de.foerderung.betriebsinhaber.domain.port.BetriebsinhaberRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class BetriebsinhaberRepositoryAdapter implements BetriebsinhaberRepository {

    private final JpaBetriebsinhaberRepository jpaRepository;

    public BetriebsinhaberRepositoryAdapter(JpaBetriebsinhaberRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<Betriebsinhaber> findAll() {
        return jpaRepository.findAll().stream()
                .map(JpaBetriebsinhaber::toModel)
                .toList();
    }

    @Override
    public Optional<Betriebsinhaber> findById(Long id) {
        return jpaRepository.findById(id)
                .map(JpaBetriebsinhaber::toModel);
    }

    @Override
    public Betriebsinhaber save(Betriebsinhaber betriebsinhaber) {
        JpaBetriebsinhaber jpaEntity = JpaBetriebsinhaber.fromModel(betriebsinhaber);
        JpaBetriebsinhaber saved = jpaRepository.save(jpaEntity);
        return saved.toModel();
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
