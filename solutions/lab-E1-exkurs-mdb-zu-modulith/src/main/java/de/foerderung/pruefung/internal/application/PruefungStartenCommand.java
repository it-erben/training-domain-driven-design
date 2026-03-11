package de.foerderung.pruefung.internal.application;

import de.foerderung.pruefung.internal.domain.AntragsReferenz;
import de.foerderung.pruefung.internal.domain.RegistrierungsNummer;

import java.time.Instant;

public record PruefungStartenCommand(
        AntragsReferenz antragsReferenz,
        RegistrierungsNummer registrierungsNummer,
        Instant eingereichtAm
) {}
