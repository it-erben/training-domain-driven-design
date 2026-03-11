package de.foerderung.pruefung.internal.adapter.acl;

import de.foerderung.antragstellung.AntragsmappeEingereicht;
import de.foerderung.pruefung.internal.application.PruefungStartenService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Event listener that reacts to integration events from the Antragstellung context.
 * Uses the ACL translator to convert the event into a Pruefung command.
 */
@Component
public class AntragstellungEventListener {

    private final AntragstellungEventTranslator translator;
    private final PruefungStartenService service;

    public AntragstellungEventListener(AntragstellungEventTranslator translator,
                                        PruefungStartenService service) {
        this.translator = translator;
        this.service = service;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(AntragsmappeEingereicht event) {
        var command = translator.translate(event);
        service.start(command);
    }
}
