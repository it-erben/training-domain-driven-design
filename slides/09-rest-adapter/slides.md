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

# Modul 09 – REST Adapter

## Der Inbound Adapter in Clean Architecture

**Geschätzte Dauer:** ca. 70 Minuten

### Lernziele

- `@RestController` als Inbound Adapter in Clean Architecture verstehen
- DTOs als Records gestalten – niemals Domain-Objekte exponieren
- Mapping zwischen DTOs, Commands und Domain sicher umsetzen
- REST-Endpunkte für CRUD korrekt mit HTTP-Statuscodes modellieren
- Problem Details (RFC 9457) mit Spring Boot 3 implementieren
- Integration Tests mit `@WebMvcTest` schreiben

---

## Der REST Adapter in Clean Architecture

```
  Außenwelt (HTTP-Client, Browser, anderer Service)
       │
  ┌────▼─────────────────────────────────────────┐
  │  adapter.web                                  │  Ring 3: Interface Adapter
  │    @RestController                            │
  │    Request-DTO → Command (Value Objects)      │
  │    Result → Response-DTO (primitive Typen)    │
  └────┬─────────────────────────────────────────┘
       │ ruft Inbound-Port auf
  ┌────▼─────────────────────────────────────────┐
  │  application.port / application.service       │  Ring 2: Use Case
  │    BesichtigungPlanen.planen(command)         │
  └────┬─────────────────────────────────────────┘
       │
  ┌────▼─────────────────────────────────────────┐
  │  domain.model                                 │  Ring 1: Entities
  │    Vermittlungsvorgang.besichtigungPlanen()   │
  └──────────────────────────────────────────────┘
```

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
// NIEMALS:
@GetMapping("/{id}")
public Vermittlungsvorgang getById(@PathVariable UUID id) {
    return repository.findById(new VorgangId(id)).orElseThrow();
}
```

> DTOs gehören zum **Adapter** — sie sind das **API-Kontract** mit der Außenwelt.

---

## Record-basierte DTOs – Request

```java
package de.immobiliencrm.vermittlung.adapter.web;

