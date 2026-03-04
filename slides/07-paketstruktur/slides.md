---
marp: true
theme: default
paginate: true
header: "DDD & Clean Architecture mit Spring Boot 3"
footer: "© 2026 – Workshop S2090"
style: |
  section {
    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  }
  h1 {
    color: #2d6a4f;
  }
  h2 {
    color: #40916c;
  }
  code {
    background-color: #f0f0f0;
    border-radius: 4px;
    padding: 2px 6px;
  }
---

# Modul 07 – Paketstruktur

## Clean Architecture in Spring Boot umsetzen

**Geschätzte Dauer:** ca. 60 Minuten

### Lernziele

- Clean Architecture auf eine Spring Boot Paketstruktur abbilden
- Wissen, welcher Code in welches Package gehört
- Domain-Schicht frei von Spring-Abhängigkeiten halten
- Mapper zwischen Schichten implementieren
- Multi-Module vs. Package-only Ansatz bewerten können
- Abhängigkeitsregeln mit ArchUnit absichern

---

## Von Ringen zu Packages

- Clean Architecture definiert **konzeptionelle Ringe**
- In Spring Boot übersetzen wir diese in **Java Packages**
- Jedes Package hat klare Verantwortlichkeiten und Abhängigkeitsregeln
- Ziel: Die Dependency Rule wird durch die **Paketstruktur sichtbar**

```
Clean Architecture Ring          Java Package
─────────────────────────────────────────────────────────────
Entities (innen)            →    domain.model, domain.event
Use Cases                   →    application.service, application.port
Interface Adapters          →    adapter.web, adapter.messaging
Frameworks & Drivers        →    infrastructure.persistence, infrastructure.config
```

---

## Paketstruktur – Übersicht

```text
de.immobiliencrm.vermittlung          ← Bounded Context
├── domain                            ← Ring 1: Entities
│   ├── model                         ← Aggregates, Entities, Value Objects
│   ├── event                         ← Domain Events (Records)
│   └── port                          ← Outbound-Ports (Repository-Interfaces)
├── application                       ← Ring 2: Use Cases
│   ├── port                          ← Inbound-Ports (Use-Case-Interfaces)
│   └── service                       ← Use-Case-Implementierungen
├── adapter                           ← Ring 3: Interface Adapters
│   └── web                           ← REST Controller, DTOs, Mapper
└── infrastructure                    ← Ring 4: Frameworks & Drivers
    ├── persistence                   ← JPA Entities, Spring Data Repos, Mapper
    └── config                        ← Spring @Configuration
```

![Paketstruktur](../diagrams/paketstruktur-spring-boot.drawio.png)

---

## Vertikaler Schnitt: Ein Request durch alle Schichten

```
HTTP POST /api/vermittlung/besichtigungen
  │
  ▼
┌─────────────────────────────────────────────────┐
│ adapter.web.BesichtigungController              │  Ring 3
│   → JSON → BesichtigungRequest (DTO)            │
│   → request.toCommand()                         │
└────────────────────┬────────────────────────────┘
                     │ ruft auf
                     ▼
┌─────────────────────────────────────────────────┐
│ application.service.BesichtigungPlanenService   │  Ring 2
│   → repository.findById(vorgangId)              │
│   → vorgang.besichtigungPlanen(termin, kontakt) │
│   → repository.save(vorgang)                    │
└──────┬─────────────────────────────┬────────────┘
       │ lädt / speichert           │ ruft auf
       ▼                            ▼
┌──────────────────┐  ┌──────────────────────────┐
│ infrastructure   │  │ domain.model             │  Ring 1
│ .persistence     │  │   Vermittlungsvorgang    │
│ JpaRepository    │  │   .besichtigungPlanen()  │
│ Adapter          │  │   → Geschäftslogik       │
│                  │  │   → Domain Event sammeln │
└──────────────────┘  └──────────────────────────┘
       Ring 4
```

---

## Domain Layer: `domain.model`

- **Reines Java** – keine Spring-Imports, keine JPA-Annotations
- Aggregates, Entities, Value Objects, Domain Services
- Enthält die gesamte **Geschäftslogik**
- Validierung und Invarianten leben hier

