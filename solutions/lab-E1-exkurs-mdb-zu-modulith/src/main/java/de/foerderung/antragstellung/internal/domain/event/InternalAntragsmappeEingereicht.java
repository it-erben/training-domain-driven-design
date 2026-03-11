package de.foerderung.antragstellung.internal.domain.event;

import de.foerderung.antragstellung.internal.domain.model.AntragId;

import java.time.Instant;

public record InternalAntragsmappeEingereicht(
        AntragId antragsmappeId,
        Instant occurredAt
) implements AntragEvent {}
