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

# Modul 01 – Spring Boot 3 Basics

**Geschätzte Dauer: 90 Minuten**

### Lernziele

- Die wichtigsten Neuerungen von Spring Boot 3 kennen
- Auto-Configuration und den Spring Application Context verstehen
- Dependency Injection mit Constructor Injection anwenden
- Spring Data JPA für einfache Persistenz nutzen
- Bean Validation zur Eingabeprüfung einsetzen
- REST-Endpoints mit Spring Web MVC erstellen

---

## Warum Spring Boot?

### Das Problem ohne Spring Boot

- Viel Boilerplate für Konfiguration (XML, Java Config)
- Abhängigkeitsversionen manuell koordinieren
- Server separat aufsetzen und deployen

### Spring Boot löst das durch

- **Opinionated Defaults** – sinnvolle Vorkonfiguration
- **Starter-Dependencies** – kuratierte Dependency-Sets
- **Embedded Server** – Tomcat/Jetty eingebaut
- **Auto-Configuration** – Beans automatisch basierend auf Classpath
- **Actuator** – Health Checks, Metriken, Monitoring

---

## Spring Boot 3 – Was ist neu?

### Die drei großen Änderungen

| Änderung | Detail |
|----------|--------|
| **Jakarta EE 10** | `javax.*` → `jakarta.*` (Persistence, Validation, Servlet) |
| **Java 17+ Baseline** | Records, Sealed Classes, Text Blocks, Pattern Matching |
| **GraalVM Native Image** | Ahead-of-Time Compilation für Startup < 100 ms |

### Weitere Highlights

- **Micrometer Observability API** – einheitliches Tracing und Metrics
- **Problem Details (RFC 9457)** – standardisiertes Fehlerformat
- **Verbesserte Docker-Image-Erstellung** – Cloud Native Buildpacks
- **Virtual Threads** (ab Spring Boot 3.2) – Project Loom Support

---

## Jakarta EE 10 – Namespace-Migration

### Vorher (Spring Boot 2.x)

```java
import javax.persistence.Entity;
import javax.validation.constraints.NotBlank;
import javax.servlet.http.HttpServletRequest;
```

### Nachher (Spring Boot 3.x)

```java
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.servlet.http.HttpServletRequest;
```

> **Tipp:** IntelliJ IDEA bietet automatisierte Migration an:
> *Refactor → Migrate Packages and Classes*

---

## Auto-Configuration – Die Magie hinter Spring Boot

### Wie funktioniert es?

1. Spring Boot scannt den **Classpath** nach verfügbaren Libraries
2. Für jede Library existiert eine `@AutoConfiguration`-Klasse
3. Diese registriert **Beans**, wenn bestimmte Bedingungen erfüllt sind

### Beispiel: H2 im Classpath

```
spring-boot-starter-data-jpa  →  DataSource, EntityManagerFactory
h2 (runtime)                  →  H2 DataSource (jdbc:h2:mem:...)
```

### Wichtige Conditional-Annotationen

| Annotation | Wirkt wenn… |
|-----------|-------------|
| `@ConditionalOnClass` | Klasse ist im Classpath |
| `@ConditionalOnMissingBean` | Kein eigener Bean definiert |
| `@ConditionalOnProperty` | Property hat bestimmten Wert |

> Eigene Beans überschreiben Auto-Configuration – **Convention over Configuration**.

---

## Spring Application Context

### Der IoC Container

```
┌─────────────────────────────────────────────────┐
│              Application Context                │
│                                                 │
│  ┌──────────────┐  ┌──────────────────────┐     │
│  │ @Service     │  │ @Repository          │     │
│  │ Immobilien   │──│ ImmobilienRepository │     │
│  │ Service      │  │                      │     │
│  └──────────────┘  └──────────────────────┘     │
│         │                     │                 │
│         ▼                     ▼                 │
│  ┌──────────────┐  ┌──────────────────────┐     │
│  │ @Controller  │  │ Auto-Configured      │     │
│  │ Immobilien   │  │ DataSource,          │     │
│  │ Controller   │  │ EntityManagerFactory │     │
│  └──────────────┘  └──────────────────────┘     │
└─────────────────────────────────────────────────┘
```

- Spring verwaltet Objekte als **Beans** im Application Context
- Abhängigkeiten werden automatisch aufgelöst (**Inversion of Control**)
- Default-Scope: **Singleton** – eine Instanz pro Bean

---

## Stereotyp-Annotationen

### Beans im Context registrieren

