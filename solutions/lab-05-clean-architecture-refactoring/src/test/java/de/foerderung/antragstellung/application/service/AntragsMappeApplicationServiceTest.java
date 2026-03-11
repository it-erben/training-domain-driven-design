package de.foerderung.antragstellung.application.service;

import de.foerderung.antragstellung.domain.model.AntragId;
import de.foerderung.antragstellung.domain.model.AntragStatus;
import de.foerderung.antragstellung.domain.model.AntragsMappe;
import de.foerderung.antragstellung.domain.model.Foerderbetrag;
import de.foerderung.antragstellung.domain.model.Foerderquote;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AntragsMappeApplicationServiceTest {

    @Mock
    private AntragsMappeRepository repository;

    @InjectMocks
    private AntragsMappeApplicationService service;

    private final RegistrierungsNummer regNummer = new RegistrierungsNummer("DZ-BW-2024-0042");
    private final Foerderbetrag foerderbetrag = new Foerderbetrag(new BigDecimal("10000"), "EUR");
    private final Foerderquote foerderquote = new Foerderquote(new BigDecimal("0.35"));

    @Test
    void erstellen_delegatesToFactoryMethodAndSaves() {
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.erstellen(regNummer, foerderbetrag, foerderquote);

        assertThat(result.getStatus()).isEqualTo(AntragStatus.NEU);
        assertThat(result.getRegistrierungsNummer()).isEqualTo(regNummer);
        verify(repository).save(any(AntragsMappe.class));
    }

    @Test
    void findById_existingMappe_returnsMappe() {
        var id = AntragId.generate();
        var mappe = AntragsMappe.erstellen(regNummer, foerderbetrag, foerderquote);
        when(repository.findById(id)).thenReturn(Optional.of(mappe));

        var result = service.findById(id);

        assertThat(result).isPresent();
    }

    @Test
    void findById_nonExistent_returnsEmpty() {
        when(repository.findById(any())).thenReturn(Optional.empty());

        var result = service.findById(AntragId.generate());

        assertThat(result).isEmpty();
    }
}
