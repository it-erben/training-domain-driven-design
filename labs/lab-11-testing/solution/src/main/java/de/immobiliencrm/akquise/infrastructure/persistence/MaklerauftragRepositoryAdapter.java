package de.immobiliencrm.akquise.infrastructure.persistence;

import de.immobiliencrm.akquise.domain.model.Maklerauftrag;
import de.immobiliencrm.akquise.domain.port.MaklerauftragRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter that bridges the domain repository port with the JPA repository implementation.
 */
@Component
public class MaklerauftragRepositoryAdapter implements MaklerauftragRepository {

    private final JpaMaklerauftragRepository jpaRepository;

    public MaklerauftragRepositoryAdapter(JpaMaklerauftragRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Maklerauftrag> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(JpaMaklerauftrag::toModel);
    }

    @Override
    public Maklerauftrag save(Maklerauftrag maklerauftrag) {
        JpaMaklerauftrag jpaEntity = JpaMaklerauftrag.fromModel(maklerauftrag);
        JpaMaklerauftrag saved = jpaRepository.save(jpaEntity);
        return saved.toModel();
    }
}
