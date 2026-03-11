package de.foerderung.antragstellung.internal.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record NachweisAkzeptiert(
        UUID antragsmappeId,
        UUID nachweisId,
        LocalDateTime timestamp
) implements AntragEvent {}
