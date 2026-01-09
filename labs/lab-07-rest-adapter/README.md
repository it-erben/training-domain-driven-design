# Lab 07: REST-Adapter - Viewings-API

## Aufgabe

Implementiere einen REST-Adapter, der HTTP-Requests entgegennimmt, in Commands
übersetzt und an den Use Case delegiert.

### Schritt 1: Request-DTO erstellen

Erstelle das Request-DTO `CreateViewingRequest` als Java Record im Package
`de.realestate.brokerage.adapter.web`:

```java
public record CreateViewingRequest(
        @NotBlank String prospectName,
        @NotNull @Future LocalDateTime appointmentDate
) {
    public CreateViewingCommand toCommand(UUID processId) {
        return new CreateViewingCommand(processId, prospectName, appointmentDate);
    }
}
```

Hinweis: Die Validierungs-Annotationen (`@NotBlank`, `@NotNull`, `@Future`)
gehören zur Adapter-Schicht - das Domain-Modell validiert sich selbst. `@Future`
stellt sicher, dass nur Termine in der Zukunft akzeptiert werden.

### Schritt 2: Response-DTO erstellen

Erstelle das Response-DTO `CreateViewingResponse` als Java Record im selben
Package:

```java
public record CreateViewingResponse(
        UUID viewingId,
        UUID processId
) {
    public static CreateViewingResponse from(CreateViewingResult result) {
        return new CreateViewingResponse(result.viewingId(), result.processId());
    }
}
```

Hinweis: Die `from()`-Factory macht das Mapping explizit und testbar.
Response-DTOs verwenden nur primitive Typen (UUID, String) - keine Value
Objects.

### Schritt 3: Controller implementieren

Erstelle den `ViewingController` im Package
`de.realestate.brokerage.adapter.web`:

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
        // 1. request.toCommand(processId)
        // 2. Call use case
        // 3. CreateViewingResponse.from(result)
        // 4. Return 201 Created with Location header
    }
}
```

Wichtig: Der Controller enthält keine Geschäftslogik. Er ist ein reiner
Adapter, der zwischen HTTP und der Application-Schicht übersetzt. Das Mapping
passiert über `toCommand()` und `from()` auf den DTOs.

### Schritt 4: Exception-Handler implementieren

Erstelle den `GlobalExceptionHandler` im Package
`de.realestate.brokerage.adapter.web`:

```java

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProcessNotFoundException.class)
    public ProblemDetail handleNotFound(ProcessNotFoundException ex) {
        // Return ProblemDetail with status 404, error message and type URI
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        // Return ProblemDetail with status 400 and validation errors
    }
}
```

Hinweis: `ProblemDetail` wird seit Spring Boot 4 nativ unterstützt und
implementiert RFC 9457. Setze für jeden Fehlertyp eine eigene `type`-URI, z.B.
`https://api.immo-crm.de/errors/process-not-found`.

Aktiviere Problem Details in der `application.yml`:

```yaml
spring:
  mvc:
    problemdetails:
      enabled: true
```

### Schritt 5: Mit curl testen

Starte die Anwendung und teste die Endpunkte (siehe Verifikation).

Tipp: Die Lösung initialisiert beim Start Testdaten. Verwende für die
Verifikation z.B. diese `processId`:

- `11111111-1111-1111-1111-111111111111` enthält bereits zwei Besichtigungen
- `22222222-2222-2222-2222-222222222222` ist leer und eignet sich für `POST`

Die Daten werden auch beim Start im Log ausgegeben. Die H2-Console steht
weiterhin unter `http://localhost:8080/h2-console` zur Verfügung
(JDBC-URL: `jdbc:h2:mem:realestatecrm`, User: `sa`, kein Passwort).

### Bonus: GET-Endpunkt und Besichtigung abschließen

Implementiere zusätzliche Endpunkte:

GET - Alle Besichtigungen eines Vermittlungsprozesses auflisten:

```java

@GetMapping
public List<ViewingResponse> list(@PathVariable UUID processId) {
    // Load BrokerageProcess and return viewings as response DTOs
}
```

PATCH - Eine Besichtigung als abgeschlossen markieren:

```java

@PatchMapping("/{viewingId}/complete")
public ResponseEntity<Void> complete(
        @PathVariable UUID processId,
        @PathVariable UUID viewingId) {
    // Delegate to CompleteViewingUseCase
    // Return 204 No Content
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
    "appointmentDate": "2026-04-01T14:00:00"
  }' \
  -w "\n%{http_code}\n"
```

Erwartete Antwort: HTTP 201, JSON mit `viewingId` und `processId`, sowie ein
`Location`-Header.

### Nicht existierenden Prozess verwenden (erwartet: 404 ProblemDetail)

```bash
curl -X POST http://localhost:8080/api/brokerage/processes/00000000-0000-0000-0000-000000000000/viewings \
  -H "Content-Type: application/json" \
  -d '{
    "prospectName": "Max Mustermann",
    "appointmentDate": "2026-04-01T14:00:00"
  }' \
  -w "\n%{http_code}\n"
```

Erwartete Antwort: HTTP 404, ProblemDetail-JSON:

```json
{
  "type": "https://api.immo-crm.de/errors/process-not-found",
  "title": "Not Found",
  "status": 404,
  "detail": "BrokerageProcess with ID 00000000-0000-0000-0000-000000000000 not found"
}
```

### Validierungsfehler (erwartet: 400 Bad Request)

```bash
curl -X POST http://localhost:8080/api/brokerage/processes/{processId}/viewings \
  -H "Content-Type: application/json" \
  -d '{
    "prospectName": "",
    "appointmentDate": null
  }' \
  -w "\n%{http_code}\n"
```

Erwartete Antwort: HTTP 400, ProblemDetail-JSON mit Validierungsfehlern.
