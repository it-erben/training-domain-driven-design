package de.foerderung.pruefung.internal.infrastructure.persistence;

import de.foerderung.pruefung.internal.domain.AntragsReferenz;
import de.foerderung.pruefung.internal.domain.Pruefvorgang;
import de.foerderung.pruefung.internal.domain.PruefvorgangId;
import de.foerderung.pruefung.internal.domain.PruefvorgangStatus;
import de.foerderung.pruefung.internal.domain.RegistrierungsNummer;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pruefvorgang")
public class JpaPruefvorgang {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID antragsmappeId;

    @Column(nullable = false)
    private String registrierungsNummer;

    @Column(nullable = false)
    private Instant eingereichtAm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PruefvorgangStatus status;

    protected JpaPruefvorgang() {
    }

    public static JpaPruefvorgang fromModel(Pruefvorgang pruefvorgang) {
        JpaPruefvorgang jpa = new JpaPruefvorgang();
        jpa.id = pruefvorgang.getId().value();
        jpa.antragsmappeId = pruefvorgang.getAntragsReferenz().value();
        jpa.registrierungsNummer = pruefvorgang.getRegistrierungsNummer().wert();
        jpa.eingereichtAm = pruefvorgang.getEingereichtAm();
        jpa.status = pruefvorgang.getStatus();
        return jpa;
    }

    public Pruefvorgang toModel() {
        return Pruefvorgang.rekonstruieren(
                new PruefvorgangId(id),
                new AntragsReferenz(antragsmappeId),
                new RegistrierungsNummer(registrierungsNummer),
                eingereichtAm,
                status);
    }

    public UUID getAntragsmappeId() {
        return antragsmappeId;
    }
}
