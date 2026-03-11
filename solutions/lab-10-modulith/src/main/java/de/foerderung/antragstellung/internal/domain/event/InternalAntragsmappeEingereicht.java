package de.foerderung.antragstellung.internal.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Internal domain event raised when the AntragsMappe is submitted.
 * This is NOT the integration event - see {@link de.foerderung.antragstellung.AntragsmappeEingereicht}
 * for the public API event that crosses bounded context boundaries.
 */
public record InternalAntragsmappeEingereicht(
        UUID antragsmappeId,
        LocalDateTime timestamp
) implements AntragEvent {}
