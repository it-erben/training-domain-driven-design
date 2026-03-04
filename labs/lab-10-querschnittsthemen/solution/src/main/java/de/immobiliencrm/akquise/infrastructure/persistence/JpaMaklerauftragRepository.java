package de.immobiliencrm.akquise.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Spring Data JPA repository for JpaMaklerauftrag entities.
 */
public interface JpaMaklerauftragRepository extends JpaRepository<JpaMaklerauftrag, UUID> {
}
