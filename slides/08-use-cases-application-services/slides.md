---
marp: true
theme: default
paginate: true
header: "DDD & Clean Architecture mit Spring Boot 3"
footer: "CC BY-NC-SA 4.0, Alexander Erben"
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
// domain.model — no Spring, no framework
public class CommissionCalculationService {

    public Commission calculate(Property property, PurchaseContract contract) {
        var basis = contract.purchasePrice()
            .multiply(property.commissionRate());

        if (property.isListedBuilding()) {
            return new Commission(basis.multiply(BigDecimal.valueOf(0.95)));
        }
        return new Commission(basis);
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
public class CreateViewingUseCase implements CreateViewing {

    private final BrokerageProcessRepository repository;
    private final DomainEventDispatcher eventDispatcher;

    public CreateViewingUseCase(
            BrokerageProcessRepository repository,
            DomainEventDispatcher eventDispatcher) {
        this.repository = repository;
        this.eventDispatcher = eventDispatcher;
    }

    @Transactional
    @Override
    public ViewingId execute(CreateViewingCommand cmd) {
        var process = repository.findById(cmd.processId())
            .orElseThrow(() -> new ProcessNotFoundException(cmd.processId()));

        var viewingId = process.createViewing(
            cmd.prospectId(), cmd.appointmentDate());

        repository.save(process);
        eventDispatcher.dispatchAll(process.domainEvents());

        return viewingId;
    }
}
```

---

## Command als Java Record

```java
public record CreateViewingCommand(
    ProcessId processId,
    ContactId prospectId,
    LocalDateTime appointmentDate
) {
    // Compact constructor: validation at the entry point
    public CreateViewingCommand {
        Objects.requireNonNull(processId, "processId must not be null");
        Objects.requireNonNull(prospectId,
            "prospectId must not be null");
        Objects.requireNonNull(appointmentDate, "appointmentDate must not be null");
        if (appointmentDate.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                "appointmentDate must be in the future");
        }
    }
}
```

- Lebt in `application.port` (oder als innere Klasse des Use-Case-Interfaces)
- Verwendet **Domain-Value-Objects** (`ProcessId`, `ContactId`), nicht primitive UUIDs
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
│ (UUID)   │        │ (ProcessId)  │         │          │
└──────────┘        └──────────────┘         └──────────┘
  primitiv            Value Objects           Domain Model
```

---

## CQRS-lite: Lese- und Schreib-Use-Cases trennen

### Schreibender Use Case (Command)

```java
public interface CreateViewing {
    ViewingId execute(CreateViewingCommand cmd);
}
```

### Lesender Use Case (Query)

```java
public interface QueryProcess {
    Optional<ProcessDetails> findById(ProcessId id);
    List<ProcessOverview> findByStatus(ProcessStatus status);
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
public class QueryProcessService implements QueryProcess {

    private final BrokerageProcessRepository repository;

    public QueryProcessService(BrokerageProcessRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<ProcessDetails> findById(ProcessId id) {
        return repository.findById(id)
            .map(ProcessDetails::from);
    }

    @Override
    public List<ProcessOverview> findByStatus(ProcessStatus status) {
        return repository.findByStatus(status).stream()
            .map(ProcessOverview::from)
            .toList();
    }
}
```

> `readOnly = true` erlaubt Hibernate-Optimierungen (kein Dirty Checking).

---

## Result-Typen: Was gibt ein Use Case zurück?

### Schreibende Use Cases

```java
// Only return the created ID
ViewingId execute(CreateViewingCommand cmd);

// Or a result record with more context
public record CreateViewingResult(
    ViewingId viewingId,
    ProcessId processId,
    LocalDateTime appointmentDate
) {}
```

### Lesende Use Cases: Projektion (kein Aggregate!)

```java
// Read model – do not expose the aggregate itself!
public record ProcessDetails(
    UUID id,
    String status,
    int viewingCount,
    LocalDateTime lastActivity
) {
    public static ProcessDetails from(BrokerageProcess v) {
        return new ProcessDetails(
            v.getId().value(), v.getStatus().name(),
            v.getViewings().size(), v.getLastActivity());
    }
}
```

---

## `@Transactional` – Wo und Warum?

### Richtig: Auf dem Application Service

```java
@Service
public class CreateViewingUseCase {

    @Transactional  // ← entire use case = one transaction
    public ViewingId execute(CreateViewingCommand cmd) {
        var process = repository.findById(cmd.processId())
            .orElseThrow(() -> new ProcessNotFoundException(cmd.processId()));
        var id = process.createViewing(cmd.prospectId(), cmd.appointmentDate());
        repository.save(process);
        return id;
    }
}
```

### Falsch: Auf der Domain

```java
// Domain should remain framework-free!
public class BrokerageProcess {
    @Transactional  // ← NEVER
    public ViewingId createViewing(...) { }
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

public class ProcessNotFoundException extends DomainException {
    private final ProcessId id;

    public ProcessNotFoundException(ProcessId id) {
        super("BrokerageProcess not found: " + id.value());
        this.id = id;
    }
    public ProcessId getId() { return id; }
}

public class ViewingNotPossibleException extends DomainException { /* ... */ }
public class MaxViewingsReachedException extends DomainException { /* ... */ }
```

---

## Error Handling: Exception → HTTP-Response

```java
// In the application service: let exceptions propagate!
@Transactional
public ViewingId execute(CreateViewingCommand cmd) {
    var process = repository.findById(cmd.processId())
        .orElseThrow(() -> new ProcessNotFoundException(cmd.processId()));
    var id = process.createViewing(cmd.prospectId(), cmd.appointmentDate());
    repository.save(process);
    return id;
    // No try/catch – exceptions flow to the controller advice
}
```

```java
// In the adapter: exception → HTTP status code
@RestControllerAdvice
public class DomainExceptionHandler {

    @ExceptionHandler(ProcessNotFoundException.class)
    public ResponseEntity<ErrorResponse> handle(ProcessNotFoundException ex) {
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
│ ViewingController   │     │ CreateViewingUseCase         │  │ Brokerage-       │
│                     │     │                             │  │ Process          │
│ POST /viewings      │────►│ 1. findById(processId)      │  │                  │
│   → Request DTO     │     │ 2. process.createViewing- ──┼─►│ .createViewing() │
│   → toCommand()     │     │    (appointmentDate,contact)│  │                  │
│                     │◄────│ 3. save(process)            │  │  → Invarianten   │
│   ← 201 Created    │     │ 4. dispatch(events)         │  │  → Event sammeln │
│   ← Location-Header│     │ 5. return viewingId         │  │                  │
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

Implementiert den `CreateViewingUseCase` im Immobilien-CRM:

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
