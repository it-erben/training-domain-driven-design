package de.realestate.property.domain.model;

import java.util.Objects;

public record PropertyAddress(
    String street,
    String postalCode,
    String city
) {
    public PropertyAddress {
        Objects.requireNonNull(street, "Street must not be null");
        Objects.requireNonNull(postalCode, "Postal code must not be null");
        Objects.requireNonNull(city, "City must not be null");
    }
}
