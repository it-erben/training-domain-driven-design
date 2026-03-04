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

# Modul 08 – Use Cases & Application Services

## Der Orchestrator zwischen Adapter und Domain

**Geschätzte Dauer:** ca. 60 Minuten

### Lernziele

- Application Service als Use-Case-Orchestrator verstehen
- Unterschied zwischen Domain Service und Application Service kennen
- Commands und Queries als Java Records modellieren
- `@Transactional` korrekt einsetzen
- Error Handling in Application Services beherrschen
- CQRS-lite: Lese- und Schreib-Use-Cases trennen

---

## Was ist ein Application Service?

- **Orchestrator** für einen konkreten Use Case
- Koordiniert den Ablauf zwischen Domain und Infrastruktur
- Enthält **keine** Geschäftslogik selbst
- Verantwortlich für:
  - Laden von Aggregates aus Repositories
  - Aufrufen von Domain-Methoden
  - Speichern von Ergebnissen
  - Transaktionssteuerung
  - Event-Dispatching (nach dem Speichern)

> Der Application Service ist der **Dirigent** – er spielt kein Instrument selbst.

---

## Der Use-Case-Flow

```
              Command                         Domain Event
  HTTP ─────────┐                                 │
  Request       ▼                                 ▼
┌──────┐   ┌─────────────┐   ┌──────────┐   ┌──────────┐   ┌────────┐
│ REST │──►│ Application │──►│ Domain   │──►│ Repo-    │──►│ Event  │
│Ctrlr │   │ Service     │   │ Aggregate│   │ sitory   │   │Publish │
└──────┘   └─────────────┘   └──────────┘   └────────┘   └────────┘
  ▲              │                                           │
  │              ▼                                           ▼
  │         Result / ID                              Async Listener
  │              │
  └──── Response ┘
```

1. **Controller** empfängt Request, mappt auf Command
2. **Application Service** lädt Aggregate, ruft Domain-Logik auf
3. **Domain** führt Geschäftslogik aus, sammelt Events
4. **Repository** persistiert das Ergebnis
5. **Events** werden nach dem Speichern dispatched

---

## Application Service vs. Domain Service

| Aspekt | Application Service | Domain Service |
|--------|-------------------|----------------|
| **Zweck** | Orchestrierung eines Use Case | Domänenlogik über Aggregate hinweg |
| **Geschäftslogik** | Nein | Ja |
| **Abhängigkeiten** | Ports (Repository, Event Publisher) | Nur Domain-Objekte |
| **Spring** | `@Service`, `@Transactional` | Keine (POJO) |
| **Schicht** | `application.service` | `domain.model` |
| **Testbar ohne Spring** | Ja (Ports mocken) | Ja (reines Java) |

---

## Domain Service – Beispiel

```java
// domain.model — kein Spring, kein Framework
public class ProvisionsBerechnungService {

    public Provision berechne(Immobilie immobilie, Kaufvertrag vertrag) {
        var basis = vertrag.kaufpreis()
            .multiply(immobilie.provisionssatz());

        if (immobilie.istDenkmalgeschützt()) {
            return new Provision(basis.multiply(BigDecimal.valueOf(0.95)));
        }
        return new Provision(basis);
    }
}
```

- Reine Domänenlogik, die **keinem einzelnen Aggregate** gehört
- Keine Abhängigkeit auf Infrastruktur
- Wird vom Application Service aufgerufen, nicht vom Controller

---

## Application Service – Vollständiges Beispiel

```java
@Service
public class BesichtigungAnlegenUseCase implements BesichtigungAnlegen {

    private final VermittlungsvorgangRepository repository;
    private final DomainEventDispatcher eventDispatcher;

    public BesichtigungAnlegenUseCase(
            VermittlungsvorgangRepository repository,
            DomainEventDispatcher eventDispatcher) {
        this.repository = repository;
        this.eventDispatcher = eventDispatcher;
    }

    @Transactional
    @Override
    public BesichtigungId execute(BesichtigungAnlegenCommand cmd) {
        var vorgang = repository.findById(cmd.vorgangId())
            .orElseThrow(() -> new VorgangNichtGefunden(cmd.vorgangId()));

        var besichtigungId = vorgang.besichtigungAnlegen(
            cmd.interessentId(), cmd.termin());

        repository.save(vorgang);
        eventDispatcher.dispatchAll(vorgang.domainEvents());

        return besichtigungId;
    }
}
```

---

## Command als Java Record

```java
public record BesichtigungAnlegenCommand(
    VorgangId vorgangId,
    KontaktId interessentId,
    LocalDateTime termin
) {
    // Compact Constructor: Validierung am Eingang
    public BesichtigungAnlegenCommand {
        Objects.requireNonNull(vorgangId, "vorgangId darf nicht null sein");
        Objects.requireNonNull(interessentId,
            "interessentId darf nicht null sein");
        Objects.requireNonNull(termin, "termin darf nicht null sein");
        if (termin.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                "Termin muss in der Zukunft liegen");
        }
    }
}
```

