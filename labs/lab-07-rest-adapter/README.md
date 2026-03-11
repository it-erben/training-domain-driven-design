# Lab 07: REST-Adapter - Flurstück-API

## Aufgabe

Implementiere einen REST-Adapter, der HTTP-Requests entgegennimmt, in Commands
übersetzt und an den Use Case delegiert.

### Schritt 1: Request-DTO erstellen

Erstelle das Request-DTO `FlurstueckHinzufuegenRequest` als Java Record im
Package `de.foerderung.antragstellung.adapter.web`:

```java
public record FlurstueckHinzufuegenRequest(
        @NotBlank String flurstueckNummer,
        @NotNull @Positive BigDecimal flaeche
) {
    public FlurstueckHinzufuegenCommand toCommand(UUID antragsmappeId) {
        return new FlurstueckHinzufuegenCommand(
            new AntragId(antragsmappeId),
            new FlurstueckNummer(flurstueckNummer),
            flaeche);
    }
}
```

Hinweis: Die Validierungs-Annotationen (`@NotBlank`, `@NotNull`, `@Positive`)
gehören zur Adapter-Schicht - das Domain-Modell validiert sich selbst. `@Positive`
stellt sicher, dass nur positive Flächen akzeptiert werden.

### Schritt 2: Response-DTO erstellen

Erstelle das Response-DTO `FlurstueckHinzufuegenResponse` als Java Record im
selben Package:

```java
public record FlurstueckHinzufuegenResponse(
        UUID flurstueckId,
        UUID antragsmappeId,
        String flurstueckNummer,
        BigDecimal flaeche
) {
    public static FlurstueckHinzufuegenResponse from(
            FlurstueckHinzufuegenResult result) {
        return new FlurstueckHinzufuegenResponse(
            result.flurstueckId().value(), result.antragsmappeId().value(),
            result.flurstueckNummer().wert(), result.flaeche());
    }
}
```

Hinweis: Die `from()`-Factory macht das Mapping explizit und testbar.
Response-DTOs verwenden nur primitive Typen (UUID, String) - keine Value
Objects.

### Schritt 3: Controller implementieren

Erstelle den `FlurstueckController` im Package
`de.foerderung.antragstellung.adapter.web`:

```java

@RestController
@RequestMapping("/api/antragstellung/antragsmappen/{antragsmappeId}/flurstuecke")
public class FlurstueckController {

    private final FlurstueckHinzufuegen flurstueckHinzufuegen; // Port Interface, nicht Service!

    // Constructor Injection

    @PostMapping
    public ResponseEntity<FlurstueckHinzufuegenResponse> hinzufuegen(
            @PathVariable UUID antragsmappeId,
            @Valid @RequestBody FlurstueckHinzufuegenRequest request) {
        // 1. request.toCommand(antragsmappeId)
        // 2. Call use case
        // 3. FlurstueckHinzufuegenResponse.from(result)
        // 4. Return 201 Created with Location header
    }
}
```

Wichtig: Der Controller enthält keine Geschäftslogik. Er ist ein reiner
Adapter, der zwischen HTTP und der Application-Schicht übersetzt. Der Controller
injiziert das **Port Interface** (`FlurstueckHinzufuegen`), nicht die konkrete
Service-Klasse. Das Mapping passiert über `toCommand()` und `from()` auf den
DTOs.

### Schritt 4: Exception-Handler implementieren

Erstelle den `GlobalExceptionHandler` im Package
`de.foerderung.antragstellung.adapter.web`:

```java

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AntragsmappeNichtGefundenException.class)
    public ProblemDetail handleNotFound(
            AntragsmappeNichtGefundenException ex) {
        var problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Not Found");
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(
            MethodArgumentNotValidException ex) {
        // Return ProblemDetail with status 400 and validation errors
    }
}
```

Hinweis: `ProblemDetail` wird seit Spring Boot 4 nativ unterstützt und
implementiert RFC 9457. Setze für jeden Fehlertyp eine eigene `type`-URI, z.B.
`https://api.foerderung.example/errors/antragsmappe-nicht-gefunden`.

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
Verifikation z.B. diese `antragsmappeId`:

- `11111111-1111-1111-1111-111111111111` enthält bereits zwei Flurstücke
- `22222222-2222-2222-2222-222222222222` ist leer und eignet sich für `POST`

Die Daten werden auch beim Start im Log ausgegeben. Die H2-Console steht
weiterhin unter `http://localhost:8080/h2-console` zur Verfügung
(JDBC-URL: `jdbc:h2:mem:foerderantrag`, User: `sa`, kein Passwort).

### Bonus: GET-Endpunkt und Flurstück prüfen

Implementiere zusätzliche Endpunkte:

GET - Alle Flurstücke einer AntragsMappe auflisten:

```java

@GetMapping
public List<FlurstueckResponse> list(@PathVariable UUID antragsmappeId) {
    // Load AntragsMappe and return Flurstuecke as response DTOs
}
```

PATCH - Ein Flurstück als geprüft markieren:

```java

@PatchMapping("/{flurstueckId}/pruefen")
public ResponseEntity<Void> pruefen(
        @PathVariable UUID antragsmappeId,
        @PathVariable UUID flurstueckId) {
    // Delegate to FlurstueckPruefenService
    // Return 204 No Content
}
```

## Verifikation

Starte die Anwendung und führe die folgenden curl-Befehle aus:

### Flurstück hinzufügen (erwartet: 201 Created)

```bash
curl -X POST http://localhost:8080/api/antragstellung/antragsmappen/{antragsmappeId}/flurstuecke \
  -H "Content-Type: application/json" \
  -d '{
    "flurstueckNummer": "042/0815",
    "flaeche": 12.75
  }' \
  -w "\n%{http_code}\n"
```

Erwartete Antwort: HTTP 201, JSON mit `flurstueckId` und `antragsmappeId`,
sowie ein `Location`-Header.

### Nicht existierende AntragsMappe verwenden (erwartet: 404 ProblemDetail)

```bash
curl -X POST http://localhost:8080/api/antragstellung/antragsmappen/00000000-0000-0000-0000-000000000000/flurstuecke \
  -H "Content-Type: application/json" \
  -d '{
    "flurstueckNummer": "042/0815",
    "flaeche": 12.75
  }' \
  -w "\n%{http_code}\n"
```

Erwartete Antwort: HTTP 404, ProblemDetail-JSON:

```json
{
  "type": "https://api.foerderung.example/errors/antragsmappe-nicht-gefunden",
  "title": "Antragsmappe nicht gefunden",
  "status": 404,
  "detail": "AntragsMappe mit ID 00000000-0000-0000-0000-000000000000 nicht gefunden"
}
```

### Validierungsfehler (erwartet: 400 Bad Request)

```bash
curl -X POST http://localhost:8080/api/antragstellung/antragsmappen/{antragsmappeId}/flurstuecke \
  -H "Content-Type: application/json" \
  -d '{
    "flurstueckNummer": "",
    "flaeche": null
  }' \
  -w "\n%{http_code}\n"
```

Erwartete Antwort: HTTP 400, ProblemDetail-JSON mit Validierungsfehlern.
