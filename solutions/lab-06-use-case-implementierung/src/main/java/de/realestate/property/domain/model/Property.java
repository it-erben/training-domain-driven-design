package de.realestate.property.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public class Property {

    private final Long id;
    private String title;
    private PropertyAddress address;
    private BigDecimal livingArea;
    private BigDecimal purchasePrice;

    private Property(Long id, String title, PropertyAddress address,
                     BigDecimal livingArea, BigDecimal purchasePrice) {
        this.id = id;
        this.title = Objects.requireNonNull(title, "Title must not be null");
        this.address = address;
        this.livingArea = livingArea;
        this.purchasePrice = purchasePrice;
    }

    public static Property create(String title, PropertyAddress address,
                                  BigDecimal livingArea, BigDecimal purchasePrice) {
        return new Property(null, title, address, livingArea, purchasePrice);
    }

    public static Property reconstitute(Long id, String title, PropertyAddress address,
                                        BigDecimal livingArea, BigDecimal purchasePrice) {
        return new Property(id, title, address, livingArea, purchasePrice);
    }

    public void update(String title, PropertyAddress address,
                       BigDecimal livingArea, BigDecimal purchasePrice) {
        this.title = Objects.requireNonNull(title, "Title must not be null");
        this.address = address;
        this.livingArea = livingArea;
        this.purchasePrice = purchasePrice;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public PropertyAddress getAddress() {
        return address;
    }

    public BigDecimal getLivingArea() {
        return livingArea;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }
}