public record BesichtigungAnlegenRequest(
    @NotNull UUID vorgangId,
    @NotNull @Future LocalDateTime termin,
    @NotNull UUID interessentId
) {
    // Mapping: DTO → Command (primitive Typen → Value Objects)
    public PlaneBesichtigungCommand toCommand() {
        return new PlaneBesichtigungCommand(
            new VorgangId(vorgangId),
            new KontaktId(interessentId),
            termin);
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
public record BesichtigungResponse(
    UUID besichtigungId,
    UUID vorgangId,
    LocalDateTime termin,
    String status
) {
    // Factory: Domain-Result → Response-DTO
    public static BesichtigungResponse from(
            BesichtigungId id,
            VorgangId vorgangId,
            LocalDateTime termin) {
        return new BesichtigungResponse(
            id.value(), vorgangId.value(), termin, "GEPLANT");
    }
}
```

```java
public record VorgangDetailResponse(
    UUID id, String status, int anzahlBesichtigungen,
    List<BesichtigungKurzResponse> besichtigungen
) {
    public static VorgangDetailResponse from(VorgangDetails details) { /* ... */ }
}
```

- Response-DTOs verwenden **primitive Typen** (UUID, String) — keine Value Objects
- `from()`-Factory macht das Mapping explizit und testbar

---

## REST Controller – POST (Ressource erstellen)

```java
@RestController
@RequestMapping("/api/v1/vermittlung/besichtigungen")
public class BesichtigungController {

    private final BesichtigungPlanen anlegenUseCase;
    private final VorgangAbfragen abfragenUseCase;

    public BesichtigungController(BesichtigungPlanen anlegenUseCase,
                                  VorgangAbfragen abfragenUseCase) {
        this.anlegenUseCase = anlegenUseCase;
        this.abfragenUseCase = abfragenUseCase;
    }

    @PostMapping
    public ResponseEntity<BesichtigungResponse> anlegen(
            @Valid @RequestBody BesichtigungAnlegenRequest request) {
        var command = request.toCommand();
        var id = anlegenUseCase.planen(command);
        var uri = URI.create("/api/v1/vermittlung/besichtigungen/"
            + id.value());
        var response = BesichtigungResponse.from(
            id, command.vorgangId(), command.termin());
        return ResponseEntity.created(uri).body(response);
    }
}
```

---

## REST Controller – GET, PUT, DELETE

```java
@GetMapping("/{id}")
public ResponseEntity<VorgangDetailResponse> getById(
        @PathVariable UUID id) {
    return abfragenUseCase.findById(new VorgangId(id))
        .map(VorgangDetailResponse::from)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
}

@PutMapping("/{id}/status")
public ResponseEntity<Void> statusAendern(
        @PathVariable UUID id,
        @Valid @RequestBody StatusAendernRequest request) {
    statusAendernUseCase.execute(
        new StatusAendernCommand(new VorgangId(id), request.neuerStatus()));
    return ResponseEntity.noContent().build();
}

@DeleteMapping("/{id}")
public ResponseEntity<Void> loeschen(@PathVariable UUID id) {
    loeschenUseCase.execute(new VorgangId(id));
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
| Ressource nicht gefunden | `404 Not Found` | Unbekannte VorgangId |
| Fachliche Regel verletzt | `422 Unprocessable Entity` | Max. Besichtigungen erreicht |
| Interner Fehler | `500 Internal Server Error` | Unerwarteter Datenbankfehler |

---

## Problem Details – RFC 9457

Spring Boot 3 unterstützt RFC 9457 nativ mit der `ProblemDetail`-Klasse:

```json
{
  "type": "https://api.immo-crm.de/errors/vorgang-nicht-gefunden",
  "title": "Vermittlungsvorgang nicht gefunden",
  "status": 404,
  "detail": "Kein Vorgang mit ID 550e8400-e29b-41d4-a716-446655440000",
  "instance": "/api/v1/vermittlung/besichtigungen"
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

    @ExceptionHandler(VorgangNichtGefunden.class)
    public ProblemDetail handleNotFound(VorgangNichtGefunden ex) {
        var problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Vermittlungsvorgang nicht gefunden");
        problem.setType(URI.create(
            "https://api.immo-crm.de/errors/vorgang-nicht-gefunden"));
        return problem;
    }

    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomainViolation(DomainException ex) {
        var problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problem.setTitle("Fachliche Regel verletzt");
        problem.setType(URI.create(
            "https://api.immo-crm.de/errors/domain-violation"));
        return problem;
    }
}
```

---

## Integration Test mit @WebMvcTest

```java
@WebMvcTest(BesichtigungController.class)
class BesichtigungControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private BesichtigungPlanen anlegenUseCase;
    @MockitoBean private VorgangAbfragen abfragenUseCase;

    @Test
    void sollte_besichtigung_anlegen() throws Exception {
        var expectedId = new BesichtigungId(UUID.randomUUID());
        when(anlegenUseCase.planen(any())).thenReturn(expectedId);

        mockMvc.perform(post("/api/v1/vermittlung/besichtigungen")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "vorgangId": "550e8400-e29b-41d4-a716-446655440000",
                      "interessentId": "660e8400-e29b-41d4-a716-446655440000",
                      "termin": "2026-04-15T14:00:00"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.besichtigungId").value(
                expectedId.value().toString()));
    }
}
```

---

## @WebMvcTest – Fehlerfall testen

```java
@Test
void sollte_404_liefern_wenn_vorgang_nicht_existiert() throws Exception {
    when(anlegenUseCase.planen(any()))
        .thenThrow(new VorgangNichtGefunden(
            new VorgangId(UUID.randomUUID())));

    mockMvc.perform(post("/api/v1/vermittlung/besichtigungen")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "vorgangId": "550e8400-e29b-41d4-a716-446655440000",
                  "interessentId": "660e8400-e29b-41d4-a716-446655440000",
                  "termin": "2026-04-15T14:00:00"
                }
                """))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.title")
            .value("Vermittlungsvorgang nicht gefunden"));
}
```

- `@WebMvcTest` lädt **nur** den Web-Layer — kein Spring-Kontext, keine DB
- Use Cases werden mit `@MockitoBean` gemockt
- Testet: Routing, Serialisierung, Validation, Exception Handling

---

## Gesamtbild: Request → Response

```
HTTP POST /api/v1/vermittlung/besichtigungen
  │
  ├─ Spring: Jackson deserialisiert JSON → BesichtigungAnlegenRequest
  │
  ├─ @Valid → Bean Validation
  │    └─ Fehler? → MethodArgumentNotValidException → 400 Bad Request
  │
  ├─ Controller.anlegen()
  │    ├─ request.toCommand()           ← DTO → Command (Value Objects)
  │    ├─ anlegenUseCase.planen(cmd)    ← Inbound-Port aufrufen
  │    │    ├─ VorgangNichtGefunden?    → @RestControllerAdvice → 404
  │    │    └─ DomainException?         → @RestControllerAdvice → 422
  │    └─ BesichtigungResponse.from()   ← Result → Response-DTO
  │
  └─ ResponseEntity.created(uri).body(response) → 201 Created
```

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
2. `BesichtigungController` mit POST und GET Endpunkt implementieren
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
