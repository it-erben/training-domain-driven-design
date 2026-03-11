package de.foerderung.antragstellung.infrastructure.persistence;

import de.foerderung.antragstellung.domain.model.Nachweis;
import de.foerderung.antragstellung.domain.model.NachweisId;
import jakarta.persistence.Embeddable;

import java.time.Instant;
import java.util.UUID;

@Embeddable
public class JpaNachweis {

    private UUID nachweisId;
    private String dokumentTyp;
    private String eingereichtVon;
    private Instant eingereichtAm;
    private boolean akzeptiert;

    protected JpaNachweis() {
    }

    public static JpaNachweis fromModel(Nachweis nachweis) {
        JpaNachweis jpa = new JpaNachweis();
        jpa.nachweisId = nachweis.getId().value();
        jpa.dokumentTyp = nachweis.getDokumentTyp();
        jpa.eingereichtVon = nachweis.getEingereichtVon();
        jpa.eingereichtAm = nachweis.getEingereichtAm();
        jpa.akzeptiert = nachweis.isAkzeptiert();
        return jpa;
    }

    public Nachweis toModel() {
        return Nachweis.rekonstruieren(
                new NachweisId(nachweisId),
                dokumentTyp,
                eingereichtVon,
                eingereichtAm,
                akzeptiert);
    }
}
