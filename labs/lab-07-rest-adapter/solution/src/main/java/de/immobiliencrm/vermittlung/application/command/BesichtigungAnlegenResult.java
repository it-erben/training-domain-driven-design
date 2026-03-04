package de.immobiliencrm.vermittlung.application.command;

import java.util.UUID;

/**
 * Result returned after successfully creating a new viewing appointment.
 */
public record BesichtigungAnlegenResult(
        UUID besichtigungId,
        UUID vermittlungsvorgangId
) {}
