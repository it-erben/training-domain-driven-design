package de.realestate.property.infrastructure.persistence;

import de.realestate.property.domain.model.Property;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.SoftDelete;

import java.math.BigDecimal;

@Entity
@Table(name = "properties")
@SoftDelete
public class JpaProperty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String title;

    @Valid
    @Embedded
    private JpaPropertyAddress address;

    private BigDecimal livingArea;

    private BigDecimal purchasePrice;

    protected JpaProperty() {
    }

    public static JpaProperty fromModel(Property property) {
        JpaProperty jpa = new JpaProperty();
        jpa.id = property.getId();
        jpa.title = property.getTitle();
        jpa.address = JpaPropertyAddress.fromModel(property.getAddress());
        jpa.livingArea = property.getLivingArea();
        jpa.purchasePrice = property.getPurchasePrice();
        return jpa;
    }

    public Property toModel() {
        return Property.reconstitute(
                id,
                title,
                address != null ? address.toModel() : null,
                livingArea,
                purchasePrice
        );
    }
}
