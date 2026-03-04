package de.immobiliencrm.vermittlung.adapter.web;

import java.util.UUID;

/**
 * Response DTO returned after successfully creating a new viewing appointment.
 */
public record BesichtigungAnlegenResponse(
        UUID besichtigungId,
        UUID vermittlungsvorgangId
) {}
