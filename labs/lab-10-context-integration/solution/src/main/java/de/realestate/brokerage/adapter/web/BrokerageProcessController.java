package de.realestate.brokerage.adapter.web;

import de.realestate.brokerage.application.command.CreateBrokerageProcessCommand;
import de.realestate.brokerage.application.service.BrokerageProcessService;
import de.realestate.brokerage.domain.model.BrokerageProcess;

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
@RequestMapping("/api/brokerage-processes")
public class BrokerageProcessController {

    private final BrokerageProcessService service;

    public BrokerageProcessController(BrokerageProcessService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<BrokerageProcess> create(@RequestBody CreateBrokerageProcessCommand command) {
        BrokerageProcess process = service.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(process);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BrokerageProcess> findById(@PathVariable UUID id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<BrokerageProcess> findAll() {
        return service.findAll();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
