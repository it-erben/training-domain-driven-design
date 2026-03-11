package de.foerderung.pruefung.internal.adapter.acl;

import de.foerderung.antragstellung.AntragsmappeEingereicht;
import de.foerderung.pruefung.internal.application.PruefungStartenCommand;
import de.foerderung.pruefung.internal.domain.AntragsReferenz;
import de.foerderung.pruefung.internal.domain.RegistrierungsNummer;
import org.springframework.stereotype.Component;

/**
 * Anti-Corruption Layer (ACL) translator that converts events from the
 * Antragstellung context into commands for the Pruefung context.
 * This prevents the Antragstellung domain model from leaking into Pruefung.
 */
@Component
public class AntragstellungEventTranslator {

    public PruefungStartenCommand translate(AntragsmappeEingereicht event) {
        return new PruefungStartenCommand(
                new AntragsReferenz(event.antragsmappeId()),
                new RegistrierungsNummer(event.registrierungsNummer()),
                event.eingereichtAm());
    }
}
