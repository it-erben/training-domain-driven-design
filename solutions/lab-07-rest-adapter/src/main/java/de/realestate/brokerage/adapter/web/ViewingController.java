package de.realestate.brokerage.adapter.web;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.realestate.brokerage.application.command.CreateViewingCommand;
import de.realestate.brokerage.application.command.CreateViewingResult;
import de.realestate.brokerage.application.service.CreateViewingUseCase;
import jakarta.validation.Valid;

/**
 * Inbound REST adapter for managing viewing appointments.
 * Translates HTTP requests into application commands and returns appropriate HTTP responses.
 */
@RestController
@RequestMapping("/api/brokerage/processes/{processId}/viewings")
public class ViewingController {

    private final CreateViewingUseCase createViewingUseCase;

    public ViewingController(CreateViewingUseCase createViewingUseCase) {
        this.createViewingUseCase = createViewingUseCase;
    }

    @PostMapping
    public ResponseEntity<CreateViewingResponse> create(
            @PathVariable UUID processId,
            @Valid @RequestBody CreateViewingRequest request) {

        // Map request DTO to application command
        CreateViewingCommand command = new CreateViewingCommand(
                processId,
                request.prospectName(),
                request.appointmentDate());

        // Delegate to use case
        CreateViewingResult result = createViewingUseCase.create(command);

        // Map result to response DTO
        CreateViewingResponse response = new CreateViewingResponse(
                result.viewingId(),
                result.processId());

        // Return 201 Created with Location header
        URI location = URI.create("/api/brokerage/processes/" + processId
                + "/viewings/" + result.viewingId());

        return ResponseEntity.created(location).body(response);
    }
}
