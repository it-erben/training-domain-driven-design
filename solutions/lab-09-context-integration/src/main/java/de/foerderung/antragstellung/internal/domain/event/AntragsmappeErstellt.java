package de.foerderung.antragstellung.internal.domain.event;

import de.foerderung.antragstellung.internal.domain.model.AntragId;
import de.foerderung.antragstellung.internal.domain.model.RegistrierungsNummer;

import java.time.Instant;

public record AntragsmappeErstellt(
        AntragId antragsmappeId,
        RegistrierungsNummer registrierungsNummer,
        Instant occurredAt
) implements AntragEvent {}
