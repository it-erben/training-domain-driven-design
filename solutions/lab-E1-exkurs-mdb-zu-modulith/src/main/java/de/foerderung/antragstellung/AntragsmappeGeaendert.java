package de.foerderung.antragstellung;

import java.time.Instant;
import java.util.Objects;

/**
 * Published Language: Emitted when an AntragsMappe has been changed.
 * This replaces the legacy MDB pattern where consumers cast on
 * AntragsmappeAenderung (a foreign shared-kernel object).
 *
 * Uses only primitive types + module-owned enums to avoid coupling.
 */
public record AntragsmappeGeaendert(
        String registrierungsNummer,
        AenderungsArt aenderungsArt,
        Instant geaendertAm
) {
    public AntragsmappeGeaendert {
        Objects.requireNonNull(registrierungsNummer, "RegistrierungsNummer darf nicht null sein");
        Objects.requireNonNull(aenderungsArt, "AenderungsArt darf nicht null sein");
        Objects.requireNonNull(geaendertAm, "GeaendertAm darf nicht null sein");
    }
}
