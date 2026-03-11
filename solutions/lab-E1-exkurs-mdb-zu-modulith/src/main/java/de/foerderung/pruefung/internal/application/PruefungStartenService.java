package de.foerderung.pruefung.internal.application;

import de.foerderung.pruefung.internal.domain.Pruefvorgang;
import de.foerderung.pruefung.internal.domain.PruefvorgangId;
import de.foerderung.pruefung.internal.domain.port.PruefvorgangRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PruefungStartenService {

    private final PruefvorgangRepository repository;

    public PruefungStartenService(PruefvorgangRepository repository) {
        this.repository = repository;
    }

    public void start(PruefungStartenCommand cmd) {
        if (repository.existsByAntragsReferenz(cmd.antragsReferenz())) {
            return;
        }

        var pruefvorgang = Pruefvorgang.starten(
                PruefvorgangId.generate(),
                cmd.antragsReferenz(),
                cmd.registrierungsNummer(),
                cmd.eingereichtAm());
        repository.save(pruefvorgang);
    }
}
