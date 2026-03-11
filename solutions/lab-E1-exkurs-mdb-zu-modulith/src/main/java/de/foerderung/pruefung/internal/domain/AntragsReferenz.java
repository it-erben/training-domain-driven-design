package de.foerderung.pruefung.internal.domain;

import java.util.Objects;
import java.util.UUID;

public record AntragsReferenz(UUID antragsmappeId) {

    public AntragsReferenz {
        Objects.requireNonNull(antragsmappeId, "AntragsmappeId darf nicht null sein");
    }
}
