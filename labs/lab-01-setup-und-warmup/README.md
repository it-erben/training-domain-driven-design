# Lab 01: Setup und Warmup

## Teil 1: Projekt starten

Importiere das Projekt aus `initial-project/` in deine IDE.

## Teil 2: Betriebsinhaber-CRUD implementieren

Implementiere eine CRUD-API für Betriebsinhaber im Package
`de.foerderung.betriebsinhaber`. Die Aufgabe ist bewusst ohne Schritt-für-Schritt-
Anleitung gehalten, da ihr Spring Boot teilweise schon kennt.
Scheut euch aber nicht,
Fragen zu stellen, falls etwas nicht funktioniert oder es für euch neu ist.

### Was zu bauen ist

Entity `Betriebsinhaber` (`@Entity`, `@Table(name = "betriebsinhaber")`):

| Feld              | Typ          | Constraints                         |
|-------------------|--------------|-------------------------------------|
| `id`              | `Long`       | `@Id`, `@GeneratedValue(IDENTITY)`  |
| `name`            | `String`     | `@NotBlank`                         |
| `betriebsnummer`  | `String`     | `@NotBlank`                         |
| `strasse`         | `String`     | `@NotBlank`                         |
| `plz`             | `String`     | `@NotBlank`                         |
| `ort`             | `String`     | `@NotBlank`                         |
| `betriebsflaeche` | `BigDecimal` | optional (in Hektar)                |
| `foerdersumme`    | `BigDecimal` | optional                            |

Repository: `BetriebsinhaberRepository extends JpaRepository<Betriebsinhaber, Long>`

Service: `BetriebsinhaberService` mit `@Service`, Constructor Injection, CRUD-
Methoden (`findAll`, `findById`, `save`, `update`, `delete`)

Controller: `BetriebsinhaberController` unter `@RequestMapping("/api/betriebsinhaber")`

Die Pfade unten sind relativ zu diesem Basis-Pfad gemeint. In Spring
MVC sollte das typischerweise so aussehen: `@GetMapping`, `@PostMapping`,
`@GetMapping("/{id}")`, `@PutMapping("/{id}")`, `@DeleteMapping("/{id}")`.

| HTTP     | Pfad    | Erfolg           | Fehler        |
|----------|---------|------------------|---------------|
| `GET`    | `/`     | 200 OK           | -             |
| `GET`    | `/{id}` | 200 OK           | 404 Not Found |
| `POST`   | `/`     | 201 Created      | 400/422       |
| `PUT`    | `/{id}` | 200 OK           | 404 Not Found |
| `DELETE` | `/{id}` | 204 No Content   | 404 Not Found |

Hinweise:

- Validiere die Daten im Controller mit `@Valid` + `@RequestBody`
- Setze in der `ResponseEntity` korrekte HTTP-Status-Codes
- `id` und `status` sind serverseitig verwaltete Felder und sollten nicht aus
  dem Request-Body übernommen werden

#### Bonus: Pagination

Erweitere `GET /` um Pagination mit `Pageable`:

```bash
curl "http://localhost:8080/api/betriebsinhaber?page=0&size=5&sort=name,asc"
```

### Teil 2b: Fachliche Geschäftsregeln implementieren (Zusatzaufgabe)

Die Betriebsinhaber-Verwaltung ist jetzt rechtlich reguliert. Erweitere deine
`Betriebsinhaber`-Entity um ein Status-Feld (`ENTWURF`, `AKTIV`, `STILLGELEGT`) und
implementiere folgende Regeln in deinen `BetriebsinhaberService`:

1. Freigabe: Füge eine Methode `freigeben(id)` hinzu. Ein Betriebsinhaber darf
   nur freigegeben werden (`status = AKTIV`), wenn alle Adressdaten
   vorhanden sind und der Name nicht leer ist.
2. Flächen-Schutz: Wenn ein Betriebsinhaber bereits `AKTIV` ist, darf die
   `betriebsflaeche` bei einem Update nicht um mehr als 20% reduziert werden, ohne
   dass der Status automatisch zurück auf `ENTWURF` gesetzt wird.
