package de.realestate.brokerage.adapter.web;

import java.util.UUID;

/**
 * Response DTO returned after successfully creating a new viewing appointment.
 */
public record CreateViewingResponse(
        UUID viewingId,
        UUID processId
) {}
