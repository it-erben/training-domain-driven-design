# Lab 01: Setup und Warmup

## Teil 1: Projekt starten

Importiere das Projekt aus `initial-project/` in deine IDE.

## Teil 2: Immobilien-CRUD implementieren

Implementiere eine CRUD-API für Immobilien im Package
`de.realestate.property`. Die Aufgabe ist bewusst ohne Schritt-für-Schritt-
Anleitung gehalten, da ihr Spring Boot teilweise schon kennt. 
Scheut euch aber nicht,
Fragen zu stellen, falls etwas nicht funktioniert oder es für euch neu ist.

### Was zu bauen ist

Entity `Property` (`@Entity`, `@Table(name = "properties")`):

| Feld            | Typ          | Constraints                         |
|-----------------|--------------|-------------------------------------|
| `id`            | `Long`       | `@Id`, `@GeneratedValue(IDENTITY)`  |
| `title`         | `String`     | `@NotBlank`                         |
| `street`        | `String`     | `@NotBlank`                         |
| `postalCode`    | `String`     | `@NotBlank`                         |
| `city`          | `String`     | `@NotBlank`                         |
| `livingArea`    | `BigDecimal` | optional                            |
| `purchasePrice` | `BigDecimal` | optional                            |

Repository: `PropertyRepository extends JpaRepository<Property, Long>`

Service: `PropertyService` mit `@Service`, Constructor Injection, CRUD-
Methoden (`findAll`, `findById`, `save`, `update`, `delete`)

Controller: `PropertyController` unter `@RequestMapping("/api/properties")`

Die Pfade oben sind relativ zu diesem Basis-Pfad gemeint. In Spring
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
curl "http://localhost:8080/api/properties?page=0&size=5&sort=title,asc"
```

### Teil 2b: Fachliche Geschäftsregeln implementieren (Zusatzaufgabe)

Deine Immobilienverwaltung ist jetzt rechtlich reguliert. Erweitere deine
`Property`-Entity um ein Status-Feld (`DRAFT`, `ACTIVE`, `RETIRED`) und
implementiere folgende Regeln in deinen `PropertyService`:

1. Aktivierung: Füge eine Methode `publish(id)` hinzu. Eine Immobilie darf
   nur veröffentlicht werden (`status = ACTIVE`), wenn alle Adressdaten
   vorhanden sind und der Titel nicht leer ist.
2. Preis-Schutz: Wenn eine Immobilie bereits `ACTIVE` ist, darf der
   `purchasePrice` bei einem Update nicht um mehr als 20% gesenkt werden, ohne
   dass der Status automatisch zurück auf `DRAFT` gesetzt wird.
3. Lösch-Schutz: Eine Immobilie im Status `ACTIVE` darf nicht gelöscht
   werden. Sie muss zuerst manuell auf `RETIRED` gesetzt werden.

Ergänze dafür diese fachlichen Aktionen im Controller:

| HTTP   | Pfad             | Erfolg | Fehler        |
|--------|------------------|--------|---------------|
| `POST` | `/{id}/publish`  | 200 OK | 404 / 422     |
| `POST` | `/{id}/retire`   | 200 OK | 404 Not Found |

Hinweis zur Modellierung:

- Die API aus Teil 2 verhindert durch Bean Validation bereits, dass
  unvollständige Immobilien regulär angelegt werden.
- Die `publish`-Regel ist trotzdem sinnvoll: Sie schützt gegen inkonsistente
  Bestandsdaten, Importe, technische Hintertüren oder spätere Änderungen.

#### Verifikation

```bash
# Erzeuge eine Immobilie
curl -X POST http://localhost:8080/api/properties \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Einfamilienhaus am Stadtpark",
    "street": "Parkstraße 42",
    "postalCode": "50667",
    "city": "Köln",
    "livingArea": 145.5,
    "purchasePrice": 485000
  }'
# → HTTP 201, JSON mit generierter ID

# Alle abfragen
curl http://localhost:8080/api/properties
# → HTTP 200, JSON-Array

# Einzelne abfragen
curl http://localhost:8080/api/properties/1
# → HTTP 200

# Aktualisieren
curl -X PUT http://localhost:8080/api/properties/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Einfamilienhaus am Stadtpark (renoviert)",
    "street": "Parkstraße 42",
    "postalCode": "50667",
    "city": "Köln",
    "livingArea": 155.0,
    "purchasePrice": 525000
  }'
# → HTTP 200

