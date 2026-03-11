---
marp: true
theme: default
paginate: true
header: "DDD & Clean Architecture mit Spring Boot 4"
footer: "CC BY-NC-SA 4.0, Alexander Erben"
---

# Modul 08 - Paketstruktur

## Clean Architecture in Spring Boot umsetzen

---

## Lernziele

- Clean Architecture auf eine Spring Boot Paketstruktur abbilden
- Wissen, welcher Code in welches Package gehört
- Domain-Schicht frei von Spring-Abhängigkeiten halten
- Mapper zwischen Schichten implementieren
- Multi-Module vs. Package-only Ansatz bewerten können
- Abhängigkeitsregeln mit ArchUnit absichern

---

## Von Ringen zu Packages

- Clean Architecture definiert konzeptionelle Ringe
- In Spring Boot übersetzen wir diese in Java Packages
- Jedes Package hat klare Verantwortlichkeiten und Abhängigkeitsregeln
- Ziel: Die Dependency Rule wird durch die Paketstruktur sichtbar

```
Clean Architecture Ring          Java Package
─────────────────────────────────────────────────────────────
Entities (innen)            →    domain.model, domain.event
Use Cases                   →    application.service, application.port
Interface Adapters          →    adapter.web, adapter.messaging
Frameworks & Drivers        →    infrastructure.persistence, infrastructure.config
```

---

## Paketstruktur - Übersicht

```text
de.foerderung.antragstellung  ← Bounded Context: Antragstellung
├── domain                                   ← Ring 1: Entities
│   ├── model                                ← AntragsMappe, Flurstück, Betriebsinhaber
│   ├── event                                ← AntragsmappeErstellt, KontrolleDurchgefuehrt
│   └── port                                 ← AntragsMappeRepository (Interface)
├── application                              ← Ring 2: Use Cases
│   ├── port                                 ← FlurstueckHinzufuegen (Inbound-Port)
│   └── service                              ← FlurstueckHinzufuegenService
├── adapter                                  ← Ring 3: Interface Adapters
│   └── web                                  ← AntragsMappeController, DTOs, Mapper
└── infrastructure                           ← Ring 4: Frameworks & Drivers
    ├── persistence                          ← JPA Entities, Spring Data Repos, Mapper
    └── config                               ← Spring @Configuration
```

---

## Domain Layer: `domain.model`

- Enthält die wichtigste Geschäftslogik
- Reines Java - keine Spring-Imports, keine JPA-Annotations
- Aggregates, Entities, Value Objects, Domain Services
- Hier findet die Validierung der Entities stattt

---

```java
package de.foerderung.antragstellung.domain.model;
// Keine Spring-Imports!

public class AntragsMappe {
    private final AntragId id;
    private AntragStatus status;
    private final List<Flurstueck> flurstuecke;

    public FlurstueckId flurstueckHinzufuegen(FlurstueckNummer nummer,
                                            BigDecimal flaeche) {
        if (status == AntragStatus.EINGEREICHT) {
            throw new AntragBereitsEingereichtException(id);
        }
        var flurstueck = new Flurstueck(FlurstueckId.generate(), nummer, flaeche);
        flurstuecke.add(flurstueck);
        return flurstueck.getId();
    }
}
```

---

## Domain Layer: `domain.port`

- Outbound-Ports: Interfaces für den Zugriff auf externe Ressourcen
- Definiert, was die Domäne von der Außenwelt braucht
- Keine Implementierungsdetails - kein JPA, kein SQL

---

```java
package de.foerderung.antragstellung.domain.port;

public interface AntragsMappeRepository {

    void save(AntragsMappe mappe);
    Optional<AntragsMappe> findById(AntragId id);
    Optional<AntragsMappe> findByRegistrierungsNummer(RegistrierungsNummer nr);
    void delete(AntragsMappe mappe);
}
```

Kein `extends JpaRepository` - das ist ein reines Domain-Interface!

