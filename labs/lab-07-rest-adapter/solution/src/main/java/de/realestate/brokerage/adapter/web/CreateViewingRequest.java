package de.realestate.brokerage.adapter.web;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for creating a new viewing appointment via REST API.
 */
public record CreateViewingRequest(
        @NotBlank String prospectName,
        @NotNull LocalDateTime appointmentDate
) {}
