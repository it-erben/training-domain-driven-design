package de.foerderung.antragstellung.internal.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaAntragsMappeRepository extends JpaRepository<JpaAntragsMappe, UUID> {
}