---

## Domain Layer: `domain.event`

- Domain Events als Java Records (immutable)
- Vergangenheitsform, fachlich benannt
- Keine Framework-Abhängigkeiten

---

```java
package de.foerderung.antragstellung.domain.event;

public record FlurstueckHinzugefuegt(
    AntragId antragsmappeId, FlurstueckId flurstueckId,
    FlurstueckNummer flurstueckNummer, BigDecimal flaeche,
    Instant occurredAt
) {
    public FlurstueckHinzugefuegt {
        Objects.requireNonNull(antragsmappeId);
        Objects.requireNonNull(flurstueckId);
    }
}
```

Events werden im Aggregate gesammelt und nach dem Speichern von der Infrastruktur dispatched (Event Collection Pattern).

---

## Application Layer: `application.port` (Inbound)

- Inbound-Ports: Interfaces, die beschreiben, was die Anwendung _kann_
- Optional, aber nützlich für Testbarkeit und Dokumentation
- Der Controller kennt nur das Interface, nicht die Implementierung

---

```java
package de.foerderung.antragstellung.application.port;

public interface FlurstueckHinzufuegen {
    FlurstueckHinzufuegenResult hinzufuegen(FlurstueckHinzufuegenCommand command);
}

public record FlurstueckHinzufuegenCommand(
    AntragId antragsmappeId,
    FlurstueckNummer flurstueckNummer,
    BigDecimal flaeche
) {
    public FlurstueckHinzufuegenCommand {
        Objects.requireNonNull(antragsmappeId);
        Objects.requireNonNull(flurstueckNummer, "Flurstuecknummer ist erforderlich");
    }
}

public record FlurstueckHinzufuegenResult(
    FlurstueckId flurstueckId,
    AntragId antragsmappeId,
    FlurstueckNummer flurstueckNummer,
    BigDecimal flaeche
) {}
```

---

## Application Layer: `application.service`

- Implementiert den Inbound-Port (Use Case), falls er getrennt ist
- Orchestriert den Ablauf: laden → Domain aufrufen → speichern
- Hier leben `@Service` und `@Transactional`
- Kennt die Domain, aber nicht die Infrastruktur-Details

---
<style scoped>section { font-size: 1.7em; }</style>

```java
package de.foerderung.antragstellung.application.service;

@Service
@Transactional
public class FlurstueckHinzufuegenService implements FlurstueckHinzufuegen {

    private final AntragsMappeRepository repository;

    public FlurstueckHinzufuegenService(AntragsMappeRepository repository) {
        this.repository = repository;
    }

    @Override
    public FlurstueckHinzufuegenResult hinzufuegen(FlurstueckHinzufuegenCommand cmd) {
        var mappe = repository.findById(cmd.antragsmappeId())
            .orElseThrow(() -> new AntragsmappeNichtGefundenException(cmd.antragsmappeId()));
        var flurstueckId = mappe.flurstueckHinzufuegen(
            cmd.flurstueckNummer(), cmd.flaeche());
        repository.save(mappe);
        return new FlurstueckHinzufuegenResult(
            flurstueckId, mappe.getId(),
            cmd.flurstueckNummer(), cmd.flaeche());
    }
}
```

---

## Adapter Layer: `adapter.web`

- REST-Controller mit `@RestController`
- DTOs als Records für Request und Response
- Mapping: DTO → Command (rein), Domain → ResponseDTO (raus)
- Keine Geschäftslogik - nur Delegation an den Inbound-Port

---
<style scoped>section { font-size: 1.8em; }</style>

```java
package de.foerderung.antragstellung.adapter.web;

@RestController
@RequestMapping("/api/antragstellung/flurstuecke")
public class FlurstueckController {

    private final FlurstueckHinzufuegen useCase;

    public FlurstueckController(FlurstueckHinzufuegen useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    public ResponseEntity<FlurstueckResponse> hinzufuegen(
            @Valid @RequestBody FlurstueckRequest request) {
        var result = useCase.hinzufuegen(request.toCommand());
        var uri = URI.create("/api/antragstellung/flurstuecke/"
            + result.flurstueckId().value());
        return ResponseEntity.created(uri)
            .body(new FlurstueckResponse(result.flurstueckId().value()));
    }
}
```

