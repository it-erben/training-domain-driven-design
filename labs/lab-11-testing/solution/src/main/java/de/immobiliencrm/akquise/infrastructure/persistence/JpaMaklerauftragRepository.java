package de.immobiliencrm.akquise.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Spring Data JPA repository for the Maklerauftrag JPA entity.
 */
public interface JpaMaklerauftragRepository extends JpaRepository<JpaMaklerauftrag, UUID> {
}
