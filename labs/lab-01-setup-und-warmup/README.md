# Lab 01: Setup und Warmup

## Lernziel

Entwicklungsumgebung prüfen und mit einer CRUD-API für Immobilien starten. 
Dieses Lab stellt sicher, dass alle technisch startklar sind und das
Projekt-Setup verstanden ist.

## Dauer

30 Minuten

## Voraussetzungen

- JDK 17+ installiert (`java -version`)
- Maven 3.9+ installiert (`mvn -version`)
- IDE mit Spring-Boot-Unterstützung (IntelliJ IDEA empfohlen)

## Teil 1: Projekt starten (10 Min)

Importiere das Projekt aus `initial-project/` in deine IDE und verifiziere:

```bash
cd initial-project
mvn clean verify
mvn spring-boot:run
```

Health-Check:

```bash
curl http://localhost:8080/actuator/health
# Erwartete Antwort: {"status":"UP"}
```

> Falls der Build fehlschlägt: JDK- und Maven-Version prüfen. IntelliJ ggf.
> `Maven > Reload Project` ausführen.

## Teil 2: Immobilien-CRUD implementieren

Implementiere eine vollständige CRUD-API für Immobilien im Package
`de.realestate.property`. Die Aufgabe ist bewusst ohne Schritt-für-Schritt-
Anleitung gehalten, da ihr Spring Boot schon kennt. Scheut euch aber nicht, 
Fragen zu stellen, falls etwas nicht funktioniert.

### Was zu bauen ist

**Entity `Property`** (`@Entity`, `@Table(name = "properties")`):

| Feld            | Typ          | Constraints                         |
|-----------------|--------------|-------------------------------------|
| `id`            | `Long`       | `@Id`, `@GeneratedValue(IDENTITY)`  |
| `title`         | `String`     | `@NotBlank`                         |
| `street`        | `String`     | `@NotBlank`                         |
| `postalCode`    | `String`     | `@NotBlank`                         |
| `city`          | `String`     | `@NotBlank`                         |
| `livingArea`    | `BigDecimal` | optional                            |
| `purchasePrice` | `BigDecimal` | optional                            |

**Repository:** `PropertyRepository extends JpaRepository<Property, Long>`

**Service:** `PropertyService` mit `@Service`, Constructor Injection, CRUD-
Methoden (`findAll`, `findById`, `save`, `update`, `delete`)

**Controller:** `PropertyController` unter `@RequestMapping("/api/properties")`

| HTTP     | Pfad    | Erfolg           | Fehler        |
|----------|---------|------------------|---------------|
| `GET`    | `/`     | 200 OK           | –             |
| `GET`    | `/{id}` | 200 OK           | 404 Not Found |
| `POST`   | `/`     | 201 Created      | 400/422       |
| `PUT`    | `/{id}` | 200 OK           | 404 Not Found |
| `DELETE` | `/{id}` | 204 No Content   | 404 Not Found |

Hinweise:

- Validiere die Daten im Controller mir `@Valid` + `@RequestBody`
- Setze in der `ResponseEntity` korrekte HTTP-Status-Codes

### Bonus: Pagination

Erweitere `GET /` um Pagination mit `Pageable`:

```bash
curl "http://localhost:8080/api/properties?page=0&size=5&sort=title,asc"
```

## Verifikation

```bash
# Immobilie erstellen
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

# Löschen
curl -X DELETE http://localhost:8080/api/properties/1 -w "\n%{http_code}\n"
# → HTTP 204

# Validation testen
curl -X POST http://localhost:8080/api/properties \
  -H "Content-Type: application/json" \
  -d '{"title": "", "street": "", "postalCode": "", "city": ""}'
# → HTTP 400, Validierungsfehler
```

## Tipps

- **H2-Console**:
  Unter [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
  erreichbar. JDBC-URL: `jdbc:h2:mem:realestate`, Benutzer: `sa`, Passwort
  leer.
- **Fehlersuche**: Hibernate zeigt generierte SQL-Statements in der Konsole
  an (`spring.jpa.show-sql: true`).

## Diskussionspunkt

> Schaut euch die entstandene Schichtarchitektur an: Controller → Service →
> Repository → Entity. Wo liegen die Grenzen dieses Ansatzes, wenn die
> Geschäftslogik wächst? Diese Frage greifen wir in den nächsten Modulen auf.
