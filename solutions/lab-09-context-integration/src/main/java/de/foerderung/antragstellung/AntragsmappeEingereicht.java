package de.foerderung.antragstellung;

import java.time.Instant;
import java.util.UUID;

/**
 * Public integration event published when an AntragsMappe has been submitted.
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
