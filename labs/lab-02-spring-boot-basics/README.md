# Lab 02: Spring Boot Basics - Immobilien-CRUD

## Lernziel

Grundlegende Spring Boot 4 Konzepte anwenden: Entity, Repository, Service und Controller implementieren und zu einer funktionierenden CRUD-API zusammenfuegen.

## Dauer

45 Minuten

## Voraussetzungen

- Lab 01 abgeschlossen
- Starter-Projekt laeuft erfolgreich (`mvn spring-boot:run`)
- Health-Check gibt `{"status":"UP"}` zurueck

## Aufgabe

Erstelle eine vollstaendige CRUD-API fuer Immobilien. Dazu implementierst du die folgenden Klassen im Package `de.realestate.property`:

### 1. Entity: `Property`

Erstelle eine JPA-Entity `Property` mit folgenden Feldern:

| Feld | Typ | Constraints |
|---|---|---|
| `id` | `Long` | Primaerschluessel, automatisch generiert |
| `title` | `String` | Pflichtfeld (`@NotBlank`) |
| `street` | `String` | Pflichtfeld (`@NotBlank`) |
| `postalCode` | `String` | Pflichtfeld (`@NotBlank`) |
| `city` | `String` | Pflichtfeld (`@NotBlank`) |
| `livingArea` | `BigDecimal` | optional |
| `purchasePrice` | `BigDecimal` | optional |

**Hinweise:**

- Verwende `@Entity` und `@Id` aus `jakarta.persistence.*`
- Verwende `@GeneratedValue(strategy = GenerationType.IDENTITY)` fuer die ID
- Verwende `@NotBlank` aus `jakarta.validation.constraints.*`
- JPA benoetigt einen parameterlosen Konstruktor
- Erstelle Getter und Setter fuer alle Felder

### 2. Repository: `PropertyRepository`

Erstelle ein Interface, das `JpaRepository<Property, Long>` erweitert.

**Hinweis:** Spring Data JPA stellt automatisch eine Implementierung bereit - du musst nur das Interface definieren.

### 3. Service: `PropertyService`

Erstelle eine Service-Klasse mit `@Service` und folgenden Methoden:

| Methode | Beschreibung |
|---|---|
| `List<Property> findAll()` | Alle Immobilien zurueckgeben |
| `Optional<Property> findById(Long id)` | Immobilie nach ID suchen |
| `Property save(Property property)` | Neue Immobilie speichern |
| `Optional<Property> update(Long id, Property property)` | Bestehende Immobilie aktualisieren |
| `boolean delete(Long id)` | Immobilie loeschen, gibt `true` zurueck wenn gefunden |

**Hinweise:**

- Verwende Constructor Injection fuer das Repository (kein `@Autowired` auf Feldern)
- Die `update`-Methode soll pruefen, ob die Immobilie existiert, und dann die Felder uebernehmen

### 4. Controller: `PropertyController`

Erstelle einen REST-Controller mit `@RestController` und `@RequestMapping("/api/properties")`:

| HTTP-Methode | Pfad | Beschreibung | Status-Code |
|---|---|---|---|
| `GET` | `/` | Alle Immobilien auflisten | 200 OK |
| `GET` | `/{id}` | Einzelne Immobilie abfragen | 200 OK / 404 Not Found |
| `POST` | `/` | Neue Immobilie erstellen | 201 Created |
| `PUT` | `/{id}` | Immobilie aktualisieren | 200 OK / 404 Not Found |
| `DELETE` | `/{id}` | Immobilie loeschen | 204 No Content / 404 Not Found |

**Hinweise:**

- Verwende `@Valid` zusammen mit `@RequestBody`, damit die Bean Validation greift
- Verwende `ResponseEntity` fuer korrekte HTTP-Status-Codes
- Verwende `ResponseEntity.status(HttpStatus.CREATED).body(...)` fuer POST

## Schritt-fuer-Schritt-Anleitung

### Schritt 1: Package anlegen

Erstelle das Package `de.realestate.property` im Verzeichnis `src/main/java/`.

### Schritt 2: Entity erstellen

Erstelle die Klasse `Property.java`:

