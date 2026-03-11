package de.foerderung.antragstellung.internal.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing a supporting document (Nachweis) within the AntragsMappe aggregate.
 */
public class Nachweis {

    private final UUID id;
    private final String dokumentTyp;
    private final String eingereichtVon;
    private final LocalDateTime eingereichtAm;
    private boolean akzeptiert;

    Nachweis(UUID id, String dokumentTyp, String eingereichtVon, LocalDateTime eingereichtAm) {
        this.id = Objects.requireNonNull(id, "ID darf nicht null sein");
        this.dokumentTyp = Objects.requireNonNull(dokumentTyp, "DokumentTyp darf nicht null sein");
        this.eingereichtVon = Objects.requireNonNull(eingereichtVon,
                "EingereichtVon darf nicht null sein");
        this.eingereichtAm = Objects.requireNonNull(eingereichtAm,
                "EingereichtAm darf nicht null sein");
        this.akzeptiert = false;

        if (dokumentTyp.isBlank()) {
            throw new IllegalArgumentException("DokumentTyp darf nicht leer sein");
        }
        if (eingereichtVon.isBlank()) {
            throw new IllegalArgumentException("EingereichtVon darf nicht leer sein");
        }
    }

    public static Nachweis rekonstruieren(UUID id, String dokumentTyp, String eingereichtVon,
                                           LocalDateTime eingereichtAm, boolean akzeptiert) {
        Nachweis nachweis = new Nachweis(id, dokumentTyp, eingereichtVon, eingereichtAm);
        nachweis.akzeptiert = akzeptiert;
        return nachweis;
    }

    void akzeptieren() {
        this.akzeptiert = true;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Nachweis nachweis)) {
            return false;
        }
        return id.equals(nachweis.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public UUID getId() {
        return id;
    }

    public String getDokumentTyp() {
        return dokumentTyp;
    }

    public String getEingereichtVon() {
        return eingereichtVon;
    }

    public LocalDateTime getEingereichtAm() {
        return eingereichtAm;
    }

    public boolean isAkzeptiert() {
        return akzeptiert;
    }
}
