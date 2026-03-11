---
marp: true
theme: default
paginate: true
header: "DDD & Clean Architecture mit Spring Boot 4"
footer: "CC BY-NC-SA 4.0, Alexander Erben"
---

# Modul 09 - Use Cases & Application Services

## Der Orchestrator zwischen Adapter und Domain

---

### Lernziele

- Application Service als Use-Case-Orchestrator verstehen
- Unterschied zwischen Domain Service und Application Service kennen
- Commands und Queries als Java Records modellieren
- `@Transactional` korrekt einsetzen
- Error Handling in Application Services beherrschen
- CQRS-lite: Lese- und Schreib-Use-Cases trennen

---
<style scoped>section { font-size: 1.9em; }</style>

## Was ist ein Application Service?

- Orchestrator für einen konkreten Use Case
- Koordiniert den Ablauf zwischen Domain und Infrastruktur
- Enthält keine Geschäftslogik selbst
- Verantwortlich für:
  - Laden von Aggregates aus Repositories
  - Aufrufen von Domain-Methoden
  - Speichern von Ergebnissen
  - Transaktionssteuerung
  - Technische Nebenwirkungen nur commit-sicher auslösen

> Der Application Service ist der Dirigent - er spielt kein Instrument selbst.

---

## Der Use-Case-Flow

![Use-Case-Flow](images/use-case-flow.drawio.svg)

1. Controller empfängt Request, mappt auf Command
2. Application Service lädt Aggregate, ruft Domain-Logik auf
3. Domain führt Geschäftslogik aus und schützt Invarianten
4. Repository persistiert das Ergebnis
5. Response geht zurück; Integrationsereignisse erst nach Commit

---
<style scoped>section { font-size: 1.9em; }</style>

## Application Service vs. Domain Service

| Aspekt | Application Service | Domain Service |
|--------|-------------------|----------------|
| Zweck | Orchestrierung eines Use Case | Domänenlogik über Aggregate hinweg |
| Geschäftslogik | Nein | Ja |
| Abhängigkeiten | Ports (z. B. Repository) | Nur Domain-Objekte |
| Spring | `@Service`, `@Transactional` | Keine (POJO) |
| Schicht | `application.service` | `domain.model` |
| Testbar ohne Spring | Ja (Ports mocken) | Ja (reines Java) |

---
<style scoped>section { font-size: 1.9em; }</style>

## Domain Service - Beispiel

```java
// domain.model - no Spring, no framework
public class FoerderbetragBerechnungsService {

    public Foerderbetrag berechnen(AntragsMappe mappe,
                                   FoerderProgramm programm) {
        var gesamtFlaeche = mappe.getFlurstuecke().stream()
            .map(Flurstueck::flaeche)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (programm.hatFlaechenbonus() && gesamtFlaeche.compareTo(new BigDecimal("50")) > 0) {
            return programm.berechneGrundbetrag(gesamtFlaeche)
                .multiplizieren(BigDecimal.valueOf(1.05));
        }
        return programm.berechneGrundbetrag(gesamtFlaeche);
    }
}
```

- Reine Domänenlogik, die keinem einzelnen Aggregate gehört
- Keine Abhängigkeit auf Infrastruktur
- Wird vom Application Service aufgerufen, nicht vom Controller

---
<style scoped>section { font-size: 1.3em; }</style>

## Application Service - Vollständiges Beispiel

