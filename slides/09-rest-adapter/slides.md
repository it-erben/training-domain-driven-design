---
marp: true
theme: default
paginate: true
header: "DDD & Clean Architecture mit Spring Boot 4"
footer: "CC BY-NC-SA 4.0, Alexander Erben"
---

# Modul 09 – REST Adapter

## Der Inbound Adapter in Clean Architecture

**Geschätzte Dauer:** ca. 70 Minuten

### Lernziele

- `@RestController` als Inbound Adapter in Clean Architecture verstehen
- DTOs als Records gestalten – niemals Domain-Objekte exponieren
- Mapping zwischen DTOs, Commands und Domain sicher umsetzen
- REST-Endpunkte für CRUD korrekt mit HTTP-Statuscodes modellieren
- Problem Details (RFC 9457) mit Spring Boot 4 implementieren
- Integration Tests mit `@WebMvcTest` schreiben

---

## Der REST Adapter in Clean Architecture

![REST Adapter in Clean Architecture](images/rest-adapter-clean-architecture.drawio.png)

- Der Controller ist ein **Adapter** – er übersetzt HTTP in Domain-Sprache
- Er kennt `application.port`, aber **nicht** `infrastructure`

---

## Warum DTOs? Niemals Domain-Objekte exponieren

| Problem | Was passiert ohne DTOs |
|---------|----------------------|
| **Kopplung** | API-Änderung erzwingt Domain-Änderung (und umgekehrt) |
| **Sicherheit** | Interne Felder werden sichtbar (z.B. `domainEvents`) |
| **Serialisierung** | Jackson-Annotationen wandern in die Domain |
| **Versionierung** | Keine unabhängige API-Evolution möglich |
| **Zirkuläre Deps** | Domain kennt plötzlich `com.fasterxml.jackson` |

```java
// NEVER:
@GetMapping("/{id}")
public BrokerageProcess getById(@PathVariable UUID id) {
    return repository.findById(new ProcessId(id)).orElseThrow();
}
```

> DTOs gehören zum **Adapter** — sie sind das **API-Kontract** mit der Außenwelt.

---

## Record-basierte DTOs – Request

```java
package de.realestate.brokerage.adapter.web;

public record CreateViewingRequest(
    @NotNull UUID processId,
    @NotNull @Future LocalDateTime appointmentDate,
    @NotNull UUID prospectId
) {
    // Mapping: DTO → Command (primitive types → value objects)
    public ScheduleViewingCommand toCommand() {
        return new ScheduleViewingCommand(
            new ProcessId(processId),
            new ContactId(prospectId),
            appointmentDate);
    }
}
```

- Java Records sind ideal für DTOs: **immutable, kompakt**
- Bean Validation direkt auf den Record-Komponenten
- `toCommand()` erzeugt Value Objects aus primitiven Typen
- Kein Boilerplate: `equals()`, `hashCode()`, `toString()` inklusive

---

## Record-basierte DTOs – Response

```java
public record ViewingResponse(
    UUID viewingId,
    UUID processId,
    LocalDateTime appointmentDate,
    String status
) {
    // Factory: domain result → response DTO
    public static ViewingResponse from(
            ViewingId id,
            ProcessId processId,
            LocalDateTime appointmentDate) {
        return new ViewingResponse(
            id.value(), processId.value(), appointmentDate, "SCHEDULED");
    }
}
```

```java
public record ProcessDetailResponse(
    UUID id, String status, int viewingCount,
    List<ViewingSummaryResponse> viewings
) {
    public static ProcessDetailResponse from(ProcessDetails details) { /* ... */ }
}
```

- Response-DTOs verwenden **primitive Typen** (UUID, String) — keine Value Objects
- `from()`-Factory macht das Mapping explizit und testbar

---

## REST Controller – POST (Ressource erstellen)