```java
package de.immobiliencrm.vermittlung.domain.model;

// Kein import org.springframework.*
// Kein import jakarta.persistence.*

public class Vermittlungsvorgang {
    private final VorgangId id;
    private VorgangStatus status;
    private final List<Besichtigung> besichtigungen;

    public BesichtigungId besichtigungPlanen(KontaktId interessent,
                                            LocalDateTime termin) {
        if (status != VorgangStatus.AKTIV) {
            throw new VorgangNichtAktivException(id);
        }
        var besichtigung = new Besichtigung(
            BesichtigungId.generate(), interessent, termin);
        besichtigungen.add(besichtigung);
        return besichtigung.getId();
    }
}
```

---

## Domain Layer: `domain.port`

- **Outbound-Ports**: Interfaces für den Zugriff auf externe Ressourcen
- Definiert, was die Domäne von der Außenwelt **braucht**
- Keine Implementierungsdetails – kein JPA, kein SQL

```java
package de.immobiliencrm.vermittlung.domain.port;

import de.immobiliencrm.vermittlung.domain.model.*;

public interface VermittlungsvorgangRepository {

    VorgangId nextId();
    void save(Vermittlungsvorgang vorgang);
    Optional<Vermittlungsvorgang> findById(VorgangId id);
    List<Vermittlungsvorgang> findByStatus(VorgangStatus status);
    void delete(Vermittlungsvorgang vorgang);
}
```

> Kein `extends JpaRepository` – das ist ein **reines Domain-Interface**.

---

## Domain Layer: `domain.event`

- Domain Events als **Java Records** (immutable)
- Vergangenheitsform, fachlich benannt
- Keine Framework-Abhängigkeiten

```java
package de.immobiliencrm.vermittlung.domain.event;

import de.immobiliencrm.vermittlung.domain.model.*;

public record BesichtigungGeplant(
    VorgangId vorgangId,
    BesichtigungId besichtigungId,
    KontaktId interessentId,
    LocalDateTime termin,
    Instant occurredAt
) {
    public BesichtigungGeplant {
        Objects.requireNonNull(vorgangId);
        Objects.requireNonNull(besichtigungId);
    }
}
```

> Events werden im Aggregate **gesammelt** und nach dem Speichern
> von der Infrastruktur **dispatched** (Event Collection Pattern).

---

## Application Layer: `application.port` (Inbound)

- **Inbound-Ports**: Interfaces, die beschreiben, was die Anwendung **kann**
- Optional, aber nützlich für Testbarkeit und Dokumentation
- Der Controller kennt nur das Interface, nicht die Implementierung

```java
package de.immobiliencrm.vermittlung.application.port;

public interface BesichtigungPlanen {

    BesichtigungId planen(PlaneBesichtigungCommand command);
}
```

```java
public record PlaneBesichtigungCommand(
    VorgangId vorgangId,
    KontaktId interessentId,
    LocalDateTime termin
) {
    public PlaneBesichtigungCommand {
        Objects.requireNonNull(vorgangId);
        Objects.requireNonNull(termin, "Termin ist erforderlich");
    }
}
```

---

## Application Layer: `application.service`

- **Implementiert** den Inbound-Port (Use Case)
- Orchestriert den Ablauf: laden → Domain aufrufen → speichern
- Hier leben `@Service` und `@Transactional`
- Kennt die Domain, aber **nicht** die Infrastruktur-Details

```java
package de.immobiliencrm.vermittlung.application.service;

@Service
@Transactional
public class BesichtigungPlanenService implements BesichtigungPlanen {

    private final VermittlungsvorgangRepository repository;

    public BesichtigungPlanenService(VermittlungsvorgangRepository repository) {
        this.repository = repository;
    }

    @Override
    public BesichtigungId planen(PlaneBesichtigungCommand cmd) {
        var vorgang = repository.findById(cmd.vorgangId())
            .orElseThrow(() -> new VorgangNichtGefunden(cmd.vorgangId()));
        var besichtigungId = vorgang.besichtigungPlanen(
            cmd.interessentId(), cmd.termin());
        repository.save(vorgang);
        return besichtigungId;
    }
}
```

