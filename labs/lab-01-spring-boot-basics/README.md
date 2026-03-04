# Lab 01: Spring Boot Basics - Immobilien-CRUD

## Lernziel

Grundlegende Spring Boot 3 Konzepte anwenden: Entity, Repository, Service und Controller implementieren und zu einer funktionierenden CRUD-API zusammenfügen.

## Dauer

45 Minuten

## Voraussetzungen

- Lab 00 abgeschlossen
- Starter-Projekt läuft erfolgreich (`mvn spring-boot:run`)
- Health-Check gibt `{"status":"UP"}` zurück

## Aufgabe

Erstelle eine vollständige CRUD-API für Immobilien. Dazu implementierst du die folgenden Klassen im Package `de.immobiliencrm.immobilie`:

### 1. Entity: `Immobilie`

Erstelle eine JPA-Entity `Immobilie` mit folgenden Feldern:

| Feld | Typ | Constraints |
|---|---|---|
| `id` | `Long` | Primärschluessel, automatisch generiert |
| `bezeichnung` | `String` | Pflichtfeld (`@NotBlank`) |
| `strasse` | `String` | Pflichtfeld (`@NotBlank`) |
| `plz` | `String` | Pflichtfeld (`@NotBlank`) |
| `ort` | `String` | Pflichtfeld (`@NotBlank`) |
| `wohnfläche` | `BigDecimal` | optional |
| `kaufpreis` | `BigDecimal` | optional |

**Hinweise:**

- Verwende `@Entity` und `@Id` aus `jakarta.persistence.*`
- Verwende `@GeneratedValue(strategy = GenerationType.IDENTITY)` für die ID
- Verwende `@NotBlank` aus `jakarta.validation.constraints.*`
- JPA benötigt einen parameterlosen Konstruktor
- Erstelle Getter und Setter für alle Felder

### 2. Repository: `ImmobilieRepository`

Erstelle ein Interface, das `JpaRepository<Immobilie, Long>` erweitert.

**Hinweis:** Spring Data JPA stellt automatisch eine Implementierung bereit - du musst nur das Interface definieren.

### 3. Service: `ImmobilieService`

Erstelle eine Service-Klasse mit `@Service` und folgenden Methoden:

| Methode | Beschreibung |
|---|---|
| `List<Immobilie> findeAlle()` | Alle Immobilien zurückgeben |
| `Optional<Immobilie> findePerId(Long id)` | Immobilie nach ID suchen |
| `Immobilie speichern(Immobilie immobilie)` | Neue Immobilie speichern |
| `Optional<Immobilie> aktualisieren(Long id, Immobilie immobilie)` | Bestehende Immobilie aktualisieren |
| `boolean löschen(Long id)` | Immobilie löschen, gibt `true` zurück wenn gefunden |

**Hinweise:**

- Verwende Constructor Injection für das Repository (kein `@Autowired` auf Feldern)
- Die `aktualisieren`-Methode soll prüfen, ob die Immobilie existiert, und dann die Felder übernehmen

### 4. Controller: `ImmobilieController`

Erstelle einen REST-Controller mit `@RestController` und `@RequestMapping("/api/immobilien")`:

| HTTP-Methode | Pfad | Beschreibung | Status-Code |
|---|---|---|---|
| `GET` | `/` | Alle Immobilien auflisten | 200 OK |
| `GET` | `/{id}` | Einzelne Immobilie abfragen | 200 OK / 404 Not Found |
| `POST` | `/` | Neue Immobilie erstellen | 201 Created |
| `PUT` | `/{id}` | Immobilie aktualisieren | 200 OK / 404 Not Found |
| `DELETE` | `/{id}` | Immobilie löschen | 204 No Content / 404 Not Found |

**Hinweise:**

- Verwende `@Valid` zusammen mit `@RequestBody`, damit die Bean Validation greift
- Verwende `ResponseEntity` für korrekte HTTP-Status-Codes
- Verwende `ResponseEntity.status(HttpStatus.CREATED).body(...)` für POST

## Schritt-für-Schritt-Anleitung

### Schritt 1: Package anlegen

Erstelle das Package `de.immobiliencrm.immobilie` im Verzeichnis `src/main/java/`.

### Schritt 2: Entity erstellen

Erstelle die Klasse `Immobilie.java`:

```java
@Entity
public class Immobilie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String bezeichnung;

    // ... weitere Felder

    // Parameterloser Konstruktor (für JPA)
    public Immobilie() {
    }

    // Getter und Setter
}
```

