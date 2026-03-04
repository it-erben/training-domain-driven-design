package de.immobiliencrm.immobilie;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/immobilien")
public class ImmobilieController {

    private final ImmobilieService service;

    public ImmobilieController(ImmobilieService service) {
        this.service = service;
    }

    @GetMapping
    public List<Immobilie> alleAuflisten() {
        return service.findeAlle();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Immobilie> nachIdAbfragen(@PathVariable Long id) {
        return service.findePerId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Immobilie> erstellen(@Valid @RequestBody Immobilie immobilie) {
        Immobilie gespeichert = service.speichern(immobilie);
        return ResponseEntity.status(HttpStatus.CREATED).body(gespeichert);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Immobilie> aktualisieren(@PathVariable Long id,
                                                   @Valid @RequestBody Immobilie immobilie) {
        return service.aktualisieren(id, immobilie)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> loeschen(@PathVariable Long id) {
        if (service.loeschen(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

}
