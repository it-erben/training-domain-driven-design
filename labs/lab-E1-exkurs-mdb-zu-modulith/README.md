# Lab E1: Von MDB zu Spring Modulith

## Kontext

In diesem Lab transformiert ihr eine typische MDB-basierte Integration in eine saubere Spring-Modulith-Architektur mit DDD-Mustern. Ihr arbeitet dabei die **Stufen 2 und 3** aus dem Exkurs durch:

- **Stufe 2:** DDD nachrüsten (Published Language, ACL, Translator, Idempotenz)
- **Stufe 3:** Spring Modulith + Postgres Outbox (JMS ersetzen)

## Ausgangssituation

Importiert das Projekt aus `initial-project/` in eure IDE. Es basiert auf dem Stand von **Lab 10** (Spring Modulith). Darin existieren bereits:

- **Antragstellung** (Core BC): `AntragsMappe`, `AntragsmappeEingereicht` (Published Language)
- **Pruefung** (Core BC): ACL mit Translator + Listener, idempotenter `PruefungStartenService`
- **Betriebsinhaber**: Einfaches Modul

## Aufgabe: Auswertungs-BC hinzufuegen

Ihr baut den **Auswertungs-BC** (Supporting BC) als dritten konsumierenden Bounded Context. Dieser BC reagiert auf Aenderungen an Antragsmappen und fuehrt ein Monitoring-Dashboard.

### Szenario (aus dem Exkurs)

> Die Legacy-MDB `MonitoringSynchronizerMDB` hoert auf `topic/AntragGeaendert`, castet direkt auf `AntragsmappeAenderung` (fremdes Objekt) und ruft `optimusPrime.synchronisiere()` auf — ohne Translator, ohne eigenes Domaenenmodell, ohne Idempotenz.

Eure Aufgabe: Diese MDB durch eine saubere Spring-Modulith-Integration ersetzen.

---

## Schritt 1: Published Language erweitern (Antragstellung-BC)

Erstellt im **Root-Package** von `de.foerderung.antragstellung` (oeffentliche API):

### 1a) `AenderungsArt.java` (Enum)

```java
public enum AenderungsArt {
    AKTUALISIERT, REAKTIVIERT, ENTFERNT, ARCHIVIERT
}
```

### 1b) `AntragsmappeGeaendert.java` (Event Record)

```java
public record AntragsmappeGeaendert(
    String registrierungsNummer,
    AenderungsArt aenderungsArt,
    Instant geaendertAm
) {
    // Compact constructor mit requireNonNull fuer alle Felder
}
```

> Nur primitive Typen + eigene Enums. Keine Domain-Objekte!

### 1c) Event publizieren

Erweitert `AntragEinreichenService.create()`, sodass nach dem Speichern ein `AntragsmappeGeaendert`-Event publiziert wird:

```java
eventPublisher.publishEvent(new AntragsmappeGeaendert(
    mappe.getRegistrierungsNummer().wert(),
    AenderungsArt.AKTUALISIERT,
    Instant.now()));
```

---

## Schritt 2: Auswertungs-BC — Domain Model

Erstellt unter `de.foerderung.auswertung.internal.domain.model`:

### 2a) `MonitoringEintragId.java` (Typed ID)

```java
public record MonitoringEintragId(UUID wert) {
    // requireNonNull + generate()-Factory
}
```

### 2b) `AntragsReferenz.java` (Value Object — eigenes, NICHT das aus Pruefung!)

```java
public record AntragsReferenz(String registrierungsNummer) {
    // requireNonNull
}
```

### 2c) `MonitoringsStatus.java` (Enum mit Mapping)

```java
public enum MonitoringsStatus {
    AKTIV, INAKTIV, ARCHIVIERT
}
```

### 2d) `MonitoringEintrag.java` (Aggregate Root)

- Factory-Methode `erstellen(MonitoringEintragId, AntragsReferenz, MonitoringsStatus, Instant)`
- Factory-Methode `rekonstruieren(...)` fuer Persistenz
- Methode `aktualisiere(MonitoringsStatus, Instant)` fuer idempotentes Update
- Felder: `id`, `antragsReferenz`, `status`, `erfasstAm`, `zuletztGeaendertAm`

---

## Schritt 3: Auswertungs-BC — Repository Port

Erstellt unter `de.foerderung.auswertung.internal.domain.port`:

### `MonitoringRepository.java`

```java
public interface MonitoringRepository {
    MonitoringEintrag save(MonitoringEintrag eintrag);
    Optional<MonitoringEintrag> findByAntragsReferenz(AntragsReferenz referenz);
}
```

---

## Schritt 4: Auswertungs-BC — Application Service mit Idempotenz

Erstellt unter `de.foerderung.auswertung.internal.application`:

### 4a) `MonitoringSynchronisierenCommand.java`

```java
public record MonitoringSynchronisierenCommand(
    AntragsReferenz antragsReferenz,
    MonitoringsStatus status,
    Instant geaendertAm
) {}
```

### 4b) `MonitoringService.java`

```java
@Service
@Transactional
public class MonitoringService {

    void synchronisiere(MonitoringSynchronisierenCommand cmd) {
        // Idempotenz: findByAntragsReferenz
        // Falls vorhanden: aktualisiere()
        // Falls nicht: erstellen() + save()
    }
}
```