| Annotation | Semantik | Typischer Einsatz |
|-----------|---------|-------------------|
| `@Component` | Allgemeine Bean | Hilfsklassen, Mapper |
| `@Service` | Geschäftslogik | Business-Services |
| `@Repository` | Datenzugriff | DAO, Repository-Adapter |
| `@Controller` | Web-Controller | MVC Controller |
| `@RestController` | REST-Controller | `@Controller` + `@ResponseBody` |
| `@Configuration` | Konfigurations-Klasse | Bean-Factory-Methoden |

### Classpath Scanning

```java
@SpringBootApplication  // enthält @ComponentScan
public class ImmobilienCrmApplication {
    public static void main(String[] args) {
        SpringApplication.run(ImmobilienCrmApplication.class, args);
    }
}
```

> `@SpringBootApplication` = `@Configuration` + `@EnableAutoConfiguration` + `@ComponentScan`

---

## Constructor Injection – Best Practice

```java
@Service
public class ImmobilienService {

    private final ImmobilienRepository repository;
    private final BewertungsService bewertungsService;

    // Bei einem Konstruktor ist @Autowired optional
    public ImmobilienService(ImmobilienRepository repository,
                             BewertungsService bewertungsService) {
        this.repository = repository;
        this.bewertungsService = bewertungsService;
    }
}
```

### Warum Constructor Injection?

- Felder sind `final` → **unveränderlich** nach Konstruktion
- **Pflichtabhängigkeiten** sind sofort sichtbar
- Einfach **testbar**: Abhängigkeiten direkt im Test übergeben
- Kein Reflection nötig (anders als `@Autowired` auf Feldern)

---

## Spring Data JPA – Entity definieren

```java
@Entity
@Table(name = "immobilien")
public class Immobilie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String bezeichnung;

    private String strasse;
    private String plz;
    private String ort;
    private BigDecimal wohnfläche;
    private BigDecimal kaufpreis;

    protected Immobilie() {} // JPA benötigt No-Arg-Konstruktor
}
```

- `jakarta.persistence.*` – JPA-Annotationen im Jakarta-Namespace
- No-Arg-Konstruktor kann `protected` sein (nicht zwingend `public`)
- Jede `@Entity` braucht ein `@Id`-Feld

---

## Spring Data JPA – Repository

```java
public interface ImmobilienRepository
        extends JpaRepository<Immobilie, Long> {

    List<Immobilie> findByOrt(String ort);

    List<Immobilie> findByKaufpreisLessThan(BigDecimal maxPreis);

    Optional<Immobilie> findByBezeichnung(String bezeichnung);

    @Query("SELECT i FROM Immobilie i WHERE i.kaufpreis BETWEEN :min AND :max")
    List<Immobilie> findInPreisbereich(@Param("min") BigDecimal min,
                                       @Param("max") BigDecimal max);
}
```

- Spring **generiert die Implementierung** automatisch zur Laufzeit
- **Query Methods** folgen einer Namenskonvention (`findBy…`, `countBy…`, `existsBy…`)
- `@Query` für komplexere JPQL-Abfragen

---

## H2 In-Memory-Datenbank

### `application.yml`

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:immobiliencrm
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
  h2:
    console:
      enabled: true
      path: /h2-console
```

- Ideal für **Entwicklung und Tests** – kein DB-Server nötig
- H2 Console unter `http://localhost:8080/h2-console`
- `create-drop` – Schema wird bei jedem Start neu erstellt
- Für Produktion: PostgreSQL, MySQL oder andere

---

## Bean Validation – Eingaben prüfen

### Request als Java Record (Spring Boot 3 / Java 17+)

```java
public record ImmobilieRequest(
        @NotBlank(message = "Bezeichnung darf nicht leer sein")
        String bezeichnung,

        @NotBlank String strasse,
        @NotBlank String plz,
        @NotBlank String ort,

        @Positive(message = "Wohnfläche muss positiv sein")
        BigDecimal wohnfläche,

        @NotNull @Positive(message = "Kaufpreis muss positiv sein")
        BigDecimal kaufpreis
) {}
```

- Annotations aus `jakarta.validation.constraints.*`
- **Records** statt Klassen: immutabel, kompakt, kein Boilerplate
- Validierung wird durch `@Valid` am Controller-Parameter aktiviert

---

## REST-Controller – GET-Endpunkte

