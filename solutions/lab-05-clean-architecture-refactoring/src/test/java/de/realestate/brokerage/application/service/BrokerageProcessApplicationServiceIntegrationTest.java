package de.realestate.brokerage.application.service;

import de.realestate.brokerage.domain.model.AskingPrice;
import de.realestate.brokerage.domain.model.Commission;
import de.realestate.brokerage.domain.port.BrokerageProcessRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class BrokerageProcessApplicationServiceIntegrationTest {

    @Autowired
    private BrokerageProcessApplicationService service;

    @Autowired
    private BrokerageProcessRepository repository;

    @Test
    void create_persistsBrokerageProcess() {
        var propertyId = UUID.randomUUID();
        var askingPrice = new AskingPrice(new BigDecimal("450000"), "EUR");
        var commission = new Commission(new BigDecimal("3.57"));

        var created = service.create(propertyId, askingPrice, commission);

        var reloaded = repository.findById(created.getId());

        assertThat(reloaded).isPresent();
        assertThat(reloaded.orElseThrow().getPropertyId()).isEqualTo(propertyId);
        assertThat(reloaded.orElseThrow().getAskingPrice()).isEqualTo(askingPrice);
        assertThat(reloaded.orElseThrow().getCommission()).isEqualTo(commission);
    }
}
