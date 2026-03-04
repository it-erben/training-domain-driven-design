package de.immobiliencrm.vermittlung.adapter.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Request DTO for scheduling a new viewing.
 */
public record BesichtigungAnlegenRequest(
        @NotBlank(message = "Interessent-Name darf nicht leer sein")
        String interessentName,

        @NotNull(message = "Zeitpunkt darf nicht null sein")
        LocalDateTime zeitpunkt
) {}
