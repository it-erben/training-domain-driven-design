package de.foerderung.auswertung.internal.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface JpaMonitoringEintragRepository extends JpaRepository<JpaMonitoringEintrag, UUID> {

    Optional<JpaMonitoringEintrag> findByRegistrierungsNummer(String registrierungsNummer);
}
