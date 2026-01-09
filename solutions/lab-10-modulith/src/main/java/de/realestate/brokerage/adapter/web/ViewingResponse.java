package de.realestate.brokerage.adapter.web;

import java.time.LocalDateTime;
import java.util.UUID;

import de.realestate.brokerage.domain.model.Viewing;

public record ViewingResponse(
        UUID viewingId,
        String prospectName,
        LocalDateTime appointmentDate,
        boolean completed
) {
    public static ViewingResponse from(Viewing viewing) {
        return new ViewingResponse(
                viewing.getId(),
                viewing.getProspectName(),
                viewing.getTimestamp(),
                viewing.isCompleted());
    }
}
