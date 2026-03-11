package de.foerderung.antragstellung.internal.domain.event;

import de.foerderung.antragstellung.internal.domain.model.AntragId;

import java.time.Instant;

/**
 * Internal domain event raised when the AntragsMappe is submitted.
 * This is NOT the integration event - see {@link de.foerderung.antragstellung.AntragsmappeEingereicht}
 * for the public API event that crosses bounded context boundaries.
 */
public record InternalAntragsmappeEingereicht(
        AntragId antragsmappeId,
        Instant occurredAt
) implements AntragEvent {}