```java
@Service
public class FlurstueckHinzufuegenService implements FlurstueckHinzufuegen {

    private final AntragsMappeRepository repository;

    public FlurstueckHinzufuegenService(AntragsMappeRepository repository) {
        this.repository = repository;
    }

    @Transactional
    @Override
    public FlurstueckHinzufuegenResult hinzufuegen(FlurstueckHinzufuegenCommand cmd) {
        var mappe = repository.findById(cmd.antragsmappeId())
            .orElseThrow(() -> new AntragsmappeNichtGefundenException(
                cmd.antragsmappeId()));

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
<style scoped>section { font-size: 1.7em; }</style>

## Command als Java Record

```java
public record FlurstueckHinzufuegenCommand(
    AntragId antragsmappeId,
    FlurstueckNummer flurstueckNummer,
    BigDecimal flaeche
) {
    public FlurstueckHinzufuegenCommand {
        Objects.requireNonNull(antragsmappeId);
        Objects.requireNonNull(flurstueckNummer);
    }
}
```

- Lebt in `application.port` gemeinsam mit dem Interface
- Value Objects im Command: syntaktische Validierung im Adapter, fachliche Regeln in der Domain
- Compact Constructor verhindert null-Werte schon beim Aufruf

---
<style scoped>section { font-size: 1.5em; }</style>

## Warum Commands als Records?

| Vorteil | Erklärung |
|---------|-----------|
| Immutability | Einmal erzeugt, nicht mehr änderbar |
| Selbstdokumentation | Der Record beschreibt, was der Use Case braucht |
| Wenig Overhead | Keine Setter, kein Boilerplate, klarer Fokus |
| Kein Boilerplate | `equals()`, `hashCode()`, `toString()` inklusive |
| Testbarkeit | Records sind leicht zu instanziieren |

### Commands vs. DTOs

![Commands vs DTOs](images/commands-vs-dtos.drawio.svg)

---
<style scoped>section { font-size: 1.5em; }</style>

## CQRS-lite: Lese- und Schreib-Use-Cases trennen

### Schreibender Use Case (Command)

```java
@Service
public class FlurstueckHinzufuegenService implements FlurstueckHinzufuegen {
    @Transactional
    public FlurstueckHinzufuegenResult hinzufuegen(FlurstueckHinzufuegenCommand command) { ... }
}
```

### Lesender Use Case (Query)

```java
@Service
public class FlurstueckeAbfragenUseCase {
    @Transactional(readOnly = true)
    public List<Flurstueck> liste(UUID antragsmappeId) { ... }
}
```

- Schreibend: Ändert State und speichert Aggregate
- Lesend: Separater read-only Use Case für Abfragen
- Im Workshop bewusst pragmatisch: getrennte Use Cases, aber noch kein separates Read Model

---
<style scoped>section { font-size: 1.4em; }</style>

## Read-Only Use Case im Workshop

```java
@Service
@Transactional(readOnly = true)
public class FlurstueckeAbfragenUseCase {

    private final AntragsMappeRepository repository;

    public FlurstueckeAbfragenUseCase(AntragsMappeRepository repository) {
        this.repository = repository;
    }

    public List<Flurstueck> liste(UUID antragsmappeId) {
        var id = new AntragId(antragsmappeId);
        var mappe = repository.findById(id)
            .orElseThrow(() -> new AntragsmappeNichtGefundenException(id));
        return mappe.getFlurstuecke();
    }
}
```

> `readOnly = true` erlaubt Hibernate-Optimierungen (kein Dirty Checking).

---
<style scoped>section { font-size: 1.2em; }</style>

## Result-Typen: Was gibt ein Use Case zurück?

### Schreibende Use Cases

```java
FlurstueckHinzufuegenResult hinzufuegen(FlurstueckHinzufuegenCommand command);

public record FlurstueckHinzufuegenResult(
    FlurstueckId flurstueckId,
    AntragId antragsmappeId,
    FlurstueckNummer flurstueckNummer,
    BigDecimal flaeche
) {}
```

### Mutierende Use Cases ohne neue Ressource

```java
void einreichen(AntragEinreichenCommand command);
```

### Lese-Use-Cases im Workshop

- oft `readOnly = true`
- eigener Use Case statt Wiederverwendung eines Schreib-Services
- Mapping auf Response-DTOs typischerweise im Adapter

---
<style scoped>section { font-size: 1.5em; }</style>

## `@Transactional` - Wo und Warum?

### Richtig: Auf dem Application Service

```java
@Service
public class FlurstueckHinzufuegenService implements FlurstueckHinzufuegen {

