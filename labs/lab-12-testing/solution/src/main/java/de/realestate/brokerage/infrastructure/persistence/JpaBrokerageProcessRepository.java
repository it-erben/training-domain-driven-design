package de.realestate.brokerage.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Spring Data JPA repository for the BrokerageProcess JPA entity.
 */
public interface JpaBrokerageProcessRepository extends JpaRepository<JpaBrokerageProcess, UUID> {
}
