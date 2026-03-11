package de.foerderung.antragstellung.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Entity representing a supporting document (Nachweis) within the AntragsMappe aggregate.
 */
public class Nachweis {

    private final NachweisId id;
    private final String dokumentTyp;
    private final String eingereichtVon;
    private final Instant eingereichtAm;
    private boolean akzeptiert;

    Nachweis(NachweisId id, String dokumentTyp, String eingereichtVon, Instant eingereichtAm) {
        this.id = Objects.requireNonNull(id, "NachweisId darf nicht null sein");
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

    public static Nachweis rekonstruieren(NachweisId id, String dokumentTyp, String eingereichtVon,
                                           Instant eingereichtAm, boolean akzeptiert) {
        Nachweis nachweis = new Nachweis(id, dokumentTyp, eingereichtVon, eingereichtAm);
        nachweis.akzeptiert = akzeptiert;
        return nachweis;
    }

    /**
     * Accepts this Nachweis. Package-private — only callable via the aggregate root.
     */
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

    public NachweisId getId() {
        return id;
    }

    public String getDokumentTyp() {
        return dokumentTyp;
    }

    public String getEingereichtVon() {
        return eingereichtVon;
    }

    public Instant getEingereichtAm() {
        return eingereichtAm;
    }

    public boolean isAkzeptiert() {
        return akzeptiert;
    }
}
