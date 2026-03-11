package de.foerderung.antragstellung.infrastructure.persistence;

import de.foerderung.antragstellung.domain.model.AntragId;
import de.foerderung.antragstellung.domain.model.AntragsMappe;
import de.foerderung.antragstellung.domain.port.AntragsMappeRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adapter that implements the domain repository port using Spring Data JPA.
 * Maps between domain objects and JPA entities.
 */
@Component
public class AntragsMappeRepositoryAdapter implements AntragsMappeRepository {

    private final JpaAntragsMappeRepository jpaRepository;

    public AntragsMappeRepositoryAdapter(JpaAntragsMappeRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<AntragsMappe> findById(AntragId id) {
        return jpaRepository.findById(id.value())
                .map(JpaAntragsMappe::toModel);
    }

    @Override
    public AntragsMappe save(AntragsMappe antragsMappe) {
        JpaAntragsMappe jpaEntity = JpaAntragsMappe.fromModel(antragsMappe);
        JpaAntragsMappe saved = jpaRepository.save(jpaEntity);
        return saved.toModel();
    }

    @Override
    public void deleteById(AntragId id) {
        jpaRepository.deleteById(id.value());
    }
}