3. Lösch-Schutz: Ein Betriebsinhaber im Status `AKTIV` darf nicht gelöscht
   werden. Er muss zuerst manuell auf `STILLGELEGT` gesetzt werden.

Ergänze dafür diese fachlichen Aktionen im Controller:

| HTTP   | Pfad              | Erfolg | Fehler        |
|--------|--------------------|--------|---------------|
| `POST` | `/{id}/freigeben`  | 200 OK | 404 / 422     |
| `POST` | `/{id}/stilllegen` | 200 OK | 404 Not Found |

Hinweis zur Modellierung:

- Die API aus Teil 2 verhindert durch Bean Validation bereits, dass
  unvollständige Betriebsinhaber regulär angelegt werden.
- Die `freigeben`-Regel ist trotzdem sinnvoll: Sie schützt gegen inkonsistente
  Bestandsdaten, Importe, technische Hintertüren oder spätere Änderungen.

#### Verifikation

```bash
# Erzeuge einen Betriebsinhaber
curl -X POST http://localhost:8080/api/betriebsinhaber \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Landwirtschaftsbetrieb Müller",
    "betriebsnummer": "DE-NRW-2026-0042",
    "strasse": "Am Feldrain 7",
    "plz": "50667",
    "ort": "Köln",
    "betriebsflaeche": 145.5,
    "foerdersumme": 48500
  }'
# → HTTP 201, JSON mit generierter ID

# Alle abfragen
curl http://localhost:8080/api/betriebsinhaber
# → HTTP 200, JSON-Array

# Einzelne abfragen
curl http://localhost:8080/api/betriebsinhaber/1
# → HTTP 200

# Aktualisieren
curl -X PUT http://localhost:8080/api/betriebsinhaber/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Landwirtschaftsbetrieb Müller (erweitert)",
    "betriebsnummer": "DE-NRW-2026-0042",
    "strasse": "Am Feldrain 7",
    "plz": "50667",
    "ort": "Köln",
    "betriebsflaeche": 160.0,
    "foerdersumme": 52000
  }'
# → HTTP 200

# Validation testen
curl -X POST http://localhost:8080/api/betriebsinhaber \
  -H "Content-Type: application/json" \
  -d '{"name": "", "betriebsnummer": "", "strasse": "", "plz": "", "ort": ""}'
# → HTTP 400, Validierungsfehler

# Freigeben
curl -X POST http://localhost:8080/api/betriebsinhaber/1/freigeben
# → HTTP 200, Status = AKTIV

# Fläche stark reduzieren
curl -X PUT http://localhost:8080/api/betriebsinhaber/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Landwirtschaftsbetrieb Müller (reduziert)",
    "betriebsnummer": "DE-NRW-2026-0042",
    "strasse": "Am Feldrain 7",
    "plz": "50667",
    "ort": "Köln",
    "betriebsflaeche": 80.0,
    "foerdersumme": 52000
  }'
# → HTTP 200, Status springt zurück auf ENTWURF

# Zurück in AKTIV setzen
curl -X POST http://localhost:8080/api/betriebsinhaber/1/freigeben
# → HTTP 200, Status = AKTIV

# Löschen eines aktiven Betriebsinhabers blockieren
curl -X DELETE http://localhost:8080/api/betriebsinhaber/1
# → HTTP 422

# Betriebsinhaber zuerst stilllegen
curl -X POST http://localhost:8080/api/betriebsinhaber/1/stilllegen
# → HTTP 200, Status = STILLGELEGT

# Jetzt löschen
curl -X DELETE http://localhost:8080/api/betriebsinhaber/1 -w "\n%{http_code}\n"
# → HTTP 204

# Freigabe-Regel gegen inkonsistente Bestandsdaten demonstrieren:
# 1. Einen zweiten gültigen Betriebsinhaber anlegen (z. B. ID 2)
# 2. In der H2-Console strasse oder name direkt auf '' setzen
# 3. Dann freigeben erneut aufrufen
curl -X POST http://localhost:8080/api/betriebsinhaber/2/freigeben
# → Erwartet: HTTP 422
```

