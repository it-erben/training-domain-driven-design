package de.realestate.acquisition.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Spring Data JPA repository for JpaBrokerageContract entities.
 */
public interface JpaBrokerageContractRepository extends JpaRepository<JpaBrokerageContract, UUID> {
}
