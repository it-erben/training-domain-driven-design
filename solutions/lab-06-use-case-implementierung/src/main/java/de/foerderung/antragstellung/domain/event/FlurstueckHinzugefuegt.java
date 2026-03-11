package de.foerderung.antragstellung.domain.event;

import de.foerderung.antragstellung.domain.model.AntragId;
import de.foerderung.antragstellung.domain.model.FlurstueckId;
import de.foerderung.antragstellung.domain.model.FlurstueckNummer;

import java.math.BigDecimal;
import java.time.Instant;

public record FlurstueckHinzugefuegt(
        AntragId antragsmappeId,
        FlurstueckId flurstueckId,
        FlurstueckNummer flurstueckNummer,
        BigDecimal flaeche,
        Instant occurredAt
) implements AntragEvent {}
