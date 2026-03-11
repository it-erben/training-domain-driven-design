package de.foerderung.auswertung.internal.adapter.acl;

import de.foerderung.antragstellung.AntragsmappeGeaendert;
import de.foerderung.auswertung.internal.application.MonitoringService;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Spring Modulith Event Listener — replaces the legacy MonitoringSynchronizerMDB.
 *
 * The MDB:
 *   @MessageDriven(activationConfig = { destination = "topic/AntragGeaendert" })
 *   public class MonitoringSynchronizerMDB implements MessageListener {
 *       public void onMessage(Message message) {
 *           AntragsmappeAenderung aend = (AntragsmappeAenderung) ((ObjectMessage) message).getObject();
 *           optimusPrime.synchronisiere(aend.getRegistrationNumber());
 *       }
 *   }
 *
 * is now:
 *   Translator + Listener + MonitoringService (with idempotency)
 */
@Component
class AntragsmappeEventListener {

    private final AntragsmappeEventTranslator translator;
    private final MonitoringService service;

    AntragsmappeEventListener(AntragsmappeEventTranslator translator,
                               MonitoringService service) {
        this.translator = translator;
        this.service = service;
    }

    @ApplicationModuleListener
    void on(AntragsmappeGeaendert event) {
        var command = translator.translate(event);
        service.synchronisiere(command);
    }
}
