# Lab 08: REST-Adapter - Viewings-API

## Lernziel

Einen `@RestController` als Inbound-Adapter implementieren, DTOs für die API-Grenze definieren und Fehlerbehandlung mit `ProblemDetail` (RFC 9457) umsetzen.

## Dauer

45 Minuten

## Voraussetzungen

- Lab 07 abgeschlossen
- Slides Modul 10

## Aufgabe

Implementiere einen REST-Adapter, der HTTP-Requests entgegennimmt, in Commands übersetzt und an den Use Case delegiert.

### Schritt 1: Request-DTO erstellen

Erstelle das Request-DTO `CreateViewingRequest` als Java Record im Package `de.realestate.brokerage.adapter.web`:

```java
public record CreateViewingRequest(
    @NotBlank String prospectName,
    @NotNull LocalDateTime appointmentDate
) {}
```

**Hinweis:** Die Validierungs-Annotationen (`@NotBlank`, `@NotNull`) gehören zur Adapter-Schicht – das Domain-Modell validiert sich selbst.

### Schritt 2: Response-DTO erstellen

Erstelle das Response-DTO `CreateViewingResponse` als Java Record im selben Package:

```java
public record CreateViewingResponse(
    UUID viewingId,
    UUID processId
) {}
```

### Schritt 3: Controller implementieren

Erstelle den `ViewingController` im Package `de.realestate.brokerage.adapter.web`:

```java
@RestController
@RequestMapping("/api/brokerage/processes/{processId}/viewings")
public class ViewingController {

    private final CreateViewingUseCase createViewingUseCase;

    // Constructor Injection

    @PostMapping
    public ResponseEntity<CreateViewingResponse> create(
            @PathVariable UUID processId,
            @Valid @RequestBody CreateViewingRequest request) {
        // 1. Map request DTO to command
        // 2. Call use case
        // 3. Map result to response DTO
        // 4. Return 201 Created with Location header
    }
}
```

**Wichtig:** Der Controller enthält keine Geschäftslogik. Er ist ein reiner Adapter, der zwischen HTTP und der Application-Schicht übersetzt.

### Schritt 4: Exception-Handler implementieren

Erstelle den `GlobalExceptionHandler` im Package `de.realestate.brokerage.adapter.web`:

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProcessNotFoundException.class)
    public ProblemDetail handleNotFound(ProcessNotFoundException ex) {
        // Return ProblemDetail with status 404 and error message
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        // Return ProblemDetail with status 422 and validation errors
    }
}
```

**Hinweis:** `ProblemDetail` wird seit Spring Boot 4 nativ unterstützt und implementiert RFC 9457 (ehemals RFC 7807).

### Schritt 5: Mit curl testen

Starte die Anwendung und teste die Endpunkte (siehe Verifikation).

### Bonus: GET-Endpunkt

Implementiere einen GET-Endpunkt, der alle Besichtigungen eines Vermittlungsprozesses auflistet:

```java
@GetMapping
public List<CreateViewingResponse> list(@PathVariable UUID processId) {
    // Load BrokerageProcess and return viewings as response DTOs
}
```

## Verifikation

Starte die Anwendung und führe die folgenden curl-Befehle aus:

### Besichtigung anlegen (erwartet: 201 Created)

```bash
curl -X POST http://localhost:8080/api/brokerage/processes/{processId}/viewings \
  -H "Content-Type: application/json" \
  -d '{
    "prospectName": "Max Mustermann",
    "appointmentDate": "2025-04-01T14:00:00"
  }' \
  -w "\n%{http_code}\n"
```

Erwartete Antwort: HTTP 201, JSON mit `viewingId` und `processId`.

### Nicht existierenden Prozess verwenden (erwartet: 404 ProblemDetail)

```bash
curl -X POST http://localhost:8080/api/brokerage/processes/00000000-0000-0000-0000-000000000000/viewings \
  -H "Content-Type: application/json" \
  -d '{
    "prospectName": "Max Mustermann",
    "appointmentDate": "2025-04-01T14:00:00"
  }' \
  -w "\n%{http_code}\n"
```

Erwartete Antwort: HTTP 404, ProblemDetail-JSON:

```json
{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "BrokerageProcess with ID 00000000-0000-0000-0000-000000000000 not found"
}
```

### Validierungsfehler (erwartet: 422 ProblemDetail)

```bash
curl -X POST http://localhost:8080/api/brokerage/processes/{processId}/viewings \
  -H "Content-Type: application/json" \
  -d '{
    "prospectName": "",
    "appointmentDate": null
  }' \
  -w "\n%{http_code}\n"
```

Erwartete Antwort: HTTP 422, ProblemDetail-JSON mit Validierungsfehlern.

## Tipps

- Der Controller ist in der Clean-Architecture-Terminologie ein Inbound-Adapter. Er hängt von der Application-Schicht ab, nicht umgekehrt.
- DTOs (Request/Response) gehören zur Adapter-Schicht und werden **nicht** in der Domain- oder Application-Schicht verwendet.
- `ProblemDetail` ist der Standard für Fehlerantworten in REST-APIs und wird von Spring Boot 4 nativ unterstützt.
- Der `Location`-Header in der 201-Antwort teilt dem Client mit, wo die neu erstellte Ressource zu finden ist.
