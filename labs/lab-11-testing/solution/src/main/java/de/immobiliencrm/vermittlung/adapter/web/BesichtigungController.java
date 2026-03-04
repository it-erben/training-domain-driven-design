package de.immobiliencrm.vermittlung.adapter.web;

import de.immobiliencrm.vermittlung.application.command.BesichtigungAnlegenCommand;
import de.immobiliencrm.vermittlung.application.command.BesichtigungAnlegenResult;
import de.immobiliencrm.vermittlung.application.service.BesichtigungAnlegenUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller for managing viewings within a brokerage process.
 */
@RestController
@RequestMapping("/api/vermittlungsvorgaenge/{vorgangId}/besichtigungen")
public class BesichtigungController {

    private final BesichtigungAnlegenUseCase besichtigungAnlegenUseCase;

    public BesichtigungController(BesichtigungAnlegenUseCase besichtigungAnlegenUseCase) {
        this.besichtigungAnlegenUseCase = besichtigungAnlegenUseCase;
    }

    @PostMapping
    public ResponseEntity<BesichtigungAnlegenResponse> besichtigungAnlegen(
            @PathVariable UUID vorgangId,
            @Valid @RequestBody BesichtigungAnlegenRequest request) {
        BesichtigungAnlegenCommand command = new BesichtigungAnlegenCommand(
                vorgangId,
                request.interessentName(),
                request.zeitpunkt()
        );

        BesichtigungAnlegenResult result = besichtigungAnlegenUseCase.anlegen(command);

        BesichtigungAnlegenResponse response = new BesichtigungAnlegenResponse(
                result.besichtigungId(),
                result.vermittlungsvorgangId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
