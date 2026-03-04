package de.immobiliencrm.vermittlung.adapter.web;

import java.util.UUID;

/**
 * Response DTO after successfully scheduling a viewing.
 */
public record BesichtigungAnlegenResponse(
        UUID besichtigungId,
        UUID vermittlungsvorgangId
) {}