---

## Adapter Layer: `adapter.web`

- REST-Controller mit `@RestController`
- **DTOs** als Records für Request und Response
- Mapping: DTO → Command (rein), Domain → ResponseDTO (raus)
- Keine Geschäftslogik – nur Delegation an den Inbound-Port

```java
package de.immobiliencrm.vermittlung.adapter.web;

@RestController
@RequestMapping("/api/vermittlung/besichtigungen")
public class BesichtigungController {

    private final BesichtigungPlanen useCase;

    public BesichtigungController(BesichtigungPlanen useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    public ResponseEntity<BesichtigungResponse> planen(
            @Valid @RequestBody BesichtigungRequest request) {
        var id = useCase.planen(request.toCommand());
        var uri = URI.create("/api/vermittlung/besichtigungen/" + id.value());
        return ResponseEntity.created(uri)
            .body(new BesichtigungResponse(id.value()));
    }
}
```

---

## Adapter Layer: DTOs als Records

```java
// Request-DTO: kommt von außen, wird validiert
public record BesichtigungRequest(
    @NotNull UUID vorgangId,
    @NotNull UUID interessentId,
    @NotNull @Future LocalDateTime termin
) {
    public PlaneBesichtigungCommand toCommand() {
        return new PlaneBesichtigungCommand(
            new VorgangId(vorgangId),
            new KontaktId(interessentId),
            termin);
    }
}

// Response-DTO: geht nach außen
public record BesichtigungResponse(UUID besichtigungId) {}
```

- DTOs verwenden **primitive Typen** (UUID, String) — keine Domain-Objekte
- `toCommand()` mappt DTO → Command und erzeugt Value Objects
- Response-DTOs exponieren nur das, was der Client braucht

---

## Infrastructure: JPA-Entity (separates Modell)

```java
package de.immobiliencrm.vermittlung.infrastructure.persistence;

@Entity
@Table(name = "vermittlungsvorgang")
public class VorgangJpaEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    private VorgangStatus status;

    private UUID immobilieId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "vorgang_id")
    private List<BesichtigungJpaEntity> besichtigungen = new ArrayList<>();

    protected VorgangJpaEntity() {} // JPA braucht Default-Konstruktor

    // Getter und Setter für JPA
}
```

> Die JPA-Entity ist ein **reines Persistenzmodell** — sie enthält
> keine Geschäftslogik und gehört in `infrastructure`.

---

## Infrastructure: Mapper zwischen Domain und JPA

```java
@Component
public class VorgangMapper {

    public VorgangJpaEntity toJpaEntity(Vermittlungsvorgang domain) {
        var entity = new VorgangJpaEntity();
        entity.setId(domain.getId().value());
        entity.setStatus(domain.getStatus());
        entity.setImmobilieId(domain.getImmobilieId().value());
        entity.setBesichtigungen(
            domain.getBesichtigungen().stream()
                .map(this::toBesichtigungJpa)
                .toList());
        return entity;
    }

    public Vermittlungsvorgang toDomain(VorgangJpaEntity entity) {
        return Vermittlungsvorgang.reconstitute(
            new VorgangId(entity.getId()),
            entity.getStatus(),
            new ImmobilieId(entity.getImmobilieId()),
            entity.getBesichtigungen().stream()
                .map(this::toBesichtigungDomain)
                .toList());
    }
}
```

> `reconstitute()` ist eine Factory-Methode zum Wiederherstellen aus der DB
> — im Gegensatz zu `erstellen()`, die Geschäftsregeln prüft.

---

## Infrastructure: Repository-Adapter

