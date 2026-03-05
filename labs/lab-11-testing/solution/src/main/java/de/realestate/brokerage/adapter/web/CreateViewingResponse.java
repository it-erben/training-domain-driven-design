package de.realestate.brokerage.adapter.web;

import java.util.UUID;

/**
 * Response DTO after successfully scheduling a viewing.
 */
public record CreateViewingResponse(
        UUID viewingId,
        UUID processId
) {}
