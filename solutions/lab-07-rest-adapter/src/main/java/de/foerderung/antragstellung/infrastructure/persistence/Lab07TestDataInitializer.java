package de.foerderung.antragstellung.infrastructure.persistence;

import de.foerderung.antragstellung.domain.model.AntragsMappe;
import de.foerderung.antragstellung.domain.model.AntragStatus;
import de.foerderung.antragstellung.domain.model.Flurstueck;
import de.foerderung.antragstellung.domain.model.FlurstueckNummer;
import de.foerderung.antragstellung.domain.model.Foerderbetrag;
import de.foerderung.antragstellung.domain.model.Foerderquote;
import de.foerderung.antragstellung.domain.model.RegistrierungsNummer;
import de.foerderung.antragstellung.domain.port.AntragsMappeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
public class Lab07TestDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(Lab07TestDataInitializer.class);

    private static final UUID MAPPE_ID_WITH_FLURSTUECKE =
            UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID MAPPE_ID_EMPTY =
            UUID.fromString("22222222-2222-2222-2222-222222222222");

    private final AntragsMappeRepository repository;

    public Lab07TestDataInitializer(AntragsMappeRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        seedIfMissing(MAPPE_ID_WITH_FLURSTUECKE, createMappeWithFlurstuecke());
        seedIfMissing(MAPPE_ID_EMPTY, createEmptyMappe());

        log.info("Lab 07 test data ready. Use AntragsMappe IDs {} and {} for the Flurstuecke API.",
                MAPPE_ID_WITH_FLURSTUECKE, MAPPE_ID_EMPTY);
    }

    private void seedIfMissing(UUID mappeId, AntragsMappe mappe) {
        if (repository.findById(mappeId).isEmpty()) {
            repository.save(mappe);
        }
    }

    private AntragsMappe createMappeWithFlurstuecke() {
        Flurstueck flurstueck1 = Flurstueck.rekonstruieren(
                UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
                new FlurstueckNummer("012-00345-00678"),
                new BigDecimal("12.50"),
                "Ackerland Nordfeld",
                false);

        Flurstueck flurstueck2 = Flurstueck.rekonstruieren(
                UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
                new FlurstueckNummer("012-00345-00679"),
                new BigDecimal("8.75"),
                "Gruenland Suedwiese",
                true);

        return AntragsMappe.rekonstruieren(
                MAPPE_ID_WITH_FLURSTUECKE,
                new RegistrierungsNummer("REG-2026-001"),
                new Foerderbetrag(new BigDecimal("45000.00"), "EUR"),
                new Foerderquote(new BigDecimal("0.35")),
                AntragStatus.IN_BEARBEITUNG,
                List.of(flurstueck1, flurstueck2),
                List.of());
    }

    private AntragsMappe createEmptyMappe() {
        return AntragsMappe.rekonstruieren(
                MAPPE_ID_EMPTY,
                new RegistrierungsNummer("REG-2026-002"),
                new Foerderbetrag(new BigDecimal("72000.00"), "EUR"),
                new Foerderquote(new BigDecimal("0.40")),
                AntragStatus.NEU,
                List.of(),
                List.of());
    }
}
