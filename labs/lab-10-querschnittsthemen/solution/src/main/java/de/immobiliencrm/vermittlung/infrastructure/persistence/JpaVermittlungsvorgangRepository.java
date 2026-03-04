package de.immobiliencrm.vermittlung.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Spring Data JPA repository for JpaVermittlungsvorgang entities.
 */
public interface JpaVermittlungsvorgangRepository extends JpaRepository<JpaVermittlungsvorgang, UUID> {
}
