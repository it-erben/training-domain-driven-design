package de.foerderung.auswertung.internal.application;

import de.foerderung.auswertung.internal.domain.model.AntragsReferenz;
import de.foerderung.auswertung.internal.domain.model.MonitoringsStatus;

import java.time.Instant;
import java.util.Objects;

public record MonitoringSynchronisierenCommand(
        AntragsReferenz antragsReferenz,
        MonitoringsStatus status,
        Instant geaendertAm
) {
    public MonitoringSynchronisierenCommand {
        Objects.requireNonNull(antragsReferenz, "AntragsReferenz darf nicht null sein");
        Objects.requireNonNull(status, "Status darf nicht null sein");
        Objects.requireNonNull(geaendertAm, "GeaendertAm darf nicht null sein");
    }
}