- Lebt in `application.port` (oder als innere Klasse des Use-Case-Interfaces)
- Verwendet **Domain-Value-Objects** (`VorgangId`, `KontaktId`), nicht primitive UUIDs
- Commands repräsentieren die **Absicht** des Aufrufers

---

## Warum Commands als Records?

| Vorteil | Erklärung |
|---------|-----------|
| **Immutability** | Einmal erzeugt, nicht mehr änderbar |
| **Selbstdokumentation** | Der Record beschreibt, was der Use Case braucht |
| **Validierung** | Compact Constructor prüft Vorbedingungen sofort |
| **Kein Boilerplate** | `equals()`, `hashCode()`, `toString()` inklusive |
| **Testbarkeit** | Records sind leicht zu instanziieren |

### Commands vs. DTOs

```
HTTP-Layer          Application-Layer        Domain-Layer
┌──────────┐  map   ┌──────────────┐  call   ┌──────────┐
│ Request  │ ─────► │   Command    │ ──────► │ Aggregate│
│ DTO      │        │   Record     │         │ Methode  │
│ (UUID)   │        │ (VorgangId)  │         │          │
└──────────┘        └──────────────┘         └──────────┘
  primitiv            Value Objects           Domain Model
```

---

## CQRS-lite: Lese- und Schreib-Use-Cases trennen

### Schreibender Use Case (Command)

```java
public interface BesichtigungAnlegen {
    BesichtigungId execute(BesichtigungAnlegenCommand cmd);
}
```

### Lesender Use Case (Query)

```java
public interface VorgangAbfragen {
    Optional<VorgangDetails> findById(VorgangId id);
    List<VorgangÜbersicht> findByStatus(VorgangStatus status);
}
```

- **Schreibend:** Ändert State, gibt nur ID zurück, `@Transactional`
- **Lesend:** Kein State-Change, gibt Projektion zurück, `@Transactional(readOnly = true)`
- Kein vollständiges CQRS – aber klare **Intention** pro Use Case

---

## CQRS-lite: Lesender Service

```java
@Service
@Transactional(readOnly = true)
public class VorgangAbfragenService implements VorgangAbfragen {

    private final VermittlungsvorgangRepository repository;

    public VorgangAbfragenService(VermittlungsvorgangRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<VorgangDetails> findById(VorgangId id) {
        return repository.findById(id)
            .map(VorgangDetails::from);
    }

    @Override
    public List<VorgangÜbersicht> findByStatus(VorgangStatus status) {
        return repository.findByStatus(status).stream()
            .map(VorgangÜbersicht::from)
            .toList();
    }
}
```

> `readOnly = true` erlaubt Hibernate-Optimierungen (kein Dirty Checking).

---

## Result-Typen: Was gibt ein Use Case zurück?

### Schreibende Use Cases

```java
// Nur die erzeugte ID zurückgeben
BesichtigungId execute(BesichtigungAnlegenCommand cmd);

// Oder ein Result-Record mit mehr Kontext
public record BesichtigungAnlegenResult(
    BesichtigungId besichtigungId,
    VorgangId vorgangId,
    LocalDateTime termin
) {}
```

### Lesende Use Cases: Projektion (kein Aggregate!)

```java
// Read-Model – nicht das Aggregate selbst exponieren!
public record VorgangDetails(
    UUID id,
    String status,
    int anzahlBesichtigungen,
    LocalDateTime letzteAktivität
) {
    public static VorgangDetails from(Vermittlungsvorgang v) {
        return new VorgangDetails(
            v.getId().value(), v.getStatus().name(),
            v.getBesichtigungen().size(), v.getLetzteAktivität());
    }
}
```

---

## `@Transactional` – Wo und Warum?

### Richtig: Auf dem Application Service

```java
@Service
public class BesichtigungAnlegenUseCase {

    @Transactional  // ← gesamter Use Case = eine Transaktion
    public BesichtigungId execute(BesichtigungAnlegenCommand cmd) {
        var vorgang = repository.findById(cmd.vorgangId())
            .orElseThrow(() -> new VorgangNichtGefunden(cmd.vorgangId()));
        var id = vorgang.besichtigungAnlegen(cmd.interessentId(), cmd.termin());
        repository.save(vorgang);
        return id;
    }
}
```

### Falsch: Auf der Domain

```java
// Domain soll Framework-frei bleiben!
public class Vermittlungsvorgang {
    @Transactional  // ← NIEMALS
    public BesichtigungId besichtigungAnlegen(...) { }
}
```

---

## `@Transactional` – Best Practices

| Regel | Warum |
|-------|-------|
| Ein Use Case = eine Transaktion | Unit of Work Pattern |
| `@Transactional` auf der **Service-Methode** | Nicht tiefer (Domain) und nicht höher (Controller) |
| `readOnly = true` für Leseoperationen | Hibernate-Optimierung, kein Dirty Checking |
| Rollback bei Runtime Exceptions | Ist Standardverhalten von Spring |
| Checked Exceptions: `rollbackFor = ...` | Sonst kein automatischer Rollback |