---
<style scoped>section { font-size: 1.7em; }</style>

## Adapter Layer: DTOs als Records

```java
// Adapter dienen der Übersetzung der äußeren Schicht in die innere.

// Request DTO: kommt von außen und wird validiert
public record FlurstueckRequest(
    @NotNull UUID antragsmappeId,
    @NotNull @Size(min = 1, max = 30) String flurstueckNummer,
    @NotNull @Positive BigDecimal flaeche
) {
    public FlurstueckHinzufuegenCommand toCommand() {
        return new FlurstueckHinzufuegenCommand(
            new AntragId(antragsmappeId),
            new FlurstueckNummer(flurstueckNummer),
            flaeche);
    }
}

// Response DTO: geht nach außen
public record FlurstueckResponse(UUID flurstueckId) {}
```

---
<style scoped>section { font-size: 1.6em; }</style>

## Infrastructure: JPA-Entity (separates Modell)

```java
package de.foerderung.antragstellung.infrastructure.persistence;

@Entity
@Table(name = "antragsmappe")
public class AntragsMappeJpaEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    private AntragStatus status;

    private String registrierungsNummer;

    @Column(name = "beantragte_foerderung_betrag")
    private BigDecimal beantragteFoerderungBetrag;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "antragsmappe_id")
    private List<FlurstueckJpaEntity> flurstuecke = new ArrayList<>();

    protected AntragsMappeJpaEntity() {} // Default Constructor für JPA

    // Getter und Setter
}
```

---
<style scoped>section { font-size: 1.3em; }</style>

## Infrastructure: Mapper zwischen Domain und JPA

```java
@Component
public class AntragsMappeMapper {

    public AntragsMappeJpaEntity toJpaEntity(AntragsMappe domain) {
        var entity = new AntragsMappeJpaEntity();
        entity.setId(domain.getId().value());
        entity.setStatus(domain.getStatus());
        entity.setRegistrierungsNummer(domain.getRegistrierungsNummer().wert());
        entity.setBeantragteFoerderungBetrag(
            domain.getBeantragteFoerderung().betrag());
        entity.setFlurstuecke(
            domain.getFlurstuecke().stream()
                .map(this::toFlurstueckJpa)
                .toList());
        return entity;
    }

    public AntragsMappe toDomain(AntragsMappeJpaEntity entity) {
        return AntragsMappe.rekonstruieren(
            new AntragId(entity.getId()),
            entity.getStatus(),
            new RegistrierungsNummer(entity.getRegistrierungsNummer()),
            new Foerderbetrag(entity.getBeantragteFoerderungBetrag(), "EUR"),
            entity.getFlurstuecke().stream()
                .map(this::toFlurstueckDomain)
                .toList());
    }
}
```

---
<style scoped>section { font-size: 1.1em; }</style>

## Infrastructure: Repository-Adapter

```java
@Repository
public class JpaAntragsMappeRepository
        implements AntragsMappeRepository {

    private final AntragsMappeSpringDataRepository jpaRepo;
    private final AntragsMappeMapper mapper;

    public JpaAntragsMappeRepository(
            AntragsMappeSpringDataRepository jpaRepo,
            AntragsMappeMapper mapper) {
        this.jpaRepo = jpaRepo;
        this.mapper = mapper;
    }

    @Override
    public void save(AntragsMappe mappe) {
        jpaRepo.save(mapper.toJpaEntity(mappe));
    }

    @Override
    public Optional<AntragsMappe> findById(AntragId id) {
        return jpaRepo.findById(id.value())
            .map(mapper::toDomain);
    }

}
// IDs werden von AntragId.generate() (Factory im Domain Layer) erzeugt,
// nicht vom Repository.
```

