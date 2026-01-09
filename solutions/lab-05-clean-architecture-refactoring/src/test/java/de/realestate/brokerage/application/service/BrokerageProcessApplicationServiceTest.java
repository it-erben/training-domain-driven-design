package de.realestate.brokerage.application.service;

import de.realestate.brokerage.domain.model.AskingPrice;
import de.realestate.brokerage.domain.model.BrokerageProcess;
import de.realestate.brokerage.domain.model.Commission;
import de.realestate.brokerage.domain.model.ProcessStatus;
import de.realestate.brokerage.domain.port.BrokerageProcessRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BrokerageProcessApplicationServiceTest {

    @Mock
    private BrokerageProcessRepository repository;

    @InjectMocks
    private BrokerageProcessApplicationService service;

    private final AskingPrice askingPrice = new AskingPrice(new BigDecimal("450000"), "EUR");
    private final Commission commission = new Commission(new BigDecimal("3.57"));

    @Test
    void create_delegatesToFactoryMethodAndSaves() {
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.create(UUID.randomUUID(), askingPrice, commission);

        assertThat(result.getStatus()).isEqualTo(ProcessStatus.NEW);
        assertThat(result.getAskingPrice()).isEqualTo(askingPrice);
        verify(repository).save(any(BrokerageProcess.class));
    }

    @Test
    void findById_existingProcess_returnsProcess() {
        var id = UUID.randomUUID();
        var process = BrokerageProcess.create(UUID.randomUUID(), askingPrice, commission);
        when(repository.findById(id)).thenReturn(Optional.of(process));

        var result = service.findById(id);

        assertThat(result).isPresent();
    }

    @Test
    void findById_nonExistent_returnsEmpty() {
        when(repository.findById(any())).thenReturn(Optional.empty());

        var result = service.findById(UUID.randomUUID());

        assertThat(result).isEmpty();
    }

}
