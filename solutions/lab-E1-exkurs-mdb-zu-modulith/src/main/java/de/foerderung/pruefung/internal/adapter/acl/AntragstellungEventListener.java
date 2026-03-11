package de.foerderung.pruefung.internal.adapter.acl;

import de.foerderung.antragstellung.AntragsmappeEingereicht;
import de.foerderung.pruefung.internal.application.PruefungStartenService;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

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
