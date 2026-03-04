package de.immobiliencrm.vermittlung.infrastructure.persistence;

import de.immobiliencrm.vermittlung.domain.model.Adresse;
import de.immobiliencrm.vermittlung.domain.model.Preisvorstellung;
import de.immobiliencrm.vermittlung.domain.model.Provision;
import de.immobiliencrm.vermittlung.domain.model.Vermittlungsvorgang;
import de.immobiliencrm.vermittlung.domain.port.VermittlungsvorgangRepository;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementing the domain repository port using JPA persistence.
 */
@Component
public class VermittlungsvorgangRepositoryAdapter implements VermittlungsvorgangRepository {

    private final JpaVermittlungsvorgangRepository jpaRepository;

    public VermittlungsvorgangRepositoryAdapter(JpaVermittlungsvorgangRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Vermittlungsvorgang> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Vermittlungsvorgang> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public Vermittlungsvorgang save(Vermittlungsvorgang vermittlungsvorgang) {
        JpaVermittlungsvorgang entity = toJpa(vermittlungsvorgang);
        JpaVermittlungsvorgang saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    private Vermittlungsvorgang toDomain(JpaVermittlungsvorgang entity) {
        Adresse adresse = new Adresse(entity.getStrasse(), entity.getPlz(), entity.getOrt());
        Preisvorstellung preis = new Preisvorstellung(entity.getPreisBetrag(), entity.getPreisWaehrung());
        Provision provision = new Provision(entity.getProvisionProzentsatz());

        return Vermittlungsvorgang.erstellen(entity.getImmobilieId(), adresse, preis, provision);
    }

    private JpaVermittlungsvorgang toJpa(Vermittlungsvorgang vorgang) {
        return new JpaVermittlungsvorgang(
                vorgang.getId(),
                vorgang.getImmobilieId(),
                vorgang.getAdresse().strasse(),
                vorgang.getAdresse().plz(),
                vorgang.getAdresse().ort(),
                vorgang.getPreisvorstellung().betrag(),
                vorgang.getPreisvorstellung().waehrung(),
                vorgang.getProvision().prozentsatz(),
                vorgang.getStatus().name()
        );
    }
}
