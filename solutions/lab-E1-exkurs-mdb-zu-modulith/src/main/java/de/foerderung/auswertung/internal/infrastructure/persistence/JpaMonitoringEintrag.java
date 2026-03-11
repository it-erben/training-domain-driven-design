package de.foerderung.auswertung.internal.infrastructure.persistence;

import de.foerderung.auswertung.internal.domain.model.AntragsReferenz;
import de.foerderung.auswertung.internal.domain.model.MonitoringEintrag;
import de.foerderung.auswertung.internal.domain.model.MonitoringEintragId;
import de.foerderung.auswertung.internal.domain.model.MonitoringsStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "monitoring_eintrag")
public class JpaMonitoringEintrag {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String registrierungsNummer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MonitoringsStatus status;

    @Column(nullable = false)
    private Instant erfasstAm;

    @Column(nullable = false)
    private Instant zuletztGeaendertAm;

    protected JpaMonitoringEintrag() {
    }

    public static JpaMonitoringEintrag fromModel(MonitoringEintrag eintrag) {
        JpaMonitoringEintrag jpa = new JpaMonitoringEintrag();
        jpa.id = eintrag.getId().wert();
        jpa.registrierungsNummer = eintrag.getAntragsReferenz().registrierungsNummer();
        jpa.status = eintrag.getStatus();
        jpa.erfasstAm = eintrag.getErfasstAm();
        jpa.zuletztGeaendertAm = eintrag.getZuletztGeaendertAm();
        return jpa;
    }

    public MonitoringEintrag toModel() {
        return MonitoringEintrag.rekonstruieren(
                new MonitoringEintragId(id),
                new AntragsReferenz(registrierungsNummer),
                status,
                erfasstAm,
                zuletztGeaendertAm);
    }
}
