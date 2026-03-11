package de.foerderung.antragstellung.adapter.web;

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

import de.foerderung.antragstellung.application.command.FlurstueckHinzufuegenResult;
import de.foerderung.antragstellung.application.command.FlurstueckPruefenCommand;
import de.foerderung.antragstellung.application.service.FlurstueckHinzufuegenService;
import de.foerderung.antragstellung.application.service.FlurstueckPruefenService;
import de.foerderung.antragstellung.application.service.FlurstueckeAbfragenUseCase;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/antragsmappen/{antragsmappeId}/flurstuecke")
public class FlurstueckController {

    private final FlurstueckHinzufuegenService hinzufuegenService;
    private final FlurstueckPruefenService pruefenService;
    private final FlurstueckeAbfragenUseCase abfragenUseCase;

    public FlurstueckController(FlurstueckHinzufuegenService hinzufuegenService,
                                FlurstueckPruefenService pruefenService,
                                FlurstueckeAbfragenUseCase abfragenUseCase) {
        this.hinzufuegenService = hinzufuegenService;
        this.pruefenService = pruefenService;
        this.abfragenUseCase = abfragenUseCase;
    }

    @PostMapping
    public ResponseEntity<FlurstueckHinzufuegenResponse> hinzufuegen(
            @PathVariable UUID antragsmappeId,
            @Valid @RequestBody FlurstueckHinzufuegenRequest request) {

        FlurstueckHinzufuegenResult result = hinzufuegenService.hinzufuegen(
                request.toCommand(antragsmappeId));

        FlurstueckHinzufuegenResponse response = FlurstueckHinzufuegenResponse.from(result);

        URI location = URI.create("/api/antragsmappen/" + antragsmappeId
                + "/flurstuecke/" + result.flurstueckId());

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public List<FlurstueckResponse> auflisten(@PathVariable UUID antragsmappeId) {
        return abfragenUseCase.abfragen(antragsmappeId).stream()
                .map(FlurstueckResponse::from)
                .toList();
    }

    @PatchMapping("/{flurstueckId}/pruefen")
    public ResponseEntity<Void> pruefen(
            @PathVariable UUID antragsmappeId,
            @PathVariable UUID flurstueckId) {

        pruefenService.pruefen(FlurstueckPruefenCommand.of(antragsmappeId, flurstueckId));

        return ResponseEntity.noContent().build();
    }
}
