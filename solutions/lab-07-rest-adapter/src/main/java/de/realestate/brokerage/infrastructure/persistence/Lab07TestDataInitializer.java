package de.realestate.brokerage.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import de.realestate.brokerage.domain.model.AskingPrice;
import de.realestate.brokerage.domain.model.BrokerageProcess;
import de.realestate.brokerage.domain.model.Commission;
import de.realestate.brokerage.domain.model.ProcessStatus;
import de.realestate.brokerage.domain.model.Viewing;
import de.realestate.brokerage.domain.port.BrokerageProcessRepository;

@Component
public class Lab07TestDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(Lab07TestDataInitializer.class);

    private static final UUID PROCESS_ID_WITH_VIEWINGS =
            UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID PROCESS_ID_EMPTY =
            UUID.fromString("22222222-2222-2222-2222-222222222222");

    private final BrokerageProcessRepository repository;

    public Lab07TestDataInitializer(BrokerageProcessRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        seedIfMissing(PROCESS_ID_WITH_VIEWINGS, createProcessWithViewings());
        seedIfMissing(PROCESS_ID_EMPTY, createEmptyProcess());

        log.info("Lab 07 test data ready. Use process IDs {} and {} for the Viewings API.",
                PROCESS_ID_WITH_VIEWINGS, PROCESS_ID_EMPTY);
    }

    private void seedIfMissing(UUID processId, BrokerageProcess process) {
        if (repository.findById(processId).isEmpty()) {
            repository.save(process);
        }
    }

    private BrokerageProcess createProcessWithViewings() {
        Viewing plannedViewing = new Viewing(
                UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
                "Max Mustermann",
                LocalDateTime.of(2026, 4, 1, 14, 0));

        Viewing completedViewing = new Viewing(
                UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
                "Erika Musterfrau",
                LocalDateTime.of(2026, 4, 3, 11, 30));
        completedViewing.complete();

        return BrokerageProcess.reconstitute(
                PROCESS_ID_WITH_VIEWINGS,
                UUID.fromString("33333333-3333-3333-3333-333333333333"),
                new AskingPrice(new BigDecimal("500000.00"), "EUR"),
                new Commission(new BigDecimal("3.57")),
                ProcessStatus.VIEWING,
                List.of(plannedViewing, completedViewing),
                List.of());
    }

    private BrokerageProcess createEmptyProcess() {
        return BrokerageProcess.reconstitute(
                PROCESS_ID_EMPTY,
                UUID.fromString("44444444-4444-4444-4444-444444444444"),
                new AskingPrice(new BigDecimal("725000.00"), "EUR"),
                new Commission(new BigDecimal("3.57")),
                ProcessStatus.NEW,
                List.of(),
                List.of());
    }
}
