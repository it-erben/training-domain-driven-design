package de.realestate.property.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPropertyRepository extends JpaRepository<JpaProperty, Long> {
}