## Teil 3: Spring Boot 4 Features ausprobieren

Erweitere deine CRUD-API um Features, die in Spring Boot 4 / Hibernate 7 neu
sind.

### 3a: Betriebsadresse als Embeddable Record

Hibernate 7 unterstützt Java Records als `@Embeddable`. Extrahiere die
Adressfelder (`strasse`, `plz`, `ort`) in einen eigenen Record:

```java
@Embeddable
public record Betriebsadresse(
    @NotBlank String strasse,
    @NotBlank String plz,
    @NotBlank String ort
) {}
```

Ersetze in der `Betriebsinhaber`-Entity die drei Einzelfelder durch ein eingebettetes
`Betriebsadresse`-Feld (`@NotNull @Valid @Embedded`). Passe Service und Controller
entsprechend an.

> Hinweis: Die JSON-Struktur ändert sich dadurch - die Adresse wird ein
> verschachteltes Objekt:
>
> ```json
> {
>   "name": "Landwirtschaftsbetrieb Müller",
>   "betriebsnummer": "DE-NRW-2026-0042",
>   "adresse": {
>     "strasse": "Am Feldrain 7",
>     "plz": "50667",
>     "ort": "Köln"
>   },
>   "betriebsflaeche": 145.5,
>   "foerdersumme": 48500
> }
> ```

### 3b: Soft Delete mit @SoftDelete

Hibernate 7 bietet `@SoftDelete` für logisches Löschen ohne eigene
Implementierung. Füge die Annotation zur `Betriebsinhaber`-Entity hinzu:

```java
import org.hibernate.annotations.SoftDelete;

@Entity
@Table(name = "betriebsinhaber")
@SoftDelete
public class Betriebsinhaber { ... }
```

Verifiziere:

1. Erstelle einen Betriebsinhaber und lösche ihn per `DELETE /api/betriebsinhaber/{id}`
2. Prüfe in der H2-Console (`http://localhost:8080/h2-console`):
   der Datensatz existiert noch, hat aber eine `deleted`-Spalte mit Wert `true`
3. `GET /api/betriebsinhaber` gibt den gelöschten Eintrag nicht mehr zurück

H2-Login:

- JDBC URL: `jdbc:h2:mem:foerderung`
- User Name: `sa`
- Password: leer

### 3c: ProblemDetail für Fehlerantworten

Spring Boot 4 unterstützt RFC 9457 (Problem Details) nativ.

1. Aktiviere ProblemDetail in der `application.yml`:

   ```yaml
   spring:
     mvc:
       problemdetail:
         enabled: true
   ```

2. Erstelle eine `BetriebsinhaberNotFoundException extends RuntimeException` mit
   einem `betriebsinhaberId`-Feld.

3. Erstelle einen `@RestControllerAdvice` mit `@ExceptionHandler`, der ein
   `ProblemDetail`-Objekt zurückgibt (siehe Modul-02-Slides).

4. Passe den Controller an: Statt `ResponseEntity.notFound().build()` wird
   jetzt die Exception geworfen (z.B. per `.orElseThrow()`).

Erwartete Antwort bei `GET /api/betriebsinhaber/999`:

```json
{
  "type": "about:blank",
  "title": "Betriebsinhaber nicht gefunden",
  "status": 404,
  "detail": "Kein Betriebsinhaber mit ID 999 vorhanden",
  "instance": "/api/betriebsinhaber/999",
  "betriebsinhaberId": 999
}
```

### 3d: Virtual Threads und strukturiertes Logging

Ergänze in der `application.yml`:

```yaml
spring:
  threads:
    virtual:
      enabled: true

logging:
  structured:
    format:
      console: ecs
```

Starte die Anwendung neu und beobachte:

- Virtual Threads: Die Log-Ausgabe zeigt Thread-Namen wie
  `tomcat-handler-0` (Virtual Thread) statt `http-nio-8080-exec-1`
  (Platform Thread).
- Strukturiertes Logging: Die Konsolenausgabe ist jetzt im JSON-Format
  (Elastic Common Schema).
