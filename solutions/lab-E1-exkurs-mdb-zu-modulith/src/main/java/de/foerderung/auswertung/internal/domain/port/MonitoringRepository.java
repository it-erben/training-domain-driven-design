package de.foerderung.auswertung.internal.domain.port;

import de.foerderung.auswertung.internal.domain.model.AntragsReferenz;
import de.foerderung.auswertung.internal.domain.model.MonitoringEintrag;

import java.util.Optional;

/**
 * Repository port for MonitoringEintrag aggregates.
 * Pure Java interface — no Spring or JPA dependencies.
 */
public interface MonitoringRepository {

    MonitoringEintrag save(MonitoringEintrag eintrag);

    Optional<MonitoringEintrag> findByAntragsReferenz(AntragsReferenz referenz);
}
