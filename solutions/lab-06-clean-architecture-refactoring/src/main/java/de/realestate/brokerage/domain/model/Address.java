package de.realestate.brokerage.domain.model;

import java.util.Objects;

/**
 * Value Object representing a postal address.
 */
public record Address(String street, String zipCode, String city) {

    public Address {
        Objects.requireNonNull(street, "Street must not be null");
        Objects.requireNonNull(zipCode, "Zip code must not be null");
        Objects.requireNonNull(city, "City must not be null");

        if (street.isBlank()) {
            throw new IllegalArgumentException("Street must not be empty");
        }
        if (zipCode.isBlank()) {
            throw new IllegalArgumentException("Zip code must not be empty");
        }
        if (city.isBlank()) {
            throw new IllegalArgumentException("City must not be empty");
        }
    }
}
