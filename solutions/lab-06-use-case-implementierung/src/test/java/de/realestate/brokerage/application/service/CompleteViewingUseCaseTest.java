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

import de.realestate.brokerage.application.command.CompleteViewingCommand;
import de.realestate.brokerage.domain.event.ViewingCompleted;
import de.realestate.brokerage.domain.model.AskingPrice;
import de.realestate.brokerage.domain.model.BrokerageProcess;
import de.realestate.brokerage.domain.model.Commission;
import de.realestate.brokerage.domain.model.ProcessNotFoundException;
import de.realestate.brokerage.domain.port.BrokerageProcessRepository;

@ExtendWith(MockitoExtension.class)
class CompleteViewingUseCaseTest {

    @Mock
    private BrokerageProcessRepository repository;

    private CompleteViewingUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CompleteViewingUseCase(repository);
    }

    @Test
    void should_completeViewingSuccessfully() {
        // Arrange
        BrokerageProcess process = BrokerageProcess.create(
                UUID.randomUUID(),
                new AskingPrice(new BigDecimal("350000"), "EUR"),
                new Commission(new BigDecimal("3.57"))
        );

        UUID viewingId = process.addViewing("Max Mustermann",
                LocalDateTime.of(2025, 3, 15, 14, 0));
        UUID processId = process.getId();

        when(repository.findById(processId)).thenReturn(Optional.of(process));
        when(repository.save(any(BrokerageProcess.class))).thenReturn(process);

        CompleteViewingCommand command = new CompleteViewingCommand(processId, viewingId);

        // Act
        useCase.complete(command);

        // Assert
        assertThat(process.getViewings().get(0).isCompleted()).isTrue();
        assertThat(process.getDomainEvents()).hasSize(1);
        assertThat(process.getDomainEvents().get(0)).isInstanceOf(ViewingCompleted.class);

        verify(repository).findById(processId);
        verify(repository).save(process);
    }

    @Test
    void should_throwExceptionWhenProcessNotFound() {
        // Arrange
        UUID unknownProcessId = UUID.randomUUID();
        UUID viewingId = UUID.randomUUID();

        when(repository.findById(unknownProcessId)).thenReturn(Optional.empty());

        CompleteViewingCommand command = new CompleteViewingCommand(unknownProcessId, viewingId);

        // Act & Assert
        assertThatThrownBy(() -> useCase.complete(command))
                .isInstanceOf(ProcessNotFoundException.class)
                .hasMessageContaining(unknownProcessId.toString());

        verify(repository, never()).save(any());
    }

    @Test
    void should_throwExceptionWhenViewingNotFound() {
        // Arrange
        BrokerageProcess process = BrokerageProcess.create(
                UUID.randomUUID(),
                new AskingPrice(new BigDecimal("350000"), "EUR"),
                new Commission(new BigDecimal("3.57"))
        );

        UUID processId = process.getId();
        UUID unknownViewingId = UUID.randomUUID();

        when(repository.findById(processId)).thenReturn(Optional.of(process));

        CompleteViewingCommand command = new CompleteViewingCommand(processId, unknownViewingId);

        // Act & Assert
        assertThatThrownBy(() -> useCase.complete(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(unknownViewingId.toString());

        verify(repository, never()).save(any());
    }
}
