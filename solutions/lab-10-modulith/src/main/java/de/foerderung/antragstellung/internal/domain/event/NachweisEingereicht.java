package de.foerderung.antragstellung.internal.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record NachweisEingereicht(
        UUID antragsmappeId,
        String dokumentTyp,
        LocalDateTime timestamp
) implements AntragEvent {}
