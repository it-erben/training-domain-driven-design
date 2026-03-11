package de.foerderung.antragstellung.internal.adapter.web;

import de.foerderung.antragstellung.internal.application.AntragEinreichenCommand;
import de.foerderung.antragstellung.internal.application.AntragEinreichenService;
import de.foerderung.antragstellung.internal.domain.model.AntragsMappe;
import de.foerderung.antragstellung.internal.domain.model.AntragsmappeNichtGefundenException;
import de.foerderung.antragstellung.internal.domain.port.AntragsMappeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/antragstellung/antraege")
public class AntragsMappeController {

    private final AntragEinreichenService einreichenService;
    private final AntragsMappeRepository repository;

    public AntragsMappeController(AntragEinreichenService einreichenService,
                                   AntragsMappeRepository repository) {
        this.einreichenService = einreichenService;
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<AntragsMappeResponse> create() {
        UUID id = UUID.randomUUID();
        var mappe = einreichenService.create(id, "DE-ELER-2026-" + id.toString().substring(0, 4));
        return ResponseEntity.status(HttpStatus.CREATED).body(AntragsMappeResponse.from(mappe));
    }

    @PostMapping("/{id}/einreichen")
    public ResponseEntity<Void> einreichen(@PathVariable UUID id) {
        einreichenService.execute(new AntragEinreichenCommand(id));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public AntragsMappeResponse findById(@PathVariable UUID id) {
        return repository.findById(id)
                .map(AntragsMappeResponse::from)
                .orElseThrow(() -> new AntragsmappeNichtGefundenException(id));
    }

    record AntragsMappeResponse(UUID id, String registrierungsNummer, String status) {
        static AntragsMappeResponse from(AntragsMappe mappe) {
            return new AntragsMappeResponse(
                    mappe.getId(),
                    mappe.getRegistrierungsNummer().wert(),
                    mappe.getStatus().name());
        }
    }
}
