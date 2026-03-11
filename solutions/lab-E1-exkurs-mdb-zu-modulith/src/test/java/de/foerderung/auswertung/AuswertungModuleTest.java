package de.foerderung.auswertung;

import de.foerderung.antragstellung.AenderungsArt;
import de.foerderung.antragstellung.AntragsmappeGeaendert;
import de.foerderung.auswertung.internal.domain.model.AntragsReferenz;
import de.foerderung.auswertung.internal.domain.model.MonitoringsStatus;
import de.foerderung.auswertung.internal.domain.port.MonitoringRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@ApplicationModuleTest
class AuswertungModuleTest {

    @Autowired
    private MonitoringRepository monitoringRepository;

    @Test
    void antragGeaendert_erstelltMonitoringEintrag(Scenario scenario) {
        var event = new AntragsmappeGeaendert(
                "DZ-BW-2026-0099",
                AenderungsArt.AKTUALISIERT,
                Instant.now());

        scenario.publish(event)
                .andWaitForStateChange(() -> monitoringRepository.findByAntragsReferenz(
                        new AntragsReferenz("DZ-BW-2026-0099")))
                .andVerify(result -> {
                    assertThat(result).isPresent();
                    assertThat(result.get().getStatus()).isEqualTo(MonitoringsStatus.AKTIV);
                    assertThat(result.get().getAntragsReferenz().registrierungsNummer())
                            .isEqualTo("DZ-BW-2026-0099");
                });
    }

    @Test
    void doppeltesEvent_aktualisiertNurBestehendenEintrag(Scenario scenario) {
        var referenz = new AntragsReferenz("DZ-BW-2026-IDEMPOTENT");

        // First event: create
        var event1 = new AntragsmappeGeaendert(
                "DZ-BW-2026-IDEMPOTENT",
                AenderungsArt.AKTUALISIERT,
                Instant.parse("2026-01-01T10:00:00Z"));

        scenario.publish(event1)
                .andWaitForStateChange(() -> monitoringRepository.findByAntragsReferenz(referenz))
                .andVerify(result -> assertThat(result).isPresent());

        // Second event: update (idempotent — same referenz, different status)
        var event2 = new AntragsmappeGeaendert(
                "DZ-BW-2026-IDEMPOTENT",
                AenderungsArt.ARCHIVIERT,
                Instant.parse("2026-01-02T10:00:00Z"));

        scenario.publish(event2)
                .andWaitForStateChange(() -> monitoringRepository.findByAntragsReferenz(referenz)
                        .filter(e -> e.getStatus() == MonitoringsStatus.ARCHIVIERT))
                .andVerify(result -> {
                    assertThat(result).isPresent();
                    assertThat(result.get().getStatus()).isEqualTo(MonitoringsStatus.ARCHIVIERT);
                    assertThat(result.get().getZuletztGeaendertAm())
                            .isEqualTo(Instant.parse("2026-01-02T10:00:00Z"));
                });
    }
}
