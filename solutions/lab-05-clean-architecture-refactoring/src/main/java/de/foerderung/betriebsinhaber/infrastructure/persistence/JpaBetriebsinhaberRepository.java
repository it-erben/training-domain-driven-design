package de.foerderung.betriebsinhaber.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaBetriebsinhaberRepository extends JpaRepository<JpaBetriebsinhaber, Long> {
}
