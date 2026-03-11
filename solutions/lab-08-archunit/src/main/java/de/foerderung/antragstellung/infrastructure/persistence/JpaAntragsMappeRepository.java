package de.foerderung.antragstellung.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaAntragsMappeRepository extends JpaRepository<JpaAntragsMappe, UUID> {
}