```java
@RestController
@RequestMapping("/api/v1/brokerage/viewings")
public class ViewingController {

    private final ScheduleViewing createUseCase;
    private final QueryProcess queryUseCase;

    public ViewingController(ScheduleViewing createUseCase,
                             QueryProcess queryUseCase) {
        this.createUseCase = createUseCase;
        this.queryUseCase = queryUseCase;
    }

    @PostMapping
    public ResponseEntity<ViewingResponse> create(
            @Valid @RequestBody CreateViewingRequest request) {
        var command = request.toCommand();
        var id = createUseCase.schedule(command);
        var uri = URI.create("/api/v1/brokerage/viewings/"
            + id.value());
        var response = ViewingResponse.from(
            id, command.processId(), command.appointmentDate());
        return ResponseEntity.created(uri).body(response);
    }
}
```

---

## REST Controller – GET, PUT, DELETE

```java
@GetMapping("/{id}")
public ResponseEntity<ProcessDetailResponse> getById(
        @PathVariable UUID id) {
    return queryUseCase.findById(new ProcessId(id))
        .map(ProcessDetailResponse::from)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
}

@PutMapping("/{id}/status")
public ResponseEntity<Void> changeStatus(
        @PathVariable UUID id,
        @Valid @RequestBody ChangeStatusRequest request) {
    changeStatusUseCase.execute(
        new ChangeStatusCommand(new ProcessId(id), request.newStatus()));
    return ResponseEntity.noContent().build();
}

@DeleteMapping("/{id}")
public ResponseEntity<Void> delete(@PathVariable UUID id) {
    deleteUseCase.execute(new ProcessId(id));
    return ResponseEntity.noContent().build();
}
```

---

## HTTP Status Codes – Best Practices

| Methode | Erfolg | Fehler |
|---------|--------|--------|
| `POST` (erstellen) | `201 Created` + `Location`-Header | `400` / `422` |
| `GET` (lesen) | `200 OK` | `404 Not Found` |
| `PUT` (ändern) | `200 OK` oder `204 No Content` | `404` / `422` |
| `DELETE` (löschen) | `204 No Content` | `404` |

### Fachliche Fehler vs. technische Fehler

| Kategorie | HTTP-Status | Beispiel |
|-----------|------------|---------|
| Syntaktisch ungültig | `400 Bad Request` | Bean Validation fehlgeschlagen |
| Ressource nicht gefunden | `404 Not Found` | Unbekannte ProcessId |
| Fachliche Regel verletzt | `422 Unprocessable Entity` | Max. Besichtigungen erreicht |
| Interner Fehler | `500 Internal Server Error` | Unerwarteter Datenbankfehler |

---

## Problem Details – RFC 9457

Spring Boot 4 unterstützt RFC 9457 nativ mit der `ProblemDetail`-Klasse:

```json
{
  "type": "https://api.immo-crm.de/errors/process-not-found",
  "title": "BrokerageProcess not found",
  "status": 404,
  "detail": "No process with ID 550e8400-e29b-41d4-a716-446655440000",
  "instance": "/api/v1/brokerage/viewings"
}
```

### Aktivierung

```yaml
# application.yml
spring:
  mvc:
    problemdetails:
      enabled: true
```

- **Standardisiertes Fehlerformat** – maschinenlesbar und menschenlesbar
- Jeder Fehlertyp bekommt eine **eigene URI** (`type`-Feld)

---

## @RestControllerAdvice mit ProblemDetail

```java
@RestControllerAdvice
public class DomainExceptionHandler {

    @ExceptionHandler(ProcessNotFoundException.class)
    public ProblemDetail handleNotFound(ProcessNotFoundException ex) {
        var problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("BrokerageProcess not found");
        problem.setType(URI.create(
            "https://api.immo-crm.de/errors/process-not-found"));
        return problem;
    }

    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomainViolation(DomainException ex) {
        var problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problem.setTitle("Domain rule violated");
        problem.setType(URI.create(
            "https://api.immo-crm.de/errors/domain-violation"));
        return problem;
    }
}
```

---

## Integration Test mit @WebMvcTest