---

## Infrastructure: Spring Data (interne Hilfsschnittstelle)

```java
package de.foerderung.antragstellung.infrastructure.persistence;

// Not public! Only used by the repository adapter.
interface AntragsMappeSpringDataRepository
        extends JpaRepository<AntragsMappeJpaEntity, UUID> {

    List<AntragsMappeJpaEntity> findByStatus(AntragStatus status);
    Optional<AntragsMappeJpaEntity> findByRegistrierungsNummer(String nr);
}
```

- Package-private (`interface` ohne `public`)
- Wird nur vom `JpaAntragsMappeRepository` verwendet
- Kein Code außerhalb von `infrastructure.persistence` kennt diese Schnittstelle

---

## Abhängigkeitsregeln

![Abhängigkeitsregeln](images/abhaengigkeitsregeln.drawio.svg)

---

## Abhängigkeitsregeln

- `domain.*` importiert nichts aus `application`, `infrastructure` oder `adapter`
- `application.*` importiert nur `domain.*`
- `infrastructure.*` implementiert Ports aus `domain.port`
- `adapter.*` kennt `application.port` (und ggf. `domain.model` für Result-Typen)

> Faustregel: Abhängigkeiten zeigen immer Richtung `domain`.

---

## Multi-Module vs. Package-only

| Kriterium | Package-only | Multi-Module |
|-----------|-------------|--------------|
| Aufwand | Gering | Mittel bis hoch |
| Abhängigkeitsschutz | Konvention + ArchUnit | Compiler-enforced |
| Build-Zeit | Schnell | Langsamer (parallelisierbar) |
| Team-Skalierung | 1-2 Teams | Mehrere Teams |
| Domain ohne Spring | Durch Konvention | Durch `pom.xml` erzwungen |
| Empfohlen für | Workshop, MVPs, kleine Teams | Produktivsysteme, große Teams |

---

### Multi-Module - Maven-Beispiel

```xml
<modules>
    <module>antragstellung-domain</module>      <!-- No Spring dependency! -->
    <module>antragstellung-application</module> <!-- Only domain + @Service -->
    <module>antragstellung-infrastructure</module>
    <module>antragstellung-adapter-web</module>
    <module>antragstellung-boot</module>        <!-- Entry point, all modules -->
</modules>
```

---

## Zusammenfassung

![Zusammenfassung Schichten](images/zusammenfassung-schichten.drawio.svg)

- Jede Schicht hat eigene Datenstrukturen (DTO ≠ Command ≠ Domain ≠ JPA)
- Abhängigkeiten zeigen nur nach innen (Richtung Domain)

---

## Diskussion: Paketstruktur in der Praxis

> Ein typisches Problem in gewachsenen Backends:
> Packages wie `features`, `converter`, `formatter` und `client`
> landen im Service-Bereich, obwohl sie dort nicht hingehören.

- Wo liegen in euren Modulen heute Packages an der falschen Stelle?
- Welche Packages würden ihr nach Clean Architecture in welchen Ring verschieben?
- Wie hilft ArchUnit dabei, solche Verschiebungen dauerhaft abzusichern?
- *„Vermeide Entitäten-Frameworks!"* — Wie passt das zur Domain-Schicht ohne JPA-Annotationen?

---

## Reflexion: Prüft euer Verständnis

1. In welches Package gehört das `AntragsMappeRepository`-**Interface**? In welches die **Implementierung**?
2. Warum darf das `domain`-Package keine Spring-Imports enthalten?
3. Was ist der Unterschied zwischen `adapter.web` und `infrastructure.persistence`?

> Im Lab refaktoriert ihr ein bestehendes Projekt in die Clean-Architecture-Paketstruktur.

---

## Hands-on: Lab-05

### Refactoring zur Paketstruktur