### Warnung: Selbstaufruf

```java
@Service
public class MyService {
    @Transactional
    public void methodA() { /* ... */ }

    public void methodB() {
        methodA();  // ← @Transactional wird IGNORIERT! (Proxy-Problem)
    }
}
```

> Spring-Proxies fangen nur **externe** Aufrufe ab.

---

## Error Handling – Zwei Kategorien

### Domain Exceptions (fachlich)

```java
public abstract class DomainException extends RuntimeException {
    protected DomainException(String message) {
        super(message);
    }
}

public class VorgangNichtGefunden extends DomainException {
    private final VorgangId id;

    public VorgangNichtGefunden(VorgangId id) {
        super("Vermittlungsvorgang nicht gefunden: " + id.value());
        this.id = id;
    }
    public VorgangId getId() { return id; }
}

public class BesichtigungNichtMöglich extends DomainException { /* ... */ }
public class MaximaleBesichtigungenErreicht extends DomainException { /* ... */ }
```

---

## Error Handling: Exception → HTTP-Response

```java
// Im Application Service: Exceptions propagieren lassen!
@Transactional
public BesichtigungId execute(BesichtigungAnlegenCommand cmd) {
    var vorgang = repository.findById(cmd.vorgangId())
        .orElseThrow(() -> new VorgangNichtGefunden(cmd.vorgangId()));
    var id = vorgang.besichtigungAnlegen(cmd.interessentId(), cmd.termin());
    repository.save(vorgang);
    return id;
    // Keine try/catch – Exceptions fließen zum Controller-Advice
}
```

```java
// Im Adapter: Exception → HTTP-Status-Code
@RestControllerAdvice
public class DomainExceptionHandler {

    @ExceptionHandler(VorgangNichtGefunden.class)
    public ResponseEntity<ErrorResponse> handle(VorgangNichtGefunden ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handle(DomainException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(new ErrorResponse(ex.getMessage()));
    }
}
```

---

## Gesamtbild: Ein Use Case End-to-End

```
     adapter.web                   application.service             domain.model
┌─────────────────────┐     ┌─────────────────────────────┐  ┌──────────────────┐
│ BesichtigungCtrlr   │     │ BesichtigungAnlegenUseCase  │  │ Vermittlungs-    │
│                     │     │                             │  │ vorgang          │
│ POST /besichtigungen│────►│ 1. findById(vorgangId)      │  │                  │
│   → Request DTO     │     │ 2. vorgang.besichtigung-  ──┼─►│ .besichtigung-   │
│   → toCommand()     │     │    Anlegen(termin, kontakt) │  │  Anlegen()       │
│                     │◄────│ 3. save(vorgang)            │  │  → Invarianten   │
│   ← 201 Created    │     │ 4. dispatch(events)         │  │  → Event sammeln │
│   ← Location-Header│     │ 5. return besichtigungId    │  │                  │
└─────────────────────┘     └─────────────────────────────┘  └──────────────────┘
         │                                │                           │
         │ @RestController                │ @Service @Transactional   │ POJO
         │ DTOs, Bean Validation          │ Orchestrierung            │ Geschäftslogik
         │                                │                           │
         ▼                                ▼                           ▼
  infrastructure.persistence: JPA Entity ↔ Mapper ↔ Domain Model
```

---

## Zusammenfassung

- Application Service = **Use-Case-Orchestrator**, keine Geschäftslogik
- Domain Service = **Domänenlogik** über Aggregate hinweg
- **Commands** als Java Records mit Validierung im Compact Constructor
- **CQRS-lite**: separate Use Cases für Lesen und Schreiben
- `@Transactional` gehört auf den **Application Service**, nicht auf die Domain
- Domain Exceptions **propagieren** zum `@RestControllerAdvice`
- Der Application Service ist **dünn** – die Logik steckt in der Domain

---

## 🎯 Hands-on: Lab-06

### Aufgabe

Implementiert den `BesichtigungAnlegenUseCase` im Immobilien-CRM:

1. Command Record mit Validierung erstellen
2. Application Service mit `@Transactional` implementieren
3. Domain-Methode auf dem Aggregate aufrufen
4. Domain Exceptions definieren
5. Unit-Test für den Use Case schreiben (Port mocken)

> **Dauer:** ca. 45 Minuten
> Details und Aufgabenstellung im **Lab-06**

---

## 💬 Diskussion

> Wann wird ein Application Service zu komplex – und was tut ihr dann?

- Wann lohnt es sich, einen Use Case in **mehrere kleinere** aufzuteilen?
- Wie geht ihr mit Use Cases um, die **mehrere Aggregate** betreffen?
- Welche Vorteile bringt CQRS-lite in der Praxis?
- Gibt es Fälle, in denen Geschäftslogik im Application Service akzeptabel ist?
- Wie handhabt ihr **Fehler**, die erst beim Speichern auftreten
  (z.B. Unique-Constraint-Verletzung)?
