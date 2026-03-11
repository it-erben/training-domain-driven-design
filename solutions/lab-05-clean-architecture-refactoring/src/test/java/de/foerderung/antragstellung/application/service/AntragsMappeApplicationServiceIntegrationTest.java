package de.foerderung.antragstellung.application.service;

import de.foerderung.antragstellung.domain.model.Foerderbetrag;
import de.foerderung.antragstellung.domain.model.Foerderquote;
import de.foerderung.antragstellung.domain.model.RegistrierungsNummer;
import de.foerderung.antragstellung.domain.port.AntragsMappeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class AntragsMappeApplicationServiceIntegrationTest {

    @Autowired
    private AntragsMappeApplicationService service;

    @Autowired
    private AntragsMappeRepository repository;

    @Test
    void erstellen_persistsAntragsMappe() {
        var regNummer = new RegistrierungsNummer("DZ-BW-2024-0042");
        var foerderbetrag = new Foerderbetrag(new BigDecimal("10000"), "EUR");
        var foerderquote = new Foerderquote(new BigDecimal("0.35"));

        var created = service.erstellen(regNummer, foerderbetrag, foerderquote);

        var reloaded = repository.findById(created.getId());

        assertThat(reloaded).isPresent();
        assertThat(reloaded.orElseThrow().getRegistrierungsNummer()).isEqualTo(regNummer);
        var loaded = reloaded.orElseThrow();
        assertThat(loaded.getBeantragteFoerderung().betrag())
                .isEqualByComparingTo(foerderbetrag.betrag());
        assertThat(loaded.getBeantragteFoerderung().waehrung())
                .isEqualTo(foerderbetrag.waehrung());
        assertThat(loaded.getFoerderquote().prozentsatz())
                .isEqualByComparingTo(foerderquote.prozentsatz());
    }
}
