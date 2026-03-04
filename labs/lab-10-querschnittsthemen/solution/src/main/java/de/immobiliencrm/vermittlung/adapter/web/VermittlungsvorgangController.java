package de.immobiliencrm.vermittlung.adapter.web;

import de.immobiliencrm.vermittlung.application.command.ErstelleVermittlungsvorgangCommand;
import de.immobiliencrm.vermittlung.application.service.VermittlungsvorgangService;
import de.immobiliencrm.vermittlung.domain.model.Vermittlungsvorgang;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller exposing the brokerage process API.
 * Delegates to application service -- does not access domain model directly.
 */
@RestController
@RequestMapping("/api/vermittlungsvorgaenge")
public class VermittlungsvorgangController {

    private final VermittlungsvorgangService service;

    public VermittlungsvorgangController(VermittlungsvorgangService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Vermittlungsvorgang> erstellen(@RequestBody ErstelleVermittlungsvorgangCommand command) {
        Vermittlungsvorgang vorgang = service.erstellen(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(vorgang);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vermittlungsvorgang> findById(@PathVariable UUID id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<Vermittlungsvorgang> findAll() {
        return service.findAll();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> loeschen(@PathVariable UUID id) {
        service.loeschen(id);
        return ResponseEntity.noContent().build();
    }
}
