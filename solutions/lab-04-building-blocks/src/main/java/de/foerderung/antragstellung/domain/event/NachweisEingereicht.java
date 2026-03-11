package de.foerderung.antragstellung.domain.event;

import de.foerderung.antragstellung.domain.model.AntragId;
import de.foerderung.antragstellung.domain.model.NachweisId;

import java.time.Instant;

public record NachweisEingereicht(
        AntragId antragsmappeId,
        NachweisId nachweisId,
        String dokumentTyp,
        Instant occurredAt
) implements AntragEvent {}
