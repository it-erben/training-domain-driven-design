package de.immobiliencrm.vermittlung.application.listener;

import de.immobiliencrm.akquise.domain.event.MaklervertragAbgeschlossen;
import de.immobiliencrm.vermittlung.domain.model.Adresse;
import de.immobiliencrm.vermittlung.domain.model.Preisvorstellung;
import de.immobiliencrm.vermittlung.domain.model.Provision;
import de.immobiliencrm.vermittlung.domain.model.Vermittlungsvorgang;
import de.immobiliencrm.vermittlung.domain.port.VermittlungsvorgangRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Listener that creates a new Vermittlungsvorgang when a Maklervertrag is finalized.
 */
@Component
public class MaklervertragAbgeschlossenListener {

    private static final Logger log = LoggerFactory.getLogger(MaklervertragAbgeschlossenListener.class);

    private final VermittlungsvorgangRepository repository;

    public MaklervertragAbgeschlossenListener(VermittlungsvorgangRepository repository) {
        this.repository = repository;
    }

    @EventListener
    public void handle(MaklervertragAbgeschlossen event) {
        log.info("Received MaklervertragAbgeschlossen event for immobilieId={}", event.immobilieId());

        // Create a new Vermittlungsvorgang based on the event data
        Vermittlungsvorgang vorgang = Vermittlungsvorgang.erstellen(
                event.immobilieId(),
                new Adresse("Standardstrasse 1", "00000", "Unbekannt"),
                new Preisvorstellung(new BigDecimal("100000"), "EUR"),
                new Provision(new BigDecimal("3.57"))
        );

        repository.save(vorgang);

        log.info("Created Vermittlungsvorgang with id={} for immobilieId={}",
                vorgang.getId(), event.immobilieId());
    }
}
