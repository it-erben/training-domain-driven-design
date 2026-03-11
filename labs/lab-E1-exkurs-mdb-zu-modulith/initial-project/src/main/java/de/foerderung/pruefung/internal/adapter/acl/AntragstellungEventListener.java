package de.foerderung.pruefung.internal.adapter.acl;

import de.foerderung.antragstellung.AntragsmappeEingereicht;
import de.foerderung.pruefung.internal.application.PruefungStartenService;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Listens for events from the Antragstellung context and delegates to the
 * Pruefung application service via the ACL translator.
 *
 * Uses @ApplicationModuleListener (Spring Modulith) instead of
 * @TransactionalEventListener for reliable event processing with
 * Event Publication Registry support.
 */
@Component
class AntragstellungEventListener {

    private final AntragstellungEventTranslator translator;
    private final PruefungStartenService service;

    AntragstellungEventListener(AntragstellungEventTranslator translator,
                                PruefungStartenService service) {
        this.translator = translator;
        this.service = service;
    }

    @ApplicationModuleListener
    void on(AntragsmappeEingereicht event) {
        var command = translator.translate(event);
        service.start(command);
    }
}
