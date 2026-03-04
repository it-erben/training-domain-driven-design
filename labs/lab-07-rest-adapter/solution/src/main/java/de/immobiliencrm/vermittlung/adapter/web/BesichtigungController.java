package de.immobiliencrm.vermittlung.adapter.web;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.immobiliencrm.vermittlung.application.command.BesichtigungAnlegenCommand;
import de.immobiliencrm.vermittlung.application.command.BesichtigungAnlegenResult;
import de.immobiliencrm.vermittlung.application.service.BesichtigungAnlegenUseCase;
import jakarta.validation.Valid;

/**
 * Inbound REST adapter for managing viewing appointments (Besichtigungen).
 * Translates HTTP requests into application commands and returns appropriate HTTP responses.
 */
@RestController
@RequestMapping("/api/vermittlungsvorgaenge/{vorgangId}/besichtigungen")
public class BesichtigungController {

    private final BesichtigungAnlegenUseCase besichtigungAnlegenUseCase;

    public BesichtigungController(BesichtigungAnlegenUseCase besichtigungAnlegenUseCase) {
        this.besichtigungAnlegenUseCase = besichtigungAnlegenUseCase;
    }

    @PostMapping
    public ResponseEntity<BesichtigungAnlegenResponse> anlegen(
            @PathVariable UUID vorgangId,
            @Valid @RequestBody BesichtigungAnlegenRequest request) {

        // Map request DTO to application command
        BesichtigungAnlegenCommand command = new BesichtigungAnlegenCommand(
                vorgangId,
                request.interessentName(),
                request.zeitpunkt());

        // Delegate to use case
        BesichtigungAnlegenResult result = besichtigungAnlegenUseCase.anlegen(command);

        // Map result to response DTO
        BesichtigungAnlegenResponse response = new BesichtigungAnlegenResponse(
                result.besichtigungId(),
                result.vermittlungsvorgangId());

        // Return 201 Created with Location header
        URI location = URI.create("/api/vermittlungsvorgaenge/" + vorgangId
                + "/besichtigungen/" + result.besichtigungId());

        return ResponseEntity.created(location).body(response);
    }
}
