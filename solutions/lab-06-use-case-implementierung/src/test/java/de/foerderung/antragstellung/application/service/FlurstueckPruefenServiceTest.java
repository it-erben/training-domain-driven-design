package de.foerderung.antragstellung.application.service;

import de.foerderung.antragstellung.application.command.FlurstueckPruefenCommand;
import de.foerderung.antragstellung.domain.model.AntragId;
import de.foerderung.antragstellung.domain.model.AntragsMappe;
import de.foerderung.antragstellung.domain.model.AntragsmappeNichtGefundenException;
import de.foerderung.antragstellung.domain.model.FlurstueckId;
import de.foerderung.antragstellung.domain.model.Foerderbetrag;
import de.foerderung.antragstellung.domain.model.Foerderquote;
import de.foerderung.antragstellung.domain.model.FlurstueckNummer;
import de.foerderung.antragstellung.domain.model.RegistrierungsNummer;
import de.foerderung.antragstellung.domain.port.AntragsMappeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlurstueckPruefenServiceTest {

    @Mock
    private AntragsMappeRepository repository;

    @InjectMocks
    private FlurstueckPruefenService service;

    private AntragsMappe createMappe() {
        return AntragsMappe.erstellen(
                new RegistrierungsNummer("DZ-BW-2024-0042"),
                new Foerderbetrag(new BigDecimal("10000"), "EUR"),
                new Foerderquote(new BigDecimal("0.35"))
        );
    }

    @Test
    void pruefen_happyPath_marksFlurstueckAsGeprueftAndSaves() {
        var mappe = createMappe();
        var flurstueckId = mappe.flurstueckHinzufuegen(
                new FlurstueckNummer("042/0815"), new BigDecimal("12.5"), null);

        var cmd = new FlurstueckPruefenCommand(mappe.getId(), flurstueckId);

        when(repository.findById(mappe.getId())).thenReturn(Optional.of(mappe));
        when(repository.save(any())).thenReturn(mappe);

        service.pruefen(cmd);

        assertThat(mappe.getFlurstuecke().get(0).isGeprueft()).isTrue();
        verify(repository).save(mappe);
    }

    @Test
    void pruefen_mappeNotFound_throwsException() {
        var unknownId = AntragId.generate();
        var cmd = new FlurstueckPruefenCommand(unknownId, FlurstueckId.generate());

        when(repository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.pruefen(cmd))
                .isInstanceOf(AntragsmappeNichtGefundenException.class)
                .hasMessageContaining(unknownId.toString());

        verify(repository, never()).save(any());
    }

    @Test
    void pruefen_flurstueckNotFound_throwsIllegalArgumentException() {
        var mappe = createMappe();
        mappe.flurstueckHinzufuegen(
                new FlurstueckNummer("042/0815"), new BigDecimal("12.5"), null);

        var unknownFlurstueckId = FlurstueckId.generate();
        var cmd = new FlurstueckPruefenCommand(mappe.getId(), unknownFlurstueckId);

        when(repository.findById(mappe.getId())).thenReturn(Optional.of(mappe));

        assertThatThrownBy(() -> service.pruefen(cmd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(unknownFlurstueckId.toString());
    }
}
