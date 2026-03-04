package de.immobiliencrm.akquise.application.service;

import de.immobiliencrm.akquise.domain.event.MaklervertragAbgeschlossen;
import de.immobiliencrm.akquise.domain.model.Maklerauftrag;
import de.immobiliencrm.akquise.domain.port.MaklerauftragRepository;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Application service for concluding a broker contract.
 * Publishes an integration event after successful completion.
 */
@Service
@Transactional
public class MaklerauftragAbschliessenUseCase {

    private final MaklerauftragRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    public MaklerauftragAbschliessenUseCase(MaklerauftragRepository repository,
                                             ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Concludes the broker contract and publishes a MaklervertragAbgeschlossen event.
     */
    public void abschliessen(UUID maklerauftragId) {
        Maklerauftrag auftrag = repository.findById(maklerauftragId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Maklerauftrag mit ID " + maklerauftragId + " nicht gefunden"));

        auftrag.abschliessen();
        repository.save(auftrag);

        eventPublisher.publishEvent(new MaklervertragAbgeschlossen(
                auftrag.getId(),
                auftrag.getImmobilieId(),
                auftrag.getAbgeschlossenAm()
        ));
    }

    /**
     * Creates a new Maklerauftrag and returns it.
     */
    public Maklerauftrag erstellen(UUID eigentuemerId, UUID immobilieId) {
        Maklerauftrag auftrag = Maklerauftrag.erstellen(eigentuemerId, immobilieId);
        return repository.save(auftrag);
    }
}
