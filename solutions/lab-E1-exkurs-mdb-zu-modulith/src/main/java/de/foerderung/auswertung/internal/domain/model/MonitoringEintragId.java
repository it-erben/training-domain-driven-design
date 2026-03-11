package de.foerderung.auswertung.internal.domain.model;

import java.util.Objects;
import java.util.UUID;

public record MonitoringEintragId(UUID wert) {

    public MonitoringEintragId {
        Objects.requireNonNull(wert, "MonitoringEintragId darf nicht null sein");
    }

    public static MonitoringEintragId generate() {
        return new MonitoringEintragId(UUID.randomUUID());
    }
}