```java
@Entity
@Table(name = "properties")
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String title;

    // ... further fields

    // No-args constructor (required by JPA)
    public Property() {
    }

    // Getters and setters
}
```

### Schritt 3: Repository erstellen

Erstelle das Interface `PropertyRepository.java`:

```java
public interface PropertyRepository extends JpaRepository<Property, Long> {
}
```

Das reicht bereits - Spring Data JPA generiert die Implementierung automatisch.

### Schritt 4: Service erstellen

Erstelle die Klasse `PropertyService.java`:

```java
@Service
public class PropertyService {

    private final PropertyRepository repository;

    public PropertyService(PropertyRepository repository) {
        this.repository = repository;
    }

    // Implement CRUD methods
}
```

**Tipp fuer `update`:** Lade die bestehende Entity per `findById`, uebernimm die neuen Werte und speichere erneut.

### Schritt 5: Controller erstellen

Erstelle die Klasse `PropertyController.java`:

```java
@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    private final PropertyService service;

    // Constructor Injection

    @PostMapping
    public ResponseEntity<Property> create(@Valid @RequestBody Property property) {
        Property saved = service.save(property);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ... further endpoints
}
```

### Schritt 6: Testen

Starte die Anwendung und teste die Endpunkte (siehe Verifikation).

## Bonus: Pagination

Erweitere den `GET /`-Endpunkt um Pagination:

```java
@GetMapping
public Page<Property> findAll(Pageable pageable) {
    return service.findAll(pageable);
}
```

Dazu muss auch der Service und das Repository angepasst werden. Spring Data JPA unterstuetzt `Pageable` bereits nativ.

Aufruf mit Paginierung:

```bash
curl "http://localhost:8080/api/properties?page=0&size=5&sort=title,asc"
```

## Verifikation

Starte die Anwendung und fuehre folgende curl-Befehle aus:

### Immobilie erstellen

```bash
curl -X POST http://localhost:8080/api/properties \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Einfamilienhaus am Stadtpark",
    "street": "Parkstrasse 42",
    "postalCode": "50667",
    "city": "Koeln",
    "livingArea": 145.5,
    "purchasePrice": 485000
  }'
```

Erwartete Antwort: HTTP 201, JSON mit generierter `id`.

### Alle Immobilien abfragen

```bash
curl http://localhost:8080/api/properties
```

Erwartete Antwort: HTTP 200, JSON-Array mit der erstellten Immobilie.

### Einzelne Immobilie abfragen

```bash
curl http://localhost:8080/api/properties/1
```

Erwartete Antwort: HTTP 200, JSON-Objekt der Immobilie.

### Immobilie aktualisieren

```bash
curl -X PUT http://localhost:8080/api/properties/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Einfamilienhaus am Stadtpark (renoviert)",
    "street": "Parkstrasse 42",
    "postalCode": "50667",
    "city": "Koeln",
    "livingArea": 155.0,
    "purchasePrice": 525000
  }'
```

Erwartete Antwort: HTTP 200, aktualisiertes JSON-Objekt.

### Immobilie loeschen

```bash
curl -X DELETE http://localhost:8080/api/properties/1 -w "\n%{http_code}\n"
```

Erwartete Antwort: HTTP 204, kein Body.

### Validation testen

```bash
curl -X POST http://localhost:8080/api/properties \
  -H "Content-Type: application/json" \
  -d '{"title": "", "street": "", "postalCode": "", "city": ""}'
```

Erwartete Antwort: HTTP 400, Validierungsfehler.

## Hinweise

- **H2-Console**: Unter [http://localhost:8080/h2-console](http://localhost:8080/h2-console) kannst du die Datenbank direkt einsehen. JDBC-URL: `jdbc:h2:mem:realestate`, Benutzer: `sa`, Passwort leer.
- **Spring DevTools**: Falls du `spring-boot-devtools` als Abhaengigkeit hinzufuegst, startet die Anwendung automatisch bei Code-Aenderungen neu.
- **Fehlersuche**: Achte auf die Konsolen-Ausgabe - Hibernate zeigt die generierten SQL-Statements an (`spring.jpa.show-sql: true`).
