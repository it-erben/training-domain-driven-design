package de.foerderung.pruefung;

import de.foerderung.antragstellung.AntragsmappeEingereicht;
import de.foerderung.pruefung.internal.domain.AntragsReferenz;
import de.foerderung.pruefung.internal.domain.port.PruefvorgangRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ApplicationModuleTest
class PruefungModuleTest {

    @Autowired
    private PruefvorgangRepository pruefvorgangRepository;

    @Test
    void antragEingereicht_startetPruefung(Scenario scenario) {
        var event = new AntragsmappeEingereicht(
                UUID.randomUUID(), "DZ-BW-2024-0042", Instant.now());

        scenario.publish(event)
                .andWaitForStateChange(() -> pruefvorgangRepository.findByAntragsReferenz(
                        new AntragsReferenz(event.antragsmappeId())))
                .andVerify(result -> {
                    assertThat(result).isPresent();
                    assertThat(result.get().getRegistrierungsNummer().wert())
                            .isEqualTo("DZ-BW-2024-0042");
                });
    }
}
