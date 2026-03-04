package de.immobiliencrm.immobilie;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ImmobilieService {

    private final ImmobilieRepository repository;

    public ImmobilieService(ImmobilieRepository repository) {
        this.repository = repository;
    }

    public List<Immobilie> findeAlle() {
        return repository.findAll();
    }

    public Optional<Immobilie> findePerId(Long id) {
        return repository.findById(id);
    }

    public Immobilie speichern(Immobilie immobilie) {
        return repository.save(immobilie);
    }

    public Optional<Immobilie> aktualisieren(Long id, Immobilie immobilie) {
        return repository.findById(id)
                .map(bestehende -> {
                    bestehende.setBezeichnung(immobilie.getBezeichnung());
                    bestehende.setStrasse(immobilie.getStrasse());
                    bestehende.setPlz(immobilie.getPlz());
                    bestehende.setOrt(immobilie.getOrt());
                    bestehende.setWohnflaeche(immobilie.getWohnflaeche());
                    bestehende.setKaufpreis(immobilie.getKaufpreis());
                    return repository.save(bestehende);
                });
    }

    public boolean loeschen(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

}
