package de.immobiliencrm.vermittlung.application.listener;

import de.immobiliencrm.akquise.domain.event.MaklervertragAbgeschlossen;
import de.immobiliencrm.vermittlung.domain.model.Adresse;
import de.immobiliencrm.vermittlung.domain.model.Preisvorstellung;
import de.immobiliencrm.vermittlung.domain.model.Provision;
import de.immobiliencrm.vermittlung.domain.model.Vermittlungsvorgang;
import de.immobiliencrm.vermittlung.domain.port.VermittlungsvorgangRepository;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Listener that reacts to MaklervertragAbgeschlossen events from the Akquise BC
 * and creates a new Vermittlungsvorgang in the Vermittlung BC.
 */
@Component
public class MaklervertragAbgeschlossenListener {

    private final VermittlungsvorgangRepository repository;

    public MaklervertragAbgeschlossenListener(VermittlungsvorgangRepository repository) {
        this.repository = repository;
    }

    @EventListener
    public void handle(MaklervertragAbgeschlossen event) {
        // Create a new Vermittlungsvorgang with default values for the address and pricing.
        // In a real application, these would be resolved from additional context or services.
        Adresse adresse = new Adresse("Noch zu erfassen", "00000", "Unbekannt");
        Preisvorstellung preis = new Preisvorstellung(new BigDecimal("1"), "EUR");
        Provision provision = new Provision(new BigDecimal("3.57"));

        Vermittlungsvorgang vorgang = Vermittlungsvorgang.erstellen(
                event.immobilieId(), adresse, preis, provision);

        repository.save(vorgang);
    }
}
