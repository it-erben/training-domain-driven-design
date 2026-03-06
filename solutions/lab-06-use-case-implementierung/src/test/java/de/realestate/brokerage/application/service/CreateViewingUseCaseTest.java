package de.realestate.brokerage.application.service;

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

import de.realestate.brokerage.application.command.CreateViewingCommand;
import de.realestate.brokerage.application.command.CreateViewingResult;
import de.realestate.brokerage.domain.model.Address;
import de.realestate.brokerage.domain.model.AskingPrice;
import de.realestate.brokerage.domain.model.Commission;
import de.realestate.brokerage.domain.model.BrokerageProcess;
import de.realestate.brokerage.domain.model.ProcessNotFoundException;
import de.realestate.brokerage.domain.model.ProcessStatus;
import de.realestate.brokerage.domain.port.BrokerageProcessRepository;

@ExtendWith(MockitoExtension.class)
class CreateViewingUseCaseTest {

    @Mock
    private BrokerageProcessRepository repository;

    private CreateViewingUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateViewingUseCase(repository);
    }

    @Test
    void should_createViewingAndReturnResult() {
        // Arrange
        BrokerageProcess process = BrokerageProcess.create(
                UUID.randomUUID(),
                new Address("Hauptstrasse 1", "50667", "Koeln"),
                new AskingPrice(new BigDecimal("350000"), "EUR"),
                new Commission(new BigDecimal("3.57"))
        );

        UUID processId = process.getId();
        LocalDateTime appointmentDate = LocalDateTime.of(2025, 3, 15, 14, 0);

        when(repository.findById(processId)).thenReturn(Optional.of(process));
        when(repository.save(any(BrokerageProcess.class))).thenReturn(process);

        CreateViewingCommand command = new CreateViewingCommand(
                processId, "Max Mustermann", appointmentDate);

        // Act
        CreateViewingResult result = useCase.create(command);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.viewingId()).isNotNull();
        assertThat(result.processId()).isEqualTo(processId);
        assertThat(process.getViewings()).hasSize(1);
        assertThat(process.getStatus()).isEqualTo(ProcessStatus.VIEWING);

        verify(repository).findById(processId);
        verify(repository).save(process);
    }

    @Test
    void should_throwExceptionWhenProcessNotFound() {
        // Arrange
        UUID unknownId = UUID.randomUUID();
        LocalDateTime appointmentDate = LocalDateTime.of(2025, 3, 15, 14, 0);

        when(repository.findById(unknownId)).thenReturn(Optional.empty());

        CreateViewingCommand command = new CreateViewingCommand(
                unknownId, "Max Mustermann", appointmentDate);

        // Act & Assert
        assertThatThrownBy(() -> useCase.create(command))
                .isInstanceOf(ProcessNotFoundException.class)
                .hasMessageContaining(unknownId.toString());

        verify(repository).findById(unknownId);
        verify(repository, never()).save(any());
    }
}