    @Transactional  // <- entire use case = one transaction
    public FlurstueckHinzufuegenResult hinzufuegen(FlurstueckHinzufuegenCommand cmd) {
        var mappe = repository.findById(cmd.antragsmappeId())
            .orElseThrow(() -> new AntragsmappeNichtGefundenException(
                cmd.antragsmappeId()));
        var flurstueckId = mappe.flurstueckHinzufuegen(
            cmd.flurstueckNummer(), cmd.flaeche());
        repository.save(mappe);
        return new FlurstueckHinzufuegenResult(
            flurstueckId, mappe.getId(),
            cmd.flurstueckNummer(), cmd.flaeche());
    }
}
```

### Falsch: Auf der Domain

```java
// Domain should remain framework-free!
public class AntragsMappe {
    @Transactional  // <- NEVER!
    public UUID flurstueckHinzufuegen(...) { }
}
```

---
<style scoped>section { font-size: 1.3em; }</style>

## `@Transactional` - Best Practices

| Regel | Warum |
|-------|-------|
| Ein Use Case = eine Transaktion | Unit of Work Pattern |
| `@Transactional` auf der Service-Methode | Nicht tiefer (Domain) und nicht höher (Controller) |
| `readOnly = true` für Leseoperationen | Hibernate-Optimierung, kein Dirty Checking |
| Rollback bei Runtime Exceptions | Ist Standardverhalten von Spring |
| Checked Exceptions: `rollbackFor = ...` | Sonst kein automatischer Rollback |

> Wenn später Domain Events dazukommen: nicht direkt nach `save()` feuern, sondern erst after commit oder per Outbox.

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

> Spring-Proxies fangen nur externe Aufrufe ab.

---
<style scoped>section { font-size: 1.6em; }</style>

## Error Handling - Zwei Kategorien

### Domain Exceptions (fachlich)

```java
public class AntragsmappeNichtGefundenException extends RuntimeException {
    public AntragsmappeNichtGefundenException(AntragId id) {
        super("AntragsMappe mit ID " + id.value() + " nicht gefunden");
    }
}
```

- Im Workshop starten wir bewusst schlicht mit konkreten Exceptions
- Eine gemeinsame `DomainException`-Hierarchie ist ein möglicher nächster Schritt

---

<style scoped>section { font-size: 1.3em; }</style>

## Error Handling: Exception → HTTP-Response

```java
// In the application service: let exceptions propagate!
@Transactional
public FlurstueckHinzufuegenResult hinzufuegen(FlurstueckHinzufuegenCommand cmd) {
    var mappe = repository.findById(cmd.antragsmappeId())
        .orElseThrow(() -> new AntragsmappeNichtGefundenException(
            cmd.antragsmappeId()));
    var flurstueckId = mappe.flurstueckHinzufuegen(
        cmd.flurstueckNummer(), cmd.flaeche());
    repository.save(mappe);
    return new FlurstueckHinzufuegenResult(
        flurstueckId, mappe.getId(),
        cmd.flurstueckNummer(), cmd.flaeche());
    // No try/catch - exceptions flow to the controller advice
}
```

```java
// In the adapter: exception → HTTP status code
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AntragsmappeNichtGefundenException.class)
    public ProblemDetail handleAntragsmappeNichtGefundenException(
            AntragsmappeNichtGefundenException ex) {
        var problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Not Found");
        return problem;
    }
}
```

---

## Gesamtbild: Ein Use Case End-to-End

![Gesamtbild Use Case End-to-End](images/gesamtbild-use-case-e2e.drawio.svg)

---

## Application Services sind zustandslos

Ein Application Service darf **keinen eigenen Zustand** halten:

```java
// ❌ Zustand im Service — stirbt mit dem Pod, unsichtbar für andere Pods:
@Service
public class FlurstueckHinzufuegenService {
    private int zaehler = 0;          // Kein persistierter Zustand!
    private AntragsMappe letzteMappe; // Kein Cache ohne TTL!
}