### Schritt 3: Repository erstellen

Erstelle das Interface `ImmobilieRepository.java`:

```java
public interface ImmobilieRepository extends JpaRepository<Immobilie, Long> {
}
```

Das reicht bereits - Spring Data JPA generiert die Implementierung automatisch.

### Schritt 4: Service erstellen

Erstelle die Klasse `ImmobilieService.java`:

```java
@Service
public class ImmobilieService {

    private final ImmobilieRepository repository;

    public ImmobilieService(ImmobilieRepository repository) {
        this.repository = repository;
    }

    // CRUD-Methoden implementieren
}
```

**Tipp für `aktualisieren`:** Lade die bestehende Entity per `findById`, übernimm die neuen Werte und speichere erneut.

### Schritt 5: Controller erstellen

Erstelle die Klasse `ImmobilieController.java`:

```java
@RestController
@RequestMapping("/api/immobilien")
public class ImmobilieController {

    private final ImmobilieService service;

    // Constructor Injection

    @PostMapping
    public ResponseEntity<Immobilie> erstellen(@Valid @RequestBody Immobilie immobilie) {
        Immobilie gespeichert = service.speichern(immobilie);
        return ResponseEntity.status(HttpStatus.CREATED).body(gespeichert);
    }

    // ... weitere Endpunkte
}
```

### Schritt 6: Testen

Starte die Anwendung und teste die Endpunkte (siehe Verifikation).

## Bonus: Pagination

Erweitere den `GET /`-Endpunkt um Pagination:

```java
@GetMapping
public Page<Immobilie> alleAuflisten(Pageable pageable) {
    return service.findeAlle(pageable);
}
```

Dazu muss auch der Service und das Repository angepasst werden. Spring Data JPA unterstützt `Pageable` bereits nativ.

Aufruf mit Paginierung:

```bash
curl "http://localhost:8080/api/immobilien?page=0&size=5&sort=bezeichnung,asc"
```

## Verifikation

Starte die Anwendung und führe folgende curl-Befehle aus:

### Immobilie erstellen

```bash
curl -X POST http://localhost:8080/api/immobilien \
  -H "Content-Type: application/json" \
  -d '{
    "bezeichnung": "Einfamilienhaus am Stadtpark",
    "strasse": "Parkstrasse 42",
    "plz": "50667",
    "ort": "Köln",
    "wohnfläche": 145.5,
    "kaufpreis": 485000
  }'
```

Erwartete Antwort: HTTP 201, JSON mit generierter `id`.

### Alle Immobilien abfragen

```bash
curl http://localhost:8080/api/immobilien
```

Erwartete Antwort: HTTP 200, JSON-Array mit der erstellten Immobilie.

### Einzelne Immobilie abfragen

```bash
curl http://localhost:8080/api/immobilien/1
```

Erwartete Antwort: HTTP 200, JSON-Objekt der Immobilie.

### Immobilie aktualisieren

```bash
curl -X PUT http://localhost:8080/api/immobilien/1 \
  -H "Content-Type: application/json" \
  -d '{
    "bezeichnung": "Einfamilienhaus am Stadtpark (renoviert)",
    "strasse": "Parkstrasse 42",
    "plz": "50667",
    "ort": "Köln",
    "wohnfläche": 155.0,
    "kaufpreis": 525000
  }'
```

Erwartete Antwort: HTTP 200, aktualisiertes JSON-Objekt.

### Immobilie löschen

```bash
curl -X DELETE http://localhost:8080/api/immobilien/1 -w "\n%{http_code}\n"
```

Erwartete Antwort: HTTP 204, kein Body.

### Validation testen

```bash
curl -X POST http://localhost:8080/api/immobilien \
  -H "Content-Type: application/json" \
  -d '{"bezeichnung": "", "strasse": "", "plz": "", "ort": ""}'
```

Erwartete Antwort: HTTP 400, Validierungsfehler.

## Hinweise

- **H2-Console**: Unter [http://localhost:8080/h2-console](http://localhost:8080/h2-console) kannst du die Datenbank direkt einsehen. JDBC-URL: `jdbc:h2:mem:immobiliencrm`, Benutzer: `sa`, Passwort leer.
- **Spring DevTools**: Falls du `spring-boot-devtools` als Abhängigkeit hinzufügst, startet die Anwendung automatisch bei Code-Änderungen neu.
- **Fehlersuche**: Achte auf die Konsolen-Ausgabe - Hibernate zeigt die generierten SQL-Statements an (`spring.jpa.show-sql: true`).
