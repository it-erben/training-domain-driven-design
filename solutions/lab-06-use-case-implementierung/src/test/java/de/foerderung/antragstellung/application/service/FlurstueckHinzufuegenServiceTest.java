package de.foerderung.antragstellung.application.service;

import de.foerderung.antragstellung.application.command.FlurstueckHinzufuegenCommand;
import de.foerderung.antragstellung.application.command.FlurstueckHinzufuegenResult;
import de.foerderung.antragstellung.domain.model.AntragId;
import de.foerderung.antragstellung.domain.model.AntragsMappe;
import de.foerderung.antragstellung.domain.model.AntragsmappeNichtGefundenException;
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
class FlurstueckHinzufuegenServiceTest {

    @Mock
    private AntragsMappeRepository repository;

    @InjectMocks
    private FlurstueckHinzufuegenService service;

    private AntragsMappe createMappe() {
        return AntragsMappe.erstellen(
                new RegistrierungsNummer("DZ-BW-2024-0042"),
                new Foerderbetrag(new BigDecimal("10000"), "EUR"),
                new Foerderquote(new BigDecimal("0.35"))
        );
    }

    @Test
    void hinzufuegen_happyPath_returnsResultAndSaves() {
        var mappe = createMappe();
        var cmd = new FlurstueckHinzufuegenCommand(
                mappe.getId(),
                new FlurstueckNummer("042/0815"),
                new BigDecimal("12.5")
        );

        when(repository.findById(mappe.getId())).thenReturn(Optional.of(mappe));
        when(repository.save(any())).thenReturn(mappe);

        FlurstueckHinzufuegenResult result = service.hinzufuegen(cmd);

        assertThat(result.antragsmappeId()).isEqualTo(mappe.getId());
        assertThat(result.flurstueckNummer().wert()).isEqualTo("042/0815");
        assertThat(result.flaeche()).isEqualByComparingTo(new BigDecimal("12.5"));
        assertThat(result.flurstueckId()).isNotNull();

        verify(repository).save(mappe);
    }

    @Test
    void hinzufuegen_mappeNotFound_throwsExceptionAndDoesNotSave() {
        var unknownId = AntragId.generate();
        var cmd = new FlurstueckHinzufuegenCommand(
                unknownId,
                new FlurstueckNummer("042/0815"),
                new BigDecimal("12.5")
        );

        when(repository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.hinzufuegen(cmd))
                .isInstanceOf(AntragsmappeNichtGefundenException.class)
                .hasMessageContaining(unknownId.toString());

        verify(repository, never()).save(any());
    }
}
