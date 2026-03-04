package de.immobiliencrm.vermittlung.infrastructure.persistence;

import de.immobiliencrm.vermittlung.domain.model.Vermittlungsvorgang;
import de.immobiliencrm.vermittlung.domain.port.VermittlungsvorgangRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter that implements the domain repository port using Spring Data JPA.
 * Maps between domain objects and JPA entities.
 */
@Component
public class VermittlungsvorgangRepositoryAdapter implements VermittlungsvorgangRepository {

    private final JpaVermittlungsvorgangRepository jpaRepository;

    public VermittlungsvorgangRepositoryAdapter(JpaVermittlungsvorgangRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Vermittlungsvorgang> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(JpaVermittlungsvorgang::toModel);
    }

    @Override
    public Vermittlungsvorgang save(Vermittlungsvorgang vermittlungsvorgang) {
        JpaVermittlungsvorgang jpaEntity = JpaVermittlungsvorgang.fromModel(vermittlungsvorgang);
        JpaVermittlungsvorgang saved = jpaRepository.save(jpaEntity);
        return saved.toModel();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
