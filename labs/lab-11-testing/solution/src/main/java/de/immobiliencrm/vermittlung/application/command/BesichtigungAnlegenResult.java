package de.immobiliencrm.vermittlung.application.command;

import java.util.UUID;

/**
 * Result returned after successfully scheduling a viewing.
 */
public record BesichtigungAnlegenResult(
        UUID besichtigungId,
        UUID vermittlungsvorgangId
) {}
