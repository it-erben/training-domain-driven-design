package de.realestate.brokerage.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for JpaBrokerageProcess entities.
 */
public interface JpaBrokerageProcessRepository extends JpaRepository<JpaBrokerageProcess, UUID> {

    Optional<JpaBrokerageProcess> findByPropertyId(UUID propertyId);
}