```java
@Repository
public class JpaVermittlungsvorgangRepository
        implements VermittlungsvorgangRepository {

    private final VorgangSpringDataRepository jpaRepo;
    private final VorgangMapper mapper;

    public JpaVermittlungsvorgangRepository(
            VorgangSpringDataRepository jpaRepo,
            VorgangMapper mapper) {
        this.jpaRepo = jpaRepo;
        this.mapper = mapper;
    }

    @Override
    public void save(Vermittlungsvorgang vorgang) {
        jpaRepo.save(mapper.toJpaEntity(vorgang));
    }

    @Override
    public Optional<Vermittlungsvorgang> findById(VorgangId id) {
        return jpaRepo.findById(id.value())
            .map(mapper::toDomain);
    }

    @Override
    public VorgangId nextId() {
        return new VorgangId(UUID.randomUUID());
    }
}
```

---

## Infrastructure: Spring Data (interne Hilfsschnittstelle)

```java
package de.immobiliencrm.vermittlung.infrastructure.persistence;

// Nicht öffentlich! Nur vom Repository-Adapter verwendet.
interface VorgangSpringDataRepository
        extends JpaRepository<VorgangJpaEntity, UUID> {

    List<VorgangJpaEntity> findByStatus(VorgangStatus status);
}
```

- Package-private (`interface` ohne `public`)
- Wird **nur** vom `JpaVermittlungsvorgangRepository` verwendet
- Kein Code außerhalb von `infrastructure.persistence` kennt diese Schnittstelle

---

## Was gehört wohin? – Checkliste

| Artefakt | Package | Spring? |
|----------|---------|---------|
| Entity, Value Object | `domain.model` | Nein |
| Aggregate Root | `domain.model` | Nein |
| Repository Interface (Port) | `domain.port` | Nein |
| Domain Event | `domain.event` | Nein |
| Domain Service | `domain.model` | Nein |
| Use-Case-Interface (Port) | `application.port` | Nein |
| Application Service | `application.service` | `@Service` |
| Command / Query Record | `application.port` | Nein |
| JPA Entity | `infrastructure.persistence` | `@Entity` |
| Spring Data Repo | `infrastructure.persistence` | `JpaRepository` |
| Mapper (Domain ↔ JPA) | `infrastructure.persistence` | `@Component` |
| REST Controller | `adapter.web` | `@RestController` |
| Request/Response DTO | `adapter.web` | Nein |

---

## Abhängigkeitsregeln

```
adapter.web ──────────────► application.port
                            application.service
                                    │
                                    │ implements / uses
                                    ▼
infrastructure.persistence    domain.model
         │                    domain.port
         │ implements         domain.event
         │                        ▲
         └────────────────────────┘
```

- `domain.*` importiert **nichts** aus `application`, `infrastructure` oder `adapter`
- `application.*` importiert **nur** `domain.*`
- `infrastructure.*` implementiert Ports aus `domain.port`
- `adapter.*` kennt `application.port` (und ggf. `domain.model` für Result-Typen)

> **Faustregel:** Abhängigkeiten zeigen immer Richtung `domain`.

---

## Abhängigkeitsregeln mit ArchUnit absichern

```java
@AnalyzeClasses(packages = "de.immobiliencrm.vermittlung")
class ArchitectureTest {

    @ArchTest
    static final ArchRule domain_has_no_spring_dependencies =
        noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                "org.springframework..",
                "jakarta.persistence..",
                "jakarta.transaction..");

    @ArchTest
    static final ArchRule domain_does_not_depend_on_outer_layers =
        noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                "..application..",
                "..infrastructure..",
                "..adapter..");
}
```

---

## ArchUnit – Weitere nützliche Regeln

```java
@ArchTest
static final ArchRule application_does_not_depend_on_infrastructure =
    noClasses().that().resideInAPackage("..application..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("..infrastructure..", "..adapter..");

@ArchTest
static final ArchRule controllers_only_call_application_layer =
    classes().that().resideInAPackage("..adapter.web..")
        .should().onlyDependOnClassesThat()
        .resideInAnyPackage(
            "..adapter.web..",
            "..application.port..",
            "..domain.model..",     // für Result-Typen
            "java..",
            "jakarta.validation..",
            "org.springframework..");
```

> **Unser Workshop-Ansatz:** Package-only + ArchUnit-Tests
> statt Multi-Module-Build.

---

## Multi-Module vs. Package-only

