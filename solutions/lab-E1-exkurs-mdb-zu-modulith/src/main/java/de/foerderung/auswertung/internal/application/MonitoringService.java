package de.foerderung.auswertung.internal.application;

import de.foerderung.auswertung.internal.domain.model.MonitoringEintrag;
import de.foerderung.auswertung.internal.domain.model.MonitoringEintragId;
import de.foerderung.auswertung.internal.domain.port.MonitoringRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application Service for monitoring synchronization.
 * Implements idempotent Update-or-Create pattern:
 * - If a MonitoringEintrag already exists for the AntragsReferenz: update status
 * - If not: create a new one
 *
 * This replaces the legacy OptimusPrime.synchronisiere() which had no idempotency check.
 */
@Service
@Transactional
public class MonitoringService {

    private final MonitoringRepository repository;

    public MonitoringService(MonitoringRepository repository) {
        this.repository = repository;
    }

    public void synchronisiere(MonitoringSynchronisierenCommand cmd) {
        repository.findByAntragsReferenz(cmd.antragsReferenz())
                .ifPresentOrElse(
                        eintrag -> {
                            eintrag.aktualisiere(cmd.status(), cmd.geaendertAm());
                            repository.save(eintrag);
                        },
                        () -> repository.save(MonitoringEintrag.erstellen(
                                MonitoringEintragId.generate(),
                                cmd.antragsReferenz(),
                                cmd.status(),
                                cmd.geaendertAm()))
                );
    }
}
