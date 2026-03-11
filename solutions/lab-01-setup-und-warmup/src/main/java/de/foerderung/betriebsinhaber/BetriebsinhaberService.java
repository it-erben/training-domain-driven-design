package de.foerderung.betriebsinhaber;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BetriebsinhaberService {

    private final BetriebsinhaberRepository repository;

    public BetriebsinhaberService(BetriebsinhaberRepository repository) {
        this.repository = repository;
    }

    public List<Betriebsinhaber> findAll() {
        return repository.findAll();
    }

    public Optional<Betriebsinhaber> findById(Long id) {
        return repository.findById(id);
    }

    public Betriebsinhaber save(Betriebsinhaber betriebsinhaber) {
        betriebsinhaber.setStatus(BetriebsinhaberStatus.ENTWURF);
        return repository.save(betriebsinhaber);
    }

    public Optional<Betriebsinhaber> update(Long id, Betriebsinhaber betriebsinhaber) {
        return repository.findById(id)
                .map(existing -> {
                    // Betriebsflaeche-Schutz: Bei Reduktion > 20% wird Status auf ENTWURF zurueckgesetzt
                    if (existing.getStatus() == BetriebsinhaberStatus.AKTIV && betriebsinhaber.getBetriebsflaeche() != null) {
                        BigDecimal oldFlaeche = existing.getBetriebsflaeche();
                        BigDecimal newFlaeche = betriebsinhaber.getBetriebsflaeche();
                        if (oldFlaeche != null) {
                            BigDecimal threshold = oldFlaeche.multiply(new BigDecimal("0.80"));
                            if (newFlaeche.compareTo(threshold) < 0) {
                                existing.setStatus(BetriebsinhaberStatus.ENTWURF);
                            }
                        }
                    }

                    existing.setName(betriebsinhaber.getName());
                    existing.setBetriebsnummer(betriebsinhaber.getBetriebsnummer());
                    existing.setAdresse(betriebsinhaber.getAdresse());
                    existing.setBetriebsflaeche(betriebsinhaber.getBetriebsflaeche());
                    existing.setFoerdersumme(betriebsinhaber.getFoerdersumme());
                    return repository.save(existing);
                });
    }

    public boolean delete(Long id) {
        return repository.findById(id)
                .map(betriebsinhaber -> {
                    if (betriebsinhaber.getStatus() == BetriebsinhaberStatus.AKTIV) {
                        throw new BetriebsinhaberBusinessRuleException(
                                "Ein aktiver Betriebsinhaber kann nicht geloescht werden. Bitte zuerst stilllegen.");
                    }
                    repository.delete(betriebsinhaber);
                    return true;
                }).orElse(false);
    }

    public Betriebsinhaber freigeben(Long id) {
        Betriebsinhaber betriebsinhaber = repository.findById(id)
                .orElseThrow(() -> new BetriebsinhaberNotFoundException(id));

        if (betriebsinhaber.getName() == null || betriebsinhaber.getName().isBlank()) {
            throw new BetriebsinhaberBusinessRuleException("Name darf fuer die Freigabe nicht leer sein.");
        }
        if (betriebsinhaber.getAdresse() == null ||
                betriebsinhaber.getAdresse().strasse() == null || betriebsinhaber.getAdresse().strasse().isBlank() ||
                betriebsinhaber.getAdresse().plz() == null || betriebsinhaber.getAdresse().plz().isBlank() ||
                betriebsinhaber.getAdresse().ort() == null || betriebsinhaber.getAdresse().ort().isBlank()) {
            throw new BetriebsinhaberBusinessRuleException("Adresse muss vollstaendig sein fuer die Freigabe.");
        }

        betriebsinhaber.setStatus(BetriebsinhaberStatus.AKTIV);
        return repository.save(betriebsinhaber);
    }

    public Betriebsinhaber stilllegen(Long id) {
        Betriebsinhaber betriebsinhaber = repository.findById(id)
                .orElseThrow(() -> new BetriebsinhaberNotFoundException(id));
        betriebsinhaber.setStatus(BetriebsinhaberStatus.STILLGELEGT);
        return repository.save(betriebsinhaber);
    }
}
