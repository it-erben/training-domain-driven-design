package de.foerderung.pruefung.internal.infrastructure.persistence;

import de.foerderung.pruefung.internal.domain.AntragsReferenz;
import de.foerderung.pruefung.internal.domain.Pruefvorgang;
import de.foerderung.pruefung.internal.domain.port.PruefvorgangRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PruefvorgangRepositoryAdapter implements PruefvorgangRepository {

    private final JpaPruefvorgangRepository jpaRepository;

    public PruefvorgangRepositoryAdapter(JpaPruefvorgangRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Pruefvorgang save(Pruefvorgang pruefvorgang) {
        JpaPruefvorgang jpaEntity = JpaPruefvorgang.fromModel(pruefvorgang);
        JpaPruefvorgang saved = jpaRepository.save(jpaEntity);
        return saved.toModel();
    }

    @Override
    public Optional<Pruefvorgang> findByAntragsReferenz(AntragsReferenz antragsReferenz) {
        return jpaRepository.findByAntragsmappeId(antragsReferenz.value())
                .map(JpaPruefvorgang::toModel);
    }

    @Override
    public boolean existsByAntragsReferenz(AntragsReferenz antragsReferenz) {
        return jpaRepository.existsByAntragsmappeId(antragsReferenz.value());
    }
}
