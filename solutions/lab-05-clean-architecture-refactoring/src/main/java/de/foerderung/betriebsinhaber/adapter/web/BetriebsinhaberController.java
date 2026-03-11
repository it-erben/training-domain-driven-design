package de.foerderung.betriebsinhaber.adapter.web;

import de.foerderung.betriebsinhaber.application.service.BetriebsinhaberService;
import de.foerderung.betriebsinhaber.domain.model.Betriebsadresse;
import de.foerderung.betriebsinhaber.domain.model.Betriebsinhaber;
import de.foerderung.betriebsinhaber.domain.model.BetriebsinhaberNichtGefundenException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/betriebsinhaber")
public class BetriebsinhaberController {

    private final BetriebsinhaberService service;

    public BetriebsinhaberController(BetriebsinhaberService service) {
        this.service = service;
    }

    @GetMapping
    public List<BetriebsinhaberResponse> findAll() {
        return service.findAll().stream()
                .map(BetriebsinhaberResponse::fromModel)
                .toList();
    }

    @GetMapping("/{id}")
    public BetriebsinhaberResponse findById(@PathVariable Long id) {
        return service.findById(id)
                .map(BetriebsinhaberResponse::fromModel)
                .orElseThrow(() -> new BetriebsinhaberNichtGefundenException(id));
    }

    @PostMapping
    public ResponseEntity<BetriebsinhaberResponse> create(
            @Valid @RequestBody BetriebsinhaberRequest request) {
        Betriebsinhaber betriebsinhaber = request.toModel();
        Betriebsinhaber saved = service.save(betriebsinhaber);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BetriebsinhaberResponse.fromModel(saved));
    }

    @PutMapping("/{id}")
    public BetriebsinhaberResponse update(@PathVariable Long id,
                                           @Valid @RequestBody BetriebsinhaberRequest request) {
        return service.update(id, request.toModel())
                .map(BetriebsinhaberResponse::fromModel)
                .orElseThrow(() -> new BetriebsinhaberNichtGefundenException(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!service.delete(id)) {
            throw new BetriebsinhaberNichtGefundenException(id);
        }
    }

    record BetriebsinhaberRequest(
            @NotBlank String name,
            @Valid AdresseRequest adresse,
            BigDecimal betriebsflaeche,
            BigDecimal foerdersumme
    ) {
        Betriebsinhaber toModel() {
            Betriebsadresse addr = adresse != null
                    ? new Betriebsadresse(adresse.strasse(), adresse.plz(), adresse.ort())
                    : null;
            return Betriebsinhaber.erstellen(name, addr, betriebsflaeche, foerdersumme);
        }
    }

    record AdresseRequest(
            @NotBlank String strasse,
            @NotBlank String plz,
            @NotBlank String ort
    ) {}

    record BetriebsinhaberResponse(
            Long id,
            String name,
            AdresseResponse adresse,
            BigDecimal betriebsflaeche,
            BigDecimal foerdersumme
    ) {
        static BetriebsinhaberResponse fromModel(Betriebsinhaber betriebsinhaber) {
            AdresseResponse addr = null;
            if (betriebsinhaber.getAdresse() != null) {
                addr = new AdresseResponse(
                        betriebsinhaber.getAdresse().strasse(),
                        betriebsinhaber.getAdresse().plz(),
                        betriebsinhaber.getAdresse().ort()
                );
            }
            return new BetriebsinhaberResponse(
                    betriebsinhaber.getId(),
                    betriebsinhaber.getName(),
                    addr,
                    betriebsinhaber.getBetriebsflaeche(),
                    betriebsinhaber.getFoerdersumme()
            );
        }
    }

    record AdresseResponse(String strasse, String plz, String ort) {}
}
