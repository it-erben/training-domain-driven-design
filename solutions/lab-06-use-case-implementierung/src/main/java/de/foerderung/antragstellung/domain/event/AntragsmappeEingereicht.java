package de.foerderung.antragstellung.domain.event;

import de.foerderung.antragstellung.domain.model.AntragId;

import java.time.Instant;

public record AntragsmappeEingereicht(
        AntragId antragsmappeId,
        Instant occurredAt
) implements AntragEvent {}
