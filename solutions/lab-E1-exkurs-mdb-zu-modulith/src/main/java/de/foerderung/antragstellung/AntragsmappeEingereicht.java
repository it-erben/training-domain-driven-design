package de.foerderung.antragstellung;

import java.time.Instant;
import java.util.UUID;

/**
 * Public module API event: published when an AntragsMappe has been submitted.
 * This event is part of the module's public API and can be consumed by other modules.
 * Uses only primitive types to avoid coupling between bounded contexts.
 */
public record AntragsmappeEingereicht(
        UUID antragsmappeId,
        String registrierungsNummer,
        Instant eingereichtAm
) {
    public AntragsmappeEingereicht(UUID antragsmappeId, String registrierungsNummer) {
        this(antragsmappeId, registrierungsNummer, Instant.now());
    }
}
