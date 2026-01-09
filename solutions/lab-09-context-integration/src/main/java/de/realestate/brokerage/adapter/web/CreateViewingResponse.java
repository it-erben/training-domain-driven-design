package de.realestate.brokerage.adapter.web;

import java.util.UUID;

import de.realestate.brokerage.application.command.CreateViewingResult;

public record CreateViewingResponse(
        UUID viewingId,
        UUID processId
) {
    public static CreateViewingResponse from(CreateViewingResult result) {
        return new CreateViewingResponse(result.viewingId(), result.processId());
    }
}
