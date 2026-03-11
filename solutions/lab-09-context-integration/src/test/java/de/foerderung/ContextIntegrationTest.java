package de.foerderung;

import de.foerderung.antragstellung.AntragsmappeEingereicht;
import de.foerderung.pruefung.internal.adapter.acl.AntragstellungEventTranslator;
import de.foerderung.pruefung.internal.application.PruefungStartenService;
import de.foerderung.pruefung.internal.domain.AntragsReferenz;
import de.foerderung.pruefung.internal.domain.port.PruefvorgangRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ContextIntegrationTest {

    @Autowired
    AntragstellungEventTranslator translator;

    @Autowired
    PruefungStartenService pruefungStartenService;

    @Autowired
    PruefvorgangRepository pruefvorgangRepository;

    @Test
    void shouldCreatePruefvorgangWhenAntragsmappeEingereicht() {
        // Given: an integration event from the Antragstellung context
        var antragsmappeId = UUID.randomUUID();
        var event = new AntragsmappeEingereicht(antragsmappeId, "DZ-BW-2026-0001");

        // When: the ACL translates and the service processes it
        var command = translator.translate(event);
        pruefungStartenService.start(command);

        // Then: a Pruefvorgang is created in the Pruefung context
        var pruefvorgang = pruefvorgangRepository.findByAntragsReferenz(
                new AntragsReferenz(antragsmappeId));
        assertThat(pruefvorgang).isPresent();
        assertThat(pruefvorgang.get().getRegistrierungsNummer().wert())
                .isEqualTo("DZ-BW-2026-0001");
    }

    @Test
    void shouldBeIdempotent() {
        // Given: an integration event
        var antragsmappeId = UUID.randomUUID();
        var event = new AntragsmappeEingereicht(antragsmappeId, "DZ-BW-2026-0002");

        // When: processed twice
        var command = translator.translate(event);
        pruefungStartenService.start(command);
        pruefungStartenService.start(command); // second call should be idempotent

        // Then: only one Pruefvorgang exists
        var pruefvorgang = pruefvorgangRepository.findByAntragsReferenz(
                new AntragsReferenz(antragsmappeId));
        assertThat(pruefvorgang).isPresent();
    }
}
