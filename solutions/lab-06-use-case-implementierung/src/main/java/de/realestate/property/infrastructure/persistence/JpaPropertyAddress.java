package de.realestate.property.infrastructure.persistence;

import de.realestate.property.domain.model.PropertyAddress;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;

@Embeddable
public record JpaPropertyAddress(
    @NotBlank String street,
    @NotBlank String postalCode,
    @NotBlank String city
) {

    public static JpaPropertyAddress fromModel(PropertyAddress address) {
        if (address == null) {
            return null;
        }
        return new JpaPropertyAddress(address.street(), address.postalCode(), address.city());
    }

    public PropertyAddress toModel() {
        return new PropertyAddress(street, postalCode, city);
    }
}
