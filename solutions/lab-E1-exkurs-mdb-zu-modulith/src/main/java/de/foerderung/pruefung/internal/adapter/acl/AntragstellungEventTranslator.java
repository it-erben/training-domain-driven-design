package de.foerderung.pruefung.internal.adapter.acl;

import de.foerderung.antragstellung.AntragsmappeEingereicht;
import de.foerderung.pruefung.internal.application.PruefungStartenCommand;
import de.foerderung.pruefung.internal.domain.AntragsReferenz;
import de.foerderung.pruefung.internal.domain.RegistrierungsNummer;
import org.springframework.stereotype.Component;

@Component
class AntragstellungEventTranslator {

    PruefungStartenCommand translate(AntragsmappeEingereicht event) {
        return new PruefungStartenCommand(
                new AntragsReferenz(event.antragsmappeId()),
                new RegistrierungsNummer(event.registrierungsNummer()),
                event.eingereichtAm());
    }
}
