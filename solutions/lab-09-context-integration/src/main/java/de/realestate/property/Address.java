package de.realestate.property;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;

@Embeddable
public record Address(
    @NotBlank String street,
    @NotBlank String postalCode,
    @NotBlank String city
) {}
