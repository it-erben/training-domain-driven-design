package de.immobiliencrm.vermittlung.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.immobiliencrm.vermittlung.application.command.BesichtigungAnlegenCommand;
import de.immobiliencrm.vermittlung.application.command.BesichtigungAnlegenResult;
import de.immobiliencrm.vermittlung.domain.model.Adresse;
import de.immobiliencrm.vermittlung.domain.model.Preisvorstellung;
import de.immobiliencrm.vermittlung.domain.model.Provision;
import de.immobiliencrm.vermittlung.domain.model.Vermittlungsvorgang;
import de.immobiliencrm.vermittlung.domain.model.VermittlungsvorgangNichtGefundenException;
import de.immobiliencrm.vermittlung.domain.model.VermittlungsvorgangStatus;
import de.immobiliencrm.vermittlung.domain.port.VermittlungsvorgangRepository;

@ExtendWith(MockitoExtension.class)
class BesichtigungAnlegenUseCaseTest {

    @Mock
    private VermittlungsvorgangRepository repository;

    private BesichtigungAnlegenUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new BesichtigungAnlegenUseCase(repository);
    }

    @Test
    void anlegen_shouldCreateBesichtigungAndReturnResult() {
        // Arrange
        Vermittlungsvorgang vorgang = Vermittlungsvorgang.erstellen(
                UUID.randomUUID(),
                new Adresse("Hauptstrasse 1", "50667", "Koeln"),
                new Preisvorstellung(new BigDecimal("350000"), "EUR"),
                new Provision(new BigDecimal("3.57"))
        );

        UUID vorgangId = vorgang.getId();
        LocalDateTime zeitpunkt = LocalDateTime.of(2025, 3, 15, 14, 0);

        when(repository.findById(vorgangId)).thenReturn(Optional.of(vorgang));
        when(repository.save(any(Vermittlungsvorgang.class))).thenReturn(vorgang);

        BesichtigungAnlegenCommand command = new BesichtigungAnlegenCommand(
                vorgangId, "Max Mustermann", zeitpunkt);

        // Act
        BesichtigungAnlegenResult result = useCase.anlegen(command);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.besichtigungId()).isNotNull();
        assertThat(result.vermittlungsvorgangId()).isEqualTo(vorgangId);
        assertThat(vorgang.getBesichtigungen()).hasSize(1);
        assertThat(vorgang.getStatus()).isEqualTo(VermittlungsvorgangStatus.BESICHTIGUNG);

        verify(repository).findById(vorgangId);
        verify(repository).save(vorgang);
    }

    @Test
    void anlegen_shouldThrowExceptionWhenVorgangNotFound() {
        // Arrange
        UUID unknownId = UUID.randomUUID();
        LocalDateTime zeitpunkt = LocalDateTime.of(2025, 3, 15, 14, 0);

        when(repository.findById(unknownId)).thenReturn(Optional.empty());

        BesichtigungAnlegenCommand command = new BesichtigungAnlegenCommand(
                unknownId, "Max Mustermann", zeitpunkt);

        // Act & Assert
        assertThatThrownBy(() -> useCase.anlegen(command))
                .isInstanceOf(VermittlungsvorgangNichtGefundenException.class)
                .hasMessageContaining(unknownId.toString());

        verify(repository).findById(unknownId);
        verify(repository, never()).save(any());
    }
}
