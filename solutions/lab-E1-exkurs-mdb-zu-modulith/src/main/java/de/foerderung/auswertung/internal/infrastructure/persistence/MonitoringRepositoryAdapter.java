package de.foerderung.auswertung.internal.infrastructure.persistence;

import de.foerderung.auswertung.internal.domain.model.AntragsReferenz;
import de.foerderung.auswertung.internal.domain.model.MonitoringEintrag;
import de.foerderung.auswertung.internal.domain.port.MonitoringRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MonitoringRepositoryAdapter implements MonitoringRepository {

    private final JpaMonitoringEintragRepository jpaRepository;

    public MonitoringRepositoryAdapter(JpaMonitoringEintragRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public MonitoringEintrag save(MonitoringEintrag eintrag) {
        JpaMonitoringEintrag jpaEntity = JpaMonitoringEintrag.fromModel(eintrag);
        JpaMonitoringEintrag saved = jpaRepository.save(jpaEntity);
        return saved.toModel();
    }

    @Override
    public Optional<MonitoringEintrag> findByAntragsReferenz(AntragsReferenz referenz) {
        return jpaRepository.findByRegistrierungsNummer(referenz.registrierungsNummer())
                .map(JpaMonitoringEintrag::toModel);
    }
}
