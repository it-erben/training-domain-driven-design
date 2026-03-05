package de.realestate.brokerage.infrastructure.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * JPA entity mapped to the database table for BrokerageProcess.
 */
@Entity
@Table(name = "brokerage_process")
public class JpaBrokerageProcess {

    @Id
    private UUID id;

    private UUID propertyId;

    private String street;
    private String zipCode;
    private String city;

    private BigDecimal priceAmount;
    private String priceCurrency;

    private BigDecimal commissionPercentage;

    private String status;

    // Default constructor required by JPA
    protected JpaBrokerageProcess() {
    }

    public JpaBrokerageProcess(UUID id, UUID propertyId, String street, String zipCode, String city,
                                BigDecimal priceAmount, String priceCurrency,
                                BigDecimal commissionPercentage, String status) {
        this.id = id;
        this.propertyId = propertyId;
        this.street = street;
        this.zipCode = zipCode;
        this.city = city;
        this.priceAmount = priceAmount;
        this.priceCurrency = priceCurrency;
        this.commissionPercentage = commissionPercentage;
        this.status = status;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
