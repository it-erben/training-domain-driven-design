package de.realestate.brokerage.adapter.web;

import java.time.LocalDateTime;
import java.util.UUID;

import de.realestate.brokerage.application.command.CreateViewingCommand;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateViewingRequest(
        @NotBlank String prospectName,
        @NotNull @Future LocalDateTime appointmentDate
) {
    public CreateViewingCommand toCommand(UUID processId) {
        return new CreateViewingCommand(processId, prospectName, appointmentDate);
    }
}
