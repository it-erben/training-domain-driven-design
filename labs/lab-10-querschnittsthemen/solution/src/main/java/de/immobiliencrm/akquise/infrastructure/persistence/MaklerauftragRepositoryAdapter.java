package de.immobiliencrm.akquise.infrastructure.persistence;

import de.immobiliencrm.akquise.domain.model.Maklerauftrag;
import de.immobiliencrm.akquise.domain.port.MaklerauftragRepository;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementing the domain repository port using JPA persistence.
 */
@Component
public class MaklerauftragRepositoryAdapter implements MaklerauftragRepository {

    private final JpaMaklerauftragRepository jpaRepository;

    public MaklerauftragRepositoryAdapter(JpaMaklerauftragRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Maklerauftrag> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Maklerauftrag save(Maklerauftrag maklerauftrag) {
        JpaMaklerauftrag entity = toJpa(maklerauftrag);
        JpaMaklerauftrag saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    private Maklerauftrag toDomain(JpaMaklerauftrag entity) {
        Maklerauftrag auftrag = Maklerauftrag.erstellen(entity.getEigentuemerId(), entity.getImmobilieId());
        if (entity.getAbgeschlossenAm() != null) {
            auftrag.abschliessen();
        }
        return auftrag;
    }

    private JpaMaklerauftrag toJpa(Maklerauftrag auftrag) {
        return new JpaMaklerauftrag(
                auftrag.getId(),
                auftrag.getEigentuemerId(),
                auftrag.getImmobilieId(),
                auftrag.getAbgeschlossenAm()
        );
    }
}