> Das ist der Kern der Uebung: **Update-or-Create** statt blindes Insert!

---

## Schritt 5: Auswertungs-BC — ACL (Translator + Listener)

Erstellt unter `de.foerderung.auswertung.internal.adapter.acl`:

### 5a) `AntragsmappeEventTranslator.java`

```java
@Component
class AntragsmappeEventTranslator {
    MonitoringSynchronisierenCommand translate(AntragsmappeGeaendert event) {
        return new MonitoringSynchronisierenCommand(
            new AntragsReferenz(event.registrierungsNummer()),
            mapStatus(event.aenderungsArt()),
            event.geaendertAm()
        );
    }

    private MonitoringsStatus mapStatus(AenderungsArt art) {
        return switch (art) {
            case AKTUALISIERT, REAKTIVIERT -> MonitoringsStatus.AKTIV;
            case ENTFERNT -> MonitoringsStatus.INAKTIV;
            case ARCHIVIERT -> MonitoringsStatus.ARCHIVIERT;
        };
    }
}
```

### 5b) `AntragsmappeEventListener.java`

```java
@Component
class AntragsmappeEventListener {
    // Inject Translator + Service
    // @ApplicationModuleListener
    // void on(AntragsmappeGeaendert event) { ... }
}
```

---

## Schritt 6: Infrastruktur (Persistence)

Erstellt unter `de.foerderung.auswertung.internal.infrastructure.persistence`:

### 6a) `JpaMonitoringEintrag.java` (JPA Entity)

- `@Entity`, `@Table(name = "monitoring_eintrag")`
- Felder: `id` (UUID), `registrierungsNummer` (String, unique), `status` (Enum), `erfasstAm`, `zuletztGeaendertAm`
- `fromModel()` und `toModel()` Methoden

### 6b) `JpaMonitoringEintragRepository.java` (Spring Data)

```java
interface JpaMonitoringEintragRepository extends JpaRepository<JpaMonitoringEintrag, UUID> {
    Optional<JpaMonitoringEintrag> findByRegistrierungsNummer(String nr);
    boolean existsByRegistrierungsNummer(String nr);
}
```

### 6c) `MonitoringRepositoryAdapter.java`

Implementiert `MonitoringRepository` und delegiert an `JpaMonitoringEintragRepository`.

---

## Schritt 7: Modul-Konfiguration

### 7a) `de/foerderung/auswertung/package-info.java`

```java
@ApplicationModule(allowedDependencies = {"antragstellung"})
package de.foerderung.auswertung;

import org.springframework.modulith.ApplicationModule;
```

---

## Schritt 8: Tests

### 8a) `AuswertungModuleTest.java`

Schreibt einen Test mit der Scenario API:

```java
@ApplicationModuleTest
class AuswertungModuleTest {

    @Test
    void antragGeaendert_erstelltMonitoringEintrag(Scenario scenario) {
        // 1. AntragsmappeGeaendert-Event publizieren
        // 2. Warten bis MonitoringEintrag in der DB existiert
        // 3. Verifizieren: Status == AKTIV
    }

    @Test
    void doppeltesEvent_aktualisiertNurBestehendernEintrag(Scenario scenario) {
        // Idempotenz testen!
        // 1. Erstes Event publizieren + warten
        // 2. Zweites Event publizieren + warten
        // 3. Verifizieren: Nur ein Eintrag, aber zuletztGeaendertAm aktualisiert
    }
}
```

### 8b) Modulith-Struktur verifizieren

Stellt sicher, dass `ModulithStructureTest.verifyModuleStructure()` weiterhin gruen ist.

---

## Erwartetes Ergebnis

Nach Abschluss habt ihr:

```
de.foerderung
├── antragstellung
│   ├── AntragsmappeEingereicht.java      ← Published Language (Lab 10)
│   ├── AntragsmappeGeaendert.java        ← Published Language (NEU)
│   ├── AenderungsArt.java                ← Published Language (NEU)
│   └── internal/...
├── pruefung                               ← aus Lab 10
│   └── internal/...
├── auswertung                             ← NEU
│   └── internal
│       ├── domain/model/                  ← MonitoringEintrag, AntragsReferenz, etc.
│       ├── domain/port/                   ← MonitoringRepository
│       ├── application/                   ← MonitoringService (idempotent!)
│       ├── adapter/acl/                   ← Translator + Listener
│       └── infrastructure/persistence/    ← JPA Entity + Adapter
└── betriebsinhaber                        ← aus Lab 10
```

### Kern-Learnings

1. **Published Language**: Typisierte Events als oeffentliche API des publizierenden BC
2. **ACL + Translator**: Fremde Events in eigene Commands uebersetzen
3. **Eigenes Domaenenmodell**: `AntragsReferenz` und `MonitoringsStatus` gehoeren dem Auswertungs-BC
4. **Idempotenz**: Update-or-Create im Application Service — nicht erst bei Kafka!
5. **Modulverifikation**: Spring Modulith prueft die Grenzen zur Build-Zeit

---

## Dauer

Ca. 45 Minuten
