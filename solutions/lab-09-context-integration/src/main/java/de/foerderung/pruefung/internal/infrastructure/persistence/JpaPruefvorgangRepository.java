package de.foerderung.pruefung.internal.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaPruefvorgangRepository extends JpaRepository<JpaPruefvorgang, UUID> {

    Optional<JpaPruefvorgang> findByAntragsmappeId(UUID antragsmappeId);

    boolean existsByAntragsmappeId(UUID antragsmappeId);
}
