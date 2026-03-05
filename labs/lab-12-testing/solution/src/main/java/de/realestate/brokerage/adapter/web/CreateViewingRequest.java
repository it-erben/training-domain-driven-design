package de.realestate.brokerage.adapter.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Request DTO for scheduling a new viewing.
 */
public record CreateViewingRequest(
        @NotBlank(message = "Prospect name must not be blank")
        String prospectName,

        @NotNull(message = "Appointment date must not be null")
        LocalDateTime appointmentDate
) {}
