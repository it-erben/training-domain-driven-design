package de.foerderung.auswertung.internal.adapter.acl;

import de.foerderung.antragstellung.AenderungsArt;
import de.foerderung.antragstellung.AntragsmappeGeaendert;
import de.foerderung.auswertung.internal.application.MonitoringSynchronisierenCommand;
import de.foerderung.auswertung.internal.domain.model.AntragsReferenz;
import de.foerderung.auswertung.internal.domain.model.MonitoringsStatus;
import org.springframework.stereotype.Component;

/**
 * Anti-Corruption Layer (ACL) Translator:
 * Converts events from the Antragstellung context into commands for the Auswertung context.
 *
 * This is the missing piece from the legacy MDB pattern:
 * - BEFORE: MDB casts directly on AntragsmappeAenderung (foreign object)
 * - AFTER: Translator converts to own Command with own Value Objects
 */
@Component
class AntragsmappeEventTranslator {

    MonitoringSynchronisierenCommand translate(AntragsmappeGeaendert event) {
        return new MonitoringSynchronisierenCommand(
                new AntragsReferenz(event.registrierungsNummer()),
                mapStatus(event.aenderungsArt()),
                event.geaendertAm()
        );
    }

    private MonitoringsStatus mapStatus(AenderungsArt art) {
        return switch (art) {
            case AKTUALISIERT, REAKTIVIERT -> MonitoringsStatus.AKTIV;
            case ENTFERNT -> MonitoringsStatus.INAKTIV;
            case ARCHIVIERT -> MonitoringsStatus.ARCHIVIERT;
        };
    }
}
