package de.foerderung.antragstellung.internal.infrastructure.persistence;

import de.foerderung.antragstellung.internal.domain.model.Nachweis;
import jakarta.persistence.Embeddable;

import java.time.LocalDateTime;
import java.util.UUID;

@Embeddable
public class JpaNachweis {

    private UUID nachweisId;
    private String dokumentTyp;
    private String eingereichtVon;
    private LocalDateTime eingereichtAm;
    private boolean akzeptiert;

    protected JpaNachweis() {
    }

    public static JpaNachweis fromModel(Nachweis nachweis) {
        JpaNachweis jpa = new JpaNachweis();
        jpa.nachweisId = nachweis.getId();
        jpa.dokumentTyp = nachweis.getDokumentTyp();
        jpa.eingereichtVon = nachweis.getEingereichtVon();
        jpa.eingereichtAm = nachweis.getEingereichtAm();
        jpa.akzeptiert = nachweis.isAkzeptiert();
        return jpa;
    }

    public Nachweis toModel() {
        return Nachweis.rekonstruieren(
                nachweisId,
                dokumentTyp,
                eingereichtVon,
                eingereichtAm,
                akzeptiert);
    }
}
