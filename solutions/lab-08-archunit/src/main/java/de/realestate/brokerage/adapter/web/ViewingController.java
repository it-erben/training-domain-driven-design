package de.realestate.brokerage.adapter.web;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.realestate.brokerage.application.command.CompleteViewingCommand;
import de.realestate.brokerage.application.command.CreateViewingResult;
import de.realestate.brokerage.application.service.CompleteViewingUseCase;
import de.realestate.brokerage.application.service.CreateViewingUseCase;
import de.realestate.brokerage.application.service.ListViewingsUseCase;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/brokerage/processes/{processId}/viewings")
public class ViewingController {

    private final CreateViewingUseCase createViewingUseCase;
    private final CompleteViewingUseCase completeViewingUseCase;
    private final ListViewingsUseCase listViewingsUseCase;

    public ViewingController(CreateViewingUseCase createViewingUseCase,
                             CompleteViewingUseCase completeViewingUseCase,
                             ListViewingsUseCase listViewingsUseCase) {
        this.createViewingUseCase = createViewingUseCase;
        this.completeViewingUseCase = completeViewingUseCase;
        this.listViewingsUseCase = listViewingsUseCase;
    }

    @PostMapping
    public ResponseEntity<CreateViewingResponse> create(
            @PathVariable UUID processId,
            @Valid @RequestBody CreateViewingRequest request) {

        CreateViewingResult result = createViewingUseCase.create(request.toCommand(processId));

        CreateViewingResponse response = CreateViewingResponse.from(result);

        URI location = URI.create("/api/brokerage/processes/" + processId
                + "/viewings/" + result.viewingId());

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public List<ViewingResponse> list(@PathVariable UUID processId) {
        return listViewingsUseCase.list(processId).stream()
                .map(ViewingResponse::from)
                .toList();
    }

    @PatchMapping("/{viewingId}/complete")
    public ResponseEntity<Void> complete(
            @PathVariable UUID processId,
            @PathVariable UUID viewingId) {

        completeViewingUseCase.complete(new CompleteViewingCommand(processId, viewingId));

        return ResponseEntity.noContent().build();
    }
}
