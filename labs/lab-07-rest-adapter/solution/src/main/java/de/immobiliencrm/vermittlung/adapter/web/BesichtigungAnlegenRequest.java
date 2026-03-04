package de.immobiliencrm.vermittlung.adapter.web;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for creating a new viewing appointment via REST API.
 */
public record BesichtigungAnlegenRequest(
        @NotBlank String interessentName,
        @NotNull LocalDateTime zeitpunkt
) {}
