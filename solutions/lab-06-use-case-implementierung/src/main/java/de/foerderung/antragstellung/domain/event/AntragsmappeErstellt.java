package de.foerderung.antragstellung.domain.event;

import de.foerderung.antragstellung.domain.model.AntragId;
import de.foerderung.antragstellung.domain.model.RegistrierungsNummer;

import java.time.Instant;

public record AntragsmappeErstellt(
        AntragId antragsmappeId,
        RegistrierungsNummer registrierungsNummer,
        Instant occurredAt
) implements AntragEvent {}
