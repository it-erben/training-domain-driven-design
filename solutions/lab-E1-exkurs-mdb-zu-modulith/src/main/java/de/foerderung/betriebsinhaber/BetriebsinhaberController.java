package de.foerderung.betriebsinhaber;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/betriebsinhaber")
public class BetriebsinhaberController {

    private final BetriebsinhaberService service;

    public BetriebsinhaberController(BetriebsinhaberService service) {
        this.service = service;
    }

    @GetMapping
    public List<Betriebsinhaber> findAll() { return service.findAll(); }

    @GetMapping("/{id}")
    public Betriebsinhaber findById(@PathVariable Long id) {
        return service.findById(id).orElseThrow(() -> new BetriebsinhaberNotFoundException(id));
    }

    @PostMapping
    public ResponseEntity<Betriebsinhaber> create(@Valid @RequestBody Betriebsinhaber b) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(b));
    }

    @PutMapping("/{id}")
    public Betriebsinhaber update(@PathVariable Long id, @Valid @RequestBody Betriebsinhaber b) {
        return service.update(id, b).orElseThrow(() -> new BetriebsinhaberNotFoundException(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!service.delete(id)) throw new BetriebsinhaberNotFoundException(id);
    }
}