| Kriterium | Package-only | Multi-Module |
|-----------|-------------|--------------|
| **Aufwand** | Gering | Mittel bis hoch |
| **Abhängigkeitsschutz** | Konvention + ArchUnit | Compiler-enforced |
| **Build-Zeit** | Schnell | Langsamer (parallelisierbar) |
| **Team-Skalierung** | 1–2 Teams | Mehrere Teams |
| **Domain ohne Spring** | Durch Konvention | Durch `pom.xml` erzwungen |
| **Empfohlen für** | Workshop, MVPs, kleine Teams | Produktivsysteme, große Teams |

### Multi-Module – Maven-Beispiel

```xml
<modules>
    <module>vermittlung-domain</module>      <!-- Keine Spring-Dependency! -->
    <module>vermittlung-application</module> <!-- Nur domain + @Service -->
    <module>vermittlung-infrastructure</module>
    <module>vermittlung-adapter-web</module>
    <module>vermittlung-boot</module>        <!-- Startpunkt, alle Module -->
</modules>
```

---

## Spring Boot: Component Scanning

### Wie findet Spring die Beans über alle Packages?

```java
@SpringBootApplication  // = @ComponentScan + @EnableAutoConfiguration + ...
public class ImmobilienCrmApplication {
    // Muss im Root-Package liegen:
    // de.immobiliencrm
}
```

```
de.immobiliencrm                     ← @SpringBootApplication hier
├── vermittlung
│   ├── application.service          ← @Service wird gefunden ✅
│   ├── infrastructure.persistence   ← @Repository wird gefunden ✅
│   └── adapter.web                  ← @RestController wird gefunden ✅
├── akquise
│   └── ...                          ← auch gefunden ✅
```

- `@SpringBootApplication` scannt ab dem eigenen Package **nach unten**
- Alle `@Component`, `@Service`, `@Repository`, `@RestController` werden erkannt
- Domain-Klassen brauchen **keine Annotation** – sie werden manuell instanziiert

---

## Zusammenfassung

```
┌─────────────────────────────────────────────────────────────────┐
│ adapter.web                                                     │
│   Controller → DTO → Command                                   │
├─────────────────────────────────────────────────────────────────┤
│ application.service                                             │
│   @Service @Transactional → orchestriert Use Case               │
├─────────────────────────────────────────────────────────────────┤
│ domain.model / domain.port / domain.event                       │
│   Reines Java: Geschäftslogik, Interfaces, Events               │
├─────────────────────────────────────────────────────────────────┤
│ infrastructure.persistence                                      │
│   JPA Entities, Mapper, Repository-Adapter, Spring Data         │
└─────────────────────────────────────────────────────────────────┘
```

- Jede Schicht hat **eigene Datenstrukturen** (DTO ≠ Command ≠ Domain ≠ JPA)
- Abhängigkeiten zeigen **nur nach innen** (Richtung Domain)
- **ArchUnit** sichert die Regeln im Build ab
- **Mapper** sind der Preis für Entkopplung — aber sie lohnen sich

---

## 🎯 Hands-on: Lab-05

### Paketstruktur für den Vermittlungsprozess aufbauen

- Package-Struktur gemäß Clean Architecture anlegen
- Domain Model ohne Spring-Abhängigkeiten implementieren
- Repository-Interface als Port definieren
- Application Service mit `@Service` und `@Transactional`
- ArchUnit-Test zur Absicherung schreiben

> **Dauer:** ca. 45 Minuten
> Details und Aufgabenstellung im **Lab-05**

---

## 💬 Diskussion

> Welchen Ansatz würdet ihr wählen – Multi-Module oder Package-only?

- Wie groß ist euer Projekt / Team?
- Welche Erfahrungen habt ihr mit Maven/Gradle Multi-Module Builds?
- Reicht ArchUnit als Schutz oder braucht ihr Compiler-Enforcement?
- Wo würdet ihr die Grenze ziehen: ein Package pro BC oder
  ein eigenes Maven-Modul?
- Wie würde ein Refactoring von Package-only zu Multi-Module aussehen?