// ✅ Zustandslos — jeder Request ist unabhängig:
@Service
public class FlurstueckHinzufuegenService implements FlurstueckHinzufuegen {
    private final AntragsMappeRepository repository; // nur Abhängigkeit, kein Zustand
    // ...
}
```

> Ein zustandsloser Application Service kann beliebig oft neu gestartet,
> horizontal skaliert und auf mehreren Pods gleichzeitig ausgeführt werden —
> weil der Zustand der Domäne ausschließlich in der Datenbank lebt.
> (→ Modul 16: Kubernetes-Readiness)

---

## Reflexion: Prüft euer Verständnis

1. Was ist der Unterschied zwischen Application Service und Domain Service?
2. Warum gehört `@Transactional` auf den Application Service — und **nicht** auf die Domain?
3. Wann braucht ein Read-Endpoint ein separates Read Model (CQRS)?

---

## Zusammenfassung

- Application Service = Use-Case-Orchestrator, **zustandslos**, keine Geschäftslogik
- Domain Service = Domänenlogik über Aggregate hinweg
- Commands als schlanke Java Records in `application.command`
- CQRS-lite: getrennte Lese- und Schreib-Use-Cases, noch ohne eigenes Read Model
- `@Transactional` gehört auf den Application Service, nicht auf die Domain
- Technische Nebenwirkungen nur nach Commit auslösen
- Exceptions propagieren zum `@RestControllerAdvice`
- Der Application Service ist dünn - die Logik steckt in der Domain

> Vernon, „Implementing Domain-Driven Design" (2013), S. 521: Application Layer — dünne Orchestrierungsschicht
> Evans, „Domain-Driven Design" (2003), S. 70: Domain Layer — wo Geschäftslogik lebt
> Khononov, „Einführung in Domain-Driven Design" (2022), Kapitel 8: Architektur-Patterns (Ports & Adapters, CQRS)

---

## Diskussion: CQRS in der Praxis

> Ein typisches Anti-Pattern in gewachsenen Berechtigungssystemen:
> Der Endpoint `effektiveBerechtigungenLaden` führt gleichzeitig
> externen Abgleich, Nutzer-Import und Standort-Aktualisierung durch —
> obwohl er ein **Lese-Endpoint** ist.

```java
// Typische Kommentare in solchen Codebasen:
// FIXME: Krücke, um neue Nutzer anzulegen bzw. mit externem System abzugleichen
// TODO:  ist nur eine temporäre Lösung, bis alles auf CQRS umgestellt ist
```

- Welche der drei Schreiboperationen gehört in einen Command-Handler?
- Wie würde ein sauberer Lesepfad ohne Seiteneffekte aussehen?
- Welche Lese-Endpoints in unseren Systemen könnten von einem Read Model profitieren?
- Was passiert mit `@Transactional` auf einem Lese-Endpoint, der Schreiboperationen enthält?
  *(Rollback-Verhalten, Nebeneffekte nach Fehler, Idempotenz-Probleme)*

---

## Diskussion: Service greift auf fremdes Repository zu

> Ein weiteres typisches Anti-Pattern in gewachsenen Systemen:
> Ein Application Service aus dem Kontext `Antragstellung` greift direkt
> auf das `PruefungRepository` aus dem Kontext `Pruefung` zu —
> anstatt das Aggregate über dessen eigene Schnittstelle anzusprechen.

```java
// ❌ Service überschreitet Aggregate-Grenzen
@Service
public class AntragstellungService {

    private final PruefungRepository pruefungRepository;  // fremdes Aggregate!

    public boolean istPruefungAbgeschlossen(UUID antragId) {
        return pruefungRepository.findByAntragId(antragId)
            .map(Pruefung::isAbgeschlossen)
            .orElse(false);
    }
}
```

```java
// ✅ Über Domain Event entkoppeln oder dediziertes Query-Interface
// Option A: Lese-Port im Antragstellung-Kontext
public interface PruefungsstatusPort {
    boolean istAbgeschlossen(AntragId antragId);
}

// Option B: Domain Event, das der Pruefungs-Kontext publiziert
// → Antragstellung reagiert, speichert lokalen Zustand
```

- Jedes Aggregate hat eine klare Verantwortungsgrenze — Repository-Zugriff ist eine davon
- Direkte Repository-Abhängigkeiten über Aggregate-Grenzen hinweg erzeugen zykl. Kopplung
- ArchUnit kann dies als Architekturverletzung automatisch aufdecken (→ Modul 11)
- Lösung: Anti-Corruption Layer, dediziertes Query-Port oder Event-basierte Kommunikation
- Welche Abhängigkeiten in euren Systemen verletzen heute die Aggregate-Grenzen auf dieselbe Weise?

> Vernon, „Implementing Domain-Driven Design" (2013), S. 521: Application Services und Use-Case-Grenzen

---

## Hands-on: Lab 06

### Einen Use-Case implementieren
