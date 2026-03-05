package de.realestate.brokerage.infrastructure.persistence;

import de.realestate.brokerage.domain.model.Address;
import de.realestate.brokerage.domain.model.AskingPrice;
import de.realestate.brokerage.domain.model.Commission;
import de.realestate.brokerage.domain.model.BrokerageProcess;
import de.realestate.brokerage.domain.port.BrokerageProcessRepository;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementing the domain repository port using JPA persistence.
 */
@Component
public class BrokerageProcessRepositoryAdapter implements BrokerageProcessRepository {

    private final JpaBrokerageProcessRepository jpaRepository;

    public BrokerageProcessRepositoryAdapter(JpaBrokerageProcessRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<BrokerageProcess> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<BrokerageProcess> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public BrokerageProcess save(BrokerageProcess brokerageProcess) {
        JpaBrokerageProcess entity = toJpa(brokerageProcess);
        JpaBrokerageProcess saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    private BrokerageProcess toDomain(JpaBrokerageProcess entity) {
        Address address = new Address(entity.getStreet(), entity.getZipCode(), entity.getCity());
        AskingPrice askingPrice = new AskingPrice(entity.getPriceAmount(), entity.getPriceCurrency());
        Commission commission = new Commission(entity.getCommissionPercentage());

        return BrokerageProcess.create(entity.getPropertyId(), address, askingPrice, commission);
    }

    private JpaBrokerageProcess toJpa(BrokerageProcess process) {
        return new JpaBrokerageProcess(
                process.getId(),
                process.getPropertyId(),
                process.getAddress().street(),
                process.getAddress().zipCode(),
                process.getAddress().city(),
                process.getAskingPrice().amount(),
                process.getAskingPrice().currency(),
                process.getCommission().percentage(),
                process.getStatus().name()
        );
    }
}
