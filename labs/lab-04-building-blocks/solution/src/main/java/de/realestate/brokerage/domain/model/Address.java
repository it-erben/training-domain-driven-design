package de.realestate.brokerage.domain.model;

import java.util.Objects;

/**
 * Value Object representing a postal address.
 */
public record Address(String street, String postalCode, String city) {

    public Address {
        Objects.requireNonNull(street, "Street must not be null");
        Objects.requireNonNull(postalCode, "Postal code must not be null");
        Objects.requireNonNull(city, "City must not be null");

        if (street.isBlank()) {
            throw new IllegalArgumentException("Street must not be blank");
        }
        if (postalCode.isBlank()) {
            throw new IllegalArgumentException("Postal code must not be blank");
        }
        if (city.isBlank()) {
            throw new IllegalArgumentException("City must not be blank");
        }
    }
}
