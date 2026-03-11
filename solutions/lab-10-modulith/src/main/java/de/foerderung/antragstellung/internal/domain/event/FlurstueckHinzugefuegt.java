package de.foerderung.antragstellung.internal.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record FlurstueckHinzugefuegt(
        UUID antragsmappeId,
        UUID flurstueckId,
        LocalDateTime timestamp
) implements AntragEvent {}
