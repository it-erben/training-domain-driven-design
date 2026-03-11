package de.foerderung.antragstellung.internal.domain.event;

import de.foerderung.antragstellung.internal.domain.model.AntragId;
import de.foerderung.antragstellung.internal.domain.model.NachweisId;

import java.time.Instant;

public record NachweisAkzeptiert(
        AntragId antragsmappeId,
        NachweisId nachweisId,
        Instant occurredAt
) implements AntragEvent {}
