# Lab 07: REST-Adapter -- API fuer Besichtigungen

## Lernziel

`@RestController` als Inbound-Adapter implementieren, DTOs fuer die API-Grenze definieren und Fehlerbehandlung mit `ProblemDetail` (RFC 9457) umsetzen.

## Dauer

45 Minuten

## Voraussetzungen

- Lab 06 abgeschlossen
- Slides Modul 09

## Aufgabe

Implementiere einen REST-Adapter, der die HTTP-Requests entgegennimmt, in Commands uebersetzt und an den Use Case delegiert.

### Schritt 1: Request-DTO erstellen

Erstelle das Request-DTO `BesichtigungAnlegenRequest` als Java Record im Package `de.immobiliencrm.vermittlung.adapter.web`:

```java
public record BesichtigungAnlegenRequest(
    @NotBlank String interessentName,
    @NotNull LocalDateTime zeitpunkt
) {}
```

**Hinweis:** Die Validierungs-Annotationen (`@NotBlank`, `@NotNull`) gehoeren zur Adapter-Schicht -- das Domain-Modell validiert sich selbst.

### Schritt 2: Response-DTO erstellen

Erstelle das Response-DTO `BesichtigungAnlegenResponse` als Java Record im selben Package:

```java
public record BesichtigungAnlegenResponse(
    UUID besichtigungId,
    UUID vermittlungsvorgangId
) {}
```

### Schritt 3: Controller implementieren

Erstelle den `BesichtigungController` im Package `de.immobiliencrm.vermittlung.adapter.web`:

```java
@RestController
@RequestMapping("/api/vermittlungsvorgaenge/{vorgangId}/besichtigungen")
public class BesichtigungController {

    private final BesichtigungAnlegenUseCase besichtigungAnlegenUseCase;

    // Constructor Injection

    @PostMapping
    public ResponseEntity<BesichtigungAnlegenResponse> anlegen(
            @PathVariable UUID vorgangId,
            @Valid @RequestBody BesichtigungAnlegenRequest request) {
        // 1. Request-DTO in Command umwandeln
        // 2. Use Case aufrufen
        // 3. Result in Response-DTO umwandeln
        // 4. 201 Created mit Location-Header zurueckgeben
    }
}
```

**Wichtig:** Der Controller enthaelt keine Geschaeftslogik. Er ist ein reiner Adapter, der zwischen HTTP und Application-Schicht uebersetzt.

### Schritt 4: Exception Handler implementieren

Erstelle den `GlobalExceptionHandler` im Package `de.immobiliencrm.vermittlung.adapter.web`:

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(VermittlungsvorgangNichtGefundenException.class)
    public ProblemDetail handleNotFound(VermittlungsvorgangNichtGefundenException ex) {
        // ProblemDetail mit Status 404 und Fehlermeldung zurueckgeben
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        // ProblemDetail mit Status 422 und Validierungsfehlern zurueckgeben
    }
}
```

**Hinweis:** `ProblemDetail` ist ab Spring Boot 3 nativ unterstuetzt und implementiert RFC 9457 (ehemals RFC 7807).

### Schritt 5: Testen mit curl

Starte die Anwendung und teste die Endpunkte (siehe Verifikation).

### Bonus: GET-Endpunkt

Implementiere einen GET-Endpunkt, der alle Besichtigungen eines Vermittlungsvorgangs auflistet:

```java
@GetMapping
public List<BesichtigungAnlegenResponse> auflisten(@PathVariable UUID vorgangId) {
    // Vermittlungsvorgang laden und Besichtigungen als Response-DTOs zurueckgeben
}
```

## Verifikation

Starte die Anwendung und fuehre folgende curl-Befehle aus:

### Besichtigung anlegen (erwartet: 201 Created)

```bash
curl -X POST http://localhost:8080/api/vermittlungsvorgaenge/{vorgangId}/besichtigungen \
  -H "Content-Type: application/json" \
  -d '{
    "interessentName": "Max Mustermann",
    "zeitpunkt": "2025-04-01T14:00:00"
  }' \
  -w "\n%{http_code}\n"
```

Erwartete Antwort: HTTP 201, JSON mit `besichtigungId` und `vermittlungsvorgangId`.

### Nicht-existierenden Vermittlungsvorgang verwenden (erwartet: 404 ProblemDetail)

```bash
curl -X POST http://localhost:8080/api/vermittlungsvorgaenge/00000000-0000-0000-0000-000000000000/besichtigungen \
  -H "Content-Type: application/json" \
  -d '{
    "interessentName": "Max Mustermann",
    "zeitpunkt": "2025-04-01T14:00:00"
  }' \
  -w "\n%{http_code}\n"
```

Erwartete Antwort: HTTP 404, ProblemDetail-JSON:

```json
{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "Vermittlungsvorgang mit ID 00000000-0000-0000-0000-000000000000 nicht gefunden"
}
```

### Validierungsfehler (erwartet: 422 ProblemDetail)

```bash
curl -X POST http://localhost:8080/api/vermittlungsvorgaenge/{vorgangId}/besichtigungen \
  -H "Content-Type: application/json" \
  -d '{
    "interessentName": "",
    "zeitpunkt": null
  }' \
  -w "\n%{http_code}\n"
```

Erwartete Antwort: HTTP 422, ProblemDetail-JSON mit Validierungsfehlern.

## Tipps

- Der Controller ist ein Inbound-Adapter in der Clean-Architecture-Terminologie. Er haengt von der Application-Schicht ab, nicht umgekehrt.
- DTOs (Request/Response) gehoeren zur Adapter-Schicht und werden **nicht** in der Domain oder Application-Schicht verwendet.
- `ProblemDetail` ist der Standard fuer Fehlerantworten in REST-APIs und wird von Spring Boot 3 nativ unterstuetzt.
- Der `Location`-Header im 201-Response zeigt dem Client, wo die neu erstellte Ressource zu finden ist.
