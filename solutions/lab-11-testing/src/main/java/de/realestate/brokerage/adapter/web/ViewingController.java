package de.realestate.brokerage.adapter.web;

import de.realestate.brokerage.application.command.CreateViewingCommand;
import de.realestate.brokerage.application.command.CreateViewingResult;
import de.realestate.brokerage.application.service.CreateViewingUseCase;
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
@RequestMapping("/api/brokerage-processes/{processId}/viewings")
public class ViewingController {

    private final CreateViewingUseCase createViewingUseCase;

    public ViewingController(CreateViewingUseCase createViewingUseCase) {
        this.createViewingUseCase = createViewingUseCase;
    }

    @PostMapping
    public ResponseEntity<CreateViewingResponse> createViewing(
            @PathVariable UUID processId,
            @Valid @RequestBody CreateViewingRequest request) {
        CreateViewingCommand command = new CreateViewingCommand(
                processId,
                request.prospectName(),
                request.appointmentDate()
        );

        CreateViewingResult result = createViewingUseCase.createViewing(command);

        CreateViewingResponse response = new CreateViewingResponse(
                result.viewingId(),
                result.processId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