```java
@RestController
@RequestMapping("/api/immobilien")
public class ImmobilienController {

    private final ImmobilienService service;

    public ImmobilienController(ImmobilienService service) {
        this.service = service;
    }

    @GetMapping
    public List<ImmobilieResponse> alleAbrufen() {
        return service.findeAlle();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImmobilieResponse> nachIdSuchen(
            @PathVariable Long id) {
        return service.findeNachId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
```

---

## REST-Controller – POST mit Validation

```java
@PostMapping
public ResponseEntity<ImmobilieResponse> erstellen(
        @Valid @RequestBody ImmobilieRequest request) {

    ImmobilieResponse response = service.erstellen(request);

    URI location = URI.create("/api/immobilien/" + response.id());
    return ResponseEntity.created(location).body(response);
}
```

### HTTP-Statuscodes

| Methode | Erfolg | Fehler |
|---------|--------|--------|
| `GET /` | 200 OK | – |
| `GET /{id}` | 200 OK | 404 Not Found |
| `POST /` | 201 Created + Location | 422 Validation Error |
| `PUT /{id}` | 200 OK | 404 Not Found |
| `DELETE /{id}` | 204 No Content | 404 Not Found |

---

## Zusammenspiel der Schichten

![Klassische Spring-Boot-Schichtarchitektur](../diagrams/spring-boot-classic-layers.drawio.png)

```
HTTP Request  →  @RestController  →  @Service  →  @Repository  →  DB
HTTP Response ←  (JSON/Jackson)   ←  (Logik)   ←  (JPA)        ←  DB
```

- **Controller** empfängt HTTP-Request, validiert Eingabe, delegiert
- **Service** enthält Geschäftslogik (im klassischen Spring-Stil)
- **Repository** abstrahiert Datenbankzugriff über JPA
- **Entity** bildet die Datenbanktabelle ab

---

## Kritischer Blick: Grenzen dieser Architektur

### Was passiert, wenn die Geschäftslogik wächst?

```
┌────────────┐    ┌────────────────────────────┐    ┌────────────┐
│ Controller │───►│ Service                    │───►│ Repository │
│            │    │                            │    │            │
│  Validiert │    │  ✗ Wächst unkontrolliert   │    │  JPA       │
│  Input     │    │  ✗ Gemischte Concerns      │    │  Queries   │
│            │    │  ✗ Schwer testbar           │    │            │
└────────────┘    └────────────────────────────┘    └────────────┘
```

- Services werden zu **God Classes** mit hunderten Zeilen
- **Geschäftslogik** mischt sich mit Transaktions- und Infrastrukturcode
- Domain-Objekte sind **anämisch** – reine Datencontainer
- Abhängigkeiten zeigen nur nach unten → **Domain ist an JPA gekoppelt**

> Im weiteren Workshop werden wir diese Struktur hinterfragen
> und durch **Clean Architecture** ersetzen.

---

## Zusammenfassung

| Thema | Kernpunkte |
|-------|-----------|
| **Spring Boot 3** | Jakarta EE 10, Java 17+, Native Image, Auto-Configuration |
| **IoC Container** | Application Context, Beans, Stereotyp-Annotationen |
| **DI** | Constructor Injection, `final` Felder, testbar |
| **Spring Data JPA** | `@Entity`, `JpaRepository`, Query Methods, H2 |
| **Bean Validation** | `@Valid`, `jakarta.validation.constraints.*`, Records |
| **Spring Web MVC** | `@RestController`, `ResponseEntity`, Statuscodes |

---

## 🎯 Hands-on: Lab 01

### Immobilien-CRUD mit Spring Boot

- Eine `Immobilie`-Entity mit JPA-Annotations anlegen
- Ein `ImmobilienRepository` mit Query Methods erstellen
- Einen `ImmobilienService` mit CRUD-Methoden implementieren
- Einen `@RestController` mit GET, POST, PUT, DELETE bauen
- Bean Validation für Pflichtfelder einbauen

> **Dauer:** ca. 45 Minuten

---

## 💬 Diskussion: Klassische Schichtarchitektur

> Wo liegen die Grenzen des klassischen Ansatzes?

- Ist die Trennung Controller / Service / Repository **ausreichend**?
- Was passiert, wenn **mehrere Services** sich gegenseitig aufrufen?
- Wo leben die **Geschäftsregeln** – im Service oder in der Entity?
- Wie **testbar** ist diese Struktur ohne Spring-Kontext?

> Diese Fragen beantworten wir in den nächsten Modulen –
> beginnend mit **DDD** als Alternative zum Anemic Domain Model.
