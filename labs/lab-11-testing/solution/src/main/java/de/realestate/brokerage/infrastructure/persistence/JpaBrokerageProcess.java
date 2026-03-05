package de.realestate.brokerage.infrastructure.persistence;

import de.realestate.brokerage.domain.model.Address;
import de.realestate.brokerage.domain.model.Offer;
import de.realestate.brokerage.domain.model.Viewing;
import de.realestate.brokerage.domain.model.AskingPrice;
import de.realestate.brokerage.domain.model.Commission;
import de.realestate.brokerage.domain.model.BrokerageProcess;
import de.realestate.brokerage.domain.model.ProcessStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA entity for persisting the BrokerageProcess aggregate.
 */
@Entity
@Table(name = "brokerage_process")
public class JpaBrokerageProcess {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID propertyId;

    @Column(nullable = false)
    private String street;

    @Column(nullable = false)
    private String postalCode;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private BigDecimal priceAmount;

    @Column(nullable = false)
    private String priceCurrency;

    @Column(nullable = false)
    private BigDecimal commissionPercentage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessStatus status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "viewing", joinColumns = @JoinColumn(name = "process_id"))
    private List<JpaViewing> viewings = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "offer", joinColumns = @JoinColumn(name = "process_id"))
    private List<JpaOffer> offers = new ArrayList<>();

    protected JpaBrokerageProcess() {
        // Required by JPA
    }

    /**
     * Converts a domain BrokerageProcess to its JPA representation.
     */
    public static JpaBrokerageProcess fromModel(BrokerageProcess process) {
        JpaBrokerageProcess jpa = new JpaBrokerageProcess();
        jpa.id = process.getId();
        jpa.propertyId = process.getPropertyId();
        jpa.street = process.getAddress().street();
        jpa.postalCode = process.getAddress().postalCode();
        jpa.city = process.getAddress().city();
        jpa.priceAmount = process.getAskingPrice().amount();
        jpa.priceCurrency = process.getAskingPrice().currency();
        jpa.commissionPercentage = process.getCommission().percentage();
        jpa.status = process.getStatus();
        jpa.viewings = process.getViewings().stream()
                .map(JpaViewing::fromModel)
                .toList();
        jpa.offers = process.getOffers().stream()
                .map(JpaOffer::fromModel)
                .toList();
        return jpa;
    }

    /**
     * Converts this JPA representation back to a domain BrokerageProcess.
     */
    public BrokerageProcess toModel() {
        List<Viewing> domainViewings = viewings.stream()
                .map(JpaViewing::toModel)
                .toList();
        List<Offer> domainOffers = offers.stream()
                .map(JpaOffer::toModel)
                .toList();

        return BrokerageProcess.reconstitute(
                id,
                propertyId,
                new Address(street, postalCode, city),
                new AskingPrice(priceAmount, priceCurrency),
                new Commission(commissionPercentage),
                status,
                domainViewings,
                domainOffers
        );
    }
}