# Validation testen
curl -X POST http://localhost:8080/api/properties \
  -H "Content-Type: application/json" \
  -d '{"title": "", "street": "", "postalCode": "", "city": ""}'
# → HTTP 400, Validierungsfehler

# Veröffentlichen
curl -X POST http://localhost:8080/api/properties/1/publish
# → HTTP 200, Status = ACTIVE

# Preis stark senken
curl -X PUT http://localhost:8080/api/properties/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Einfamilienhaus am Stadtpark (Preis reduziert)",
    "street": "Parkstraße 42",
    "postalCode": "50667",
    "city": "Köln",
    "livingArea": 155.0,
    "purchasePrice": 350000
  }'
# → HTTP 200, Status springt zurück auf DRAFT

# Zurück in ACTIVE setzen
curl -X POST http://localhost:8080/api/properties/1/publish
# → HTTP 200, Status = ACTIVE

# Löschen einer aktiven Immobilie blockieren
curl -X DELETE http://localhost:8080/api/properties/1
# → HTTP 422

# Immobilie zuerst in RETIRED überführen
curl -X POST http://localhost:8080/api/properties/1/retire
# → HTTP 200, Status = RETIRED

# Jetzt löschen
curl -X DELETE http://localhost:8080/api/properties/1 -w "\n%{http_code}\n"
# → HTTP 204

# Publish-Regel gegen inkonsistente Bestandsdaten demonstrieren:
# 1. Eine zweite gültige Immobilie anlegen (z. B. ID 2)
# 2. In der H2-Console street oder title dieser Immobilie direkt auf '' setzen
# 3. Dann publish erneut aufrufen
curl -X POST http://localhost:8080/api/properties/2/publish
# → Erwartet: HTTP 422
```

## Teil 3: Spring Boot 4 Features ausprobieren

Erweitere deine CRUD-API um Features, die in Spring Boot 4 / Hibernate 7 neu
sind.

### 3a: Address als Embeddable Record

Hibernate 7 unterstützt Java Records als `@Embeddable`. Extrahiere die
Adressfelder (`street`, `postalCode`, `city`) in einen eigenen Record:

```java
@Embeddable
public record Address(
    @NotBlank String street,
    @NotBlank String postalCode,
    @NotBlank String city
) {}
```

Ersetze in der `Property`-Entity die drei Einzelfelder durch ein eingebettetes
`Address`-Feld (`@NotNull @Valid @Embedded`). Passe Service und Controller
entsprechend an.

> Hinweis: Die JSON-Struktur ändert sich dadurch - die Adresse wird ein
> verschachteltes Objekt:
>
> ```json
> {
>   "title": "Einfamilienhaus am Stadtpark",
>   "address": {
>     "street": "Parkstraße 42",
>     "postalCode": "50667",
>     "city": "Köln"
>   },
>   "livingArea": 145.5,
>   "purchasePrice": 485000
> }
> ```

### 3b: Soft Delete mit @SoftDelete

Hibernate 7 bietet `@SoftDelete` für logisches Löschen ohne eigene
Implementierung. Füge die Annotation zur `Property`-Entity hinzu:

```java
import org.hibernate.annotations.SoftDelete;

@Entity
@Table(name = "properties")
@SoftDelete
public class Property { ... }
```

Verifiziere:

1. Erstelle eine Immobilie und lösche sie per `DELETE /api/properties/{id}`
2. Prüfe in der H2-Console (`http://localhost:8080/h2-console`):
   der Datensatz existiert noch, hat aber eine `deleted`-Spalte mit Wert `true`
3. `GET /api/properties` gibt die gelöschte Immobilie nicht mehr zurück

H2-Login:

- JDBC URL: `jdbc:h2:mem:realestate`
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

2. Erstelle eine `PropertyNotFoundException extends RuntimeException` mit
   einem `propertyId`-Feld.

3. Erstelle einen `@RestControllerAdvice` mit `@ExceptionHandler`, der ein
   `ProblemDetail`-Objekt zurückgibt (siehe Modul-02-Slides).

4. Passe den Controller an: Statt `ResponseEntity.notFound().build()` wird
   jetzt die Exception geworfen (z.B. per `.orElseThrow()`).

Erwartete Antwort bei `GET /api/properties/999`:

```json
{
  "type": "about:blank",
  "title": "Property not found",
  "status": 404,
  "detail": "No property with ID 999 exists",
  "instance": "/api/properties/999",
  "propertyId": 999
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
