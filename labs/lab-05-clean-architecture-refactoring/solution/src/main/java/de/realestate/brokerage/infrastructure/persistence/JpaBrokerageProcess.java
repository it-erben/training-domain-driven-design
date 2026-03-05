package de.realestate.brokerage.infrastructure.persistence;

import de.realestate.brokerage.domain.model.Address;
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
import java.util.stream.Collectors;

/**
 * JPA Entity representing a BrokerageProcess for persistence.
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
    private String zipCode;

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
    @CollectionTable(name = "viewings", joinColumns = @JoinColumn(name = "process_id"))
    private List<JpaViewing> viewings = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "offers", joinColumns = @JoinColumn(name = "process_id"))
    private List<JpaOffer> offers = new ArrayList<>();

    protected JpaBrokerageProcess() {
    }

    /**
     * Converts this JPA entity to the domain model.
     */
    public BrokerageProcess toModel() {
        return new BrokerageProcess(
                this.id,
                this.propertyId,
                new Address(this.street, this.zipCode, this.city),
                new AskingPrice(this.priceAmount, this.priceCurrency),
                new Commission(this.commissionPercentage),
                this.status,
                this.viewings.stream()
                        .map(JpaViewing::toModel)
                        .collect(Collectors.toList()),
                this.offers.stream()
                        .map(JpaOffer::toModel)
                        .collect(Collectors.toList())
        );
    }

    /**
     * Creates a JPA entity from the domain model.
     */
    public static JpaBrokerageProcess fromModel(BrokerageProcess process) {
        JpaBrokerageProcess jpa = new JpaBrokerageProcess();
        jpa.id = process.getId();
        jpa.propertyId = process.getPropertyId();
        jpa.street = process.getAddress().street();
        jpa.zipCode = process.getAddress().zipCode();
        jpa.city = process.getAddress().city();
        jpa.priceAmount = process.getAskingPrice().amount();
        jpa.priceCurrency = process.getAskingPrice().currency();
        jpa.commissionPercentage = process.getCommission().percentage();
        jpa.status = process.getStatus();
        jpa.viewings = process.getViewings().stream()
                .map(JpaViewing::fromModel)
                .collect(Collectors.toList());
        jpa.offers = process.getOffers().stream()
                .map(JpaOffer::fromModel)
                .collect(Collectors.toList());
        return jpa;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(UUID propertyId) {
        this.propertyId = propertyId;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public BigDecimal getPriceAmount() {
        return priceAmount;
    }

    public void setPriceAmount(BigDecimal priceAmount) {
        this.priceAmount = priceAmount;
    }

    public String getPriceCurrency() {
        return priceCurrency;
    }

    public void setPriceCurrency(String priceCurrency) {
        this.priceCurrency = priceCurrency;
    }

    public BigDecimal getCommissionPercentage() {
        return commissionPercentage;
    }

    public void setCommissionPercentage(BigDecimal commissionPercentage) {
        this.commissionPercentage = commissionPercentage;
    }

    public ProcessStatus getStatus() {
        return status;
    }

    public void setStatus(ProcessStatus status) {
        this.status = status;
    }

    public List<JpaViewing> getViewings() {
        return viewings;
    }

    public void setViewings(List<JpaViewing> viewings) {
        this.viewings = viewings;
    }

    public List<JpaOffer> getOffers() {
        return offers;
    }

    public void setOffers(List<JpaOffer> offers) {
        this.offers = offers;
    }
}