```java
@WebMvcTest(ViewingController.class)
class ViewingControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private ScheduleViewing createUseCase;
    @MockitoBean private QueryProcess queryUseCase;

    @Test
    void should_create_viewing() throws Exception {
        var expectedId = new ViewingId(UUID.randomUUID());
        when(createUseCase.schedule(any())).thenReturn(expectedId);

        mockMvc.perform(post("/api/v1/brokerage/viewings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "processId": "550e8400-e29b-41d4-a716-446655440000",
                      "prospectId": "660e8400-e29b-41d4-a716-446655440000",
                      "appointmentDate": "2026-04-15T14:00:00"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.viewingId").value(
                expectedId.value().toString()));
    }
}
```

---

## @WebMvcTest – Fehlerfall testen

```java
@Test
void should_return_404_when_process_does_not_exist() throws Exception {
    when(createUseCase.schedule(any()))
        .thenThrow(new ProcessNotFoundException(
            new ProcessId(UUID.randomUUID())));

    mockMvc.perform(post("/api/v1/brokerage/viewings")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "processId": "550e8400-e29b-41d4-a716-446655440000",
                  "prospectId": "660e8400-e29b-41d4-a716-446655440000",
                  "appointmentDate": "2026-04-15T14:00:00"
                }
                """))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.title")
            .value("BrokerageProcess not found"));
}
```

- `@WebMvcTest` lädt **nur** den Web-Layer — kein Spring-Kontext, keine DB
- Use Cases werden mit `@MockitoBean` gemockt
- Testet: Routing, Serialisierung, Validation, Exception Handling

---

## Gesamtbild: Request → Response

![Gesamtbild Request Response](images/request-response-gesamtbild.drawio.png)

---

## Manuelles Mapping vs. MapStruct

| Kriterium | Manuelles Mapping | MapStruct |
|-----------|-------------------|-----------|
| **Einfachheit** | Sofort verständlich | Annotation Learning Curve |
| **Debugging** | Direkt im Code | Generierter Code |
| **Performance** | Gut | Sehr gut (Compile-Zeit) |
| **Boilerplate** | Wächst mit Anzahl DTOs | Minimal |
| **Value Objects** | Einfach (Konstruktor) | Custom Expressions nötig |

> **Empfehlung:** Startet mit manuellem Mapping (`toCommand()`, `from()`).
> Wechselt zu MapStruct erst, wenn die DTO-Anzahl deutlich wächst.

---

## Zusammenfassung

- `@RestController` ist ein **Inbound Adapter** — übersetzt HTTP ↔ Domain-Sprache
- DTOs als **Records** — niemals Domain-Objekte über die API exponieren
- Request-DTO → `toCommand()` → Value Objects, Result → `from()` → Response-DTO
- HTTP Status Codes: `201` (POST), `200` (GET), `204` (PUT/DELETE), `404`, `422`
- **Problem Details** (RFC 9457) für standardisierte Fehlermeldungen
- `@RestControllerAdvice` fängt Domain Exceptions und mappt auf `ProblemDetail`
- `@WebMvcTest` testet den Adapter **isoliert** ohne Datenbank

---

## 🎯 Hands-on: Lab-07

### Aufgabe

Implementiert den REST Adapter für das Immobilien-CRM:

1. Request- und Response-DTOs als Java Records erstellen
2. `ViewingController` mit POST und GET Endpunkt implementieren
3. Manuelles Mapping: `toCommand()` und `from()` Methoden
4. `@RestControllerAdvice` mit Problem Details (RFC 9457) einrichten
5. Integration Test mit `@WebMvcTest` schreiben

> **Dauer:** ca. 35 Minuten
> Details und Aufgabenstellung im **Lab-07**

---

## 💬 Diskussion

> Welche API-Design-Entscheidungen sind in eurem Kontext wichtig?

- Wie handhabt ihr **API-Versionierung** (URL-Pfad, Header, Query-Parameter)?
- Nutzt ihr **HATEOAS** oder „einfaches" REST?
- Wie dokumentiert ihr eure APIs (OpenAPI/Swagger, Spring REST Docs)?
- Wo zieht ihr die Grenze zwischen **Bean Validation** (DTO)
  und **Domain Validation** (Aggregate)?
