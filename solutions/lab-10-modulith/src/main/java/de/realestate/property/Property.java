package de.realestate.property;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.SoftDelete;

import java.math.BigDecimal;

@Entity
@Table(name = "properties")
@SoftDelete
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String title;

    @Valid
    @Embedded
    private Address address;

    private BigDecimal livingArea;

    private BigDecimal purchasePrice;

    public Property() {
    }

    public Property(String title, Address address,
                    BigDecimal livingArea, BigDecimal purchasePrice) {
        this.title = title;
        this.address = address;
        this.livingArea = livingArea;
        this.purchasePrice = purchasePrice;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public BigDecimal getLivingArea() {
        return livingArea;
    }

    public void setLivingArea(BigDecimal livingArea) {
        this.livingArea = livingArea;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

}
