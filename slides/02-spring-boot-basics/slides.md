---
marp: true
theme: default
paginate: true
header: "DDD & Clean Architecture mit Spring Boot 4"
footer: "CC BY-NC-SA 4.0, Alexander Erben"
---

# Modul 02 – Spring Boot 4 Recap

**Geschätzte Dauer: 45 Minuten**

### Lernziele

- Die Neuerungen in **Spring Boot 4** gegenüber 3.x kennen
- **RestClient**, **Virtual Threads** und **ProblemDetail** einsetzen können
- Das Zusammenspiel der klassischen Schichten verstehen
- Die **Grenzen** der klassischen Schichtarchitektur erkennen

---

## Ausgangspunkt: Was ihr mitbringt

Ihr kennt Spring Boot – daher nur ein kurzer Recap der Basics:

- **IoC Container**, Stereotyp-Annotationen (`@Service`, `@Repository`, …)
- **Constructor Injection** mit `final`-Feldern
- **Spring Data JPA** – `@Entity`, `JpaRepository`, Query Methods
- **Bean Validation** – `@Valid`, `@NotBlank`, `@Positive`
- **Spring Web MVC** – `@RestController`, `ResponseEntity`

> In diesem Modul konzentrieren wir uns auf das, was in
> **Spring Boot 4** anders und neu ist.

---
<style scoped>section { font-size: 1.8em; }</style>

## Spring Boot 4 – Die großen Änderungen

| Änderung                   | Detail                                          |
|----------------------------|-------------------------------------------------|
| **Spring Framework 7**     | Neues Major-Release als Basis                   |
| **Jakarta EE 11**          | Servlet 6.1, JPA 3.2, Bean Validation 3.1       |
| **Java 17+ Baseline**      | Unverändert – Java 21+ empfohlen                |
| **Virtual Threads**        | Stabil und produktionsreif                      |
| **RestClient**             | Moderner Ersatz für RestTemplate                |
| **ProblemDetail**          | RFC 9457 out of the box                         |
| **@MockBean entfernt**     | Ersetzt durch `@MockitoBean` (Spring Framework) |
| **Strukturiertes Logging** | JSON-Logs ohne externe Library                  |

---

<style scoped>section { font-size: 1.8em; }</style>

## Jakarta EE 11 – Namespace-Migration

### Vorher (Spring Boot 2.x)

```java
import javax.persistence.Entity;
import javax.validation.constraints.NotBlank;
import javax.servlet.http.HttpServletRequest;
```

### Seit Spring Boot 3.x / 4.x (Jakarta EE)

```java
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.servlet.http.HttpServletRequest;
```

> **Tipp:** IntelliJ IDEA bietet automatisierte Migration an:
> *Refactor → Migrate Packages and Classes*

---

### Hibernate 7 (Auszug aus den Neuerungen)

- **Soft Deletes** mit `@SoftDelete` – logisches Löschen ohne Custom-Implementierung
- **Java Records als Embeddable** – `@Embeddable` Records ohne Workaround
- **Verbesserte `@IdClass`-Unterstützung** für zusammengesetzte Schlüssel

---

<style scoped>section { font-size: 1.6em; }</style>

## Virtual Threads – Überblick

### Das Problem mit Platform Threads

```
1 Request = 1 Thread = ~1 MB Stack
10.000 gleichzeitige Requests = 10 GB RAM nur für Stacks
```

### Virtual Threads (Project Loom, seit Java 21)

```
1 Request = 1 Virtual Thread = ~wenige KB
Millionen gleichzeitiger Virtual Threads möglich
```

- Virtual Threads sind **leichtgewichtig** – vom JDK verwaltet, nicht vom OS
- **Blockierende I/O** (DB, HTTP, File) gibt den Carrier Thread automatisch frei
- Kein reaktiver Programmierstil nötig – normaler, synchroner Code

---

## Virtual Threads in Spring Boot 4

### Aktivierung

```yaml
spring:
  threads:
    virtual:
      enabled: true
```

Mehr ist nicht nötig!

### Was passiert dann?

- Tomcat nutzt Virtual Threads für **alle eingehenden Requests**
- `@Async`-Methoden laufen auf Virtual Threads
- Spring Data Repositories profitieren bei **blockierenden DB-Calls**
- `@Scheduled`-Tasks können auf Virtual Threads laufen

---

## Virtual Threads – Wann (nicht) sinnvoll?

### Ideal für

- **I/O-lastige Anwendungen** – DB-Queries, REST-Calls, File I/O
- **Hohe Parallelität** – viele gleichzeitige, aber kurze Requests
- **Einfacher Code** – synchron schreiben, trotzdem gut skalieren

### Vorsicht bei

- **CPU-intensive Aufgaben** – Virtual Threads bringen hier keinen Vorteil
- **`synchronized`-Blöcke** – können den Carrier Thread pinnen
- **ThreadLocal-Abhängigkeiten** – manche Libraries nutzen ThreadLocal exzessiv

### Faustregel

> Wenn eure Anwendung I/O-bound ist (was oft der Fall ist), sind Virtual Threads
> sehr sinnvoll.

---

<style scoped>section { font-size: 1.5em; }</style>

## RestClient – Der moderne HTTP-Client

### Die Evolution der HTTP-Clients in Spring

| Generation | Klasse         | Stil                 | Status                      |
|------------|----------------|----------------------|-----------------------------|
| 1.         | `RestTemplate` | Synchron, imperativ  | **Deprecated** in SB 4      |
| 2.         | `WebClient`    | Reaktiv (Mono/Flux)  | Weiterhin für reaktive Apps |
| 3.         | `RestClient`   | **Synchron, fluent** | **Empfohlen ab SB 4**       |

### Warum RestClient?

- Fluent API wie `WebClient`, aber **synchron** – kein Reactor nötig
- Unterstützt **alle HTTP-Methoden** und Content-Types
- Eingebaute **Fehlerbehandlung** mit Status-Handlern
- Integration mit Virtual Threads

---

<style scoped>section { font-size: 1.5em; }</style>

## RestClient – Beispiel

```java
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient portalRestClient(RestClient.Builder builder) {
        return builder
                .baseUrl("https://api.immoportal.de/v2")
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
```

```java
@Component
public class PortalAdapter {

    private final RestClient restClient;

    public PortalAdapter(RestClient restClient) {
        this.restClient = restClient;
    }

    public PortalListing fetchListing(String listingId) {
        return restClient.get()
                .uri("/listings/{id}", listingId)
                .retrieve()
                .body(PortalListing.class);
    }
}
```

---

<style scoped>section { font-size: 1.5em; }</style>

## RestClient – Fehlerbehandlung

```java
public PortalListing fetchListing(String listingId) {
    return restClient.get()
            .uri("/listings/{id}", listingId)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                throw new PortalNotFoundException(
                        "Listing %s not found".formatted(listingId));
            })
            .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                throw new PortalUnavailableException(
                        "Portal returned " + response.getStatusCode());
            })
            .body(PortalListing.class);
}
```

- **Status-Handler** statt globaler `ResponseErrorHandler`
- Zugriff auf Request **und** Response im Handler

---

<style scoped>section { font-size: 1.5em; }</style>

## RestClient – POST und Exchange

```java
// POST mit Body
public PortalListing createListing(PortalListingRequest request) {
    return restClient.post()
            .uri("/listings")
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body(PortalListing.class);
}

// Exchange für volle Kontrolle über die Response
public ResponseEntity<PortalListing> createListingWithHeaders(
        PortalListingRequest request) {
    return restClient.post()
            .uri("/listings")
            .body(request)
            .exchange((req, res) -> {
                var listing = res.bodyTo(PortalListing.class);
                return ResponseEntity
                        .status(res.getStatusCode())
                        .headers(res.getHeaders())
                        .body(listing);
            });
}
```

---

<style scoped>section { font-size: 1.6em; }</style>

## ProblemDetail – RFC 9457

### Standardisierte Fehlerantworten

Spring Boot 4 unterstützt **RFC 9457** (Problem Details for HTTP APIs) nativ.

```json
{
  "type": "https://api.realestate.de/errors/property-not-found",
  "title": "Property not found",
  "status": 404,
  "detail": "No property with ID 42 exists",
  "instance": "/api/properties/42"
}
```

### Aktivierung

```yaml
spring:
  mvc:
    problemdetail:
      enabled: true
```

---

<style scoped>section { font-size: 1.5em; }</style>

## ProblemDetail – Im Controller verwenden

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PropertyNotFoundException.class)
    public ProblemDetail handleNotFound(PropertyNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Property not found");
        problem.setType(URI.create("https://api.realestate.de/errors/not-found"));
        problem.setProperty("propertyId", ex.getPropertyId());
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY, "Validation failed");
        problem.setProperty("errors", ex.getFieldErrors().stream()
                .map(e -> Map.of("field", e.getField(), "message", e.getDefaultMessage()))
                .toList());
        return problem;
    }
}
```

---

## @MockitoBean – Ersatz für @MockBean

### Vorher (Spring Boot 3.x)

```java
@SpringBootTest
class PropertyServiceTest {
    @MockBean  // aus spring-boot-test
    private PropertyRepository repository;
}
```

### Seit Spring Boot 4

```java
@SpringBootTest
class PropertyServiceTest {
    @MockitoBean  // aus spring-framework-test
    private PropertyRepository repository;
}
```

- `@MockBean` und `@SpyBean` sind **entfernt** (nicht deprecated – entfernt!)
- `@MockitoBean` und `@MockitoSpyBean` aus **Spring Framework** direkt
- Funktionalität ist identisch – nur der Import ändert sich

---

<style scoped>section { font-size: 1.6em; }</style>

## Strukturiertes Logging

### JSON-Logs ohne externe Library

```yaml
logging:
  structured:
    format:
      console: ecs  # Elastic Common Schema
      # Alternativen: logstash, gelf
```

### Ergebnis

```json
{
  "@timestamp": "2026-03-06T09:15:23.456Z",
  "log.level": "INFO",
  "message": "Property created with ID 42",
  "service.name": "immobilien-crm",
  "ecs.version": "1.2.0"
}
```

- Unterstützte Formate: **ECS** (Elastic), **Logstash**, **GELF**
- Kein Logback-XML oder zusätzliche Dependencies nötig
- Ideal für **ELK-Stack**, **Grafana Loki** oder **Datadog**

---

## Weitere Neuerungen im Überblick

| Feature                      | Details                                                      |
|------------------------------|--------------------------------------------------------------|
| **Declarative HTTP Clients** | `@HttpExchange`-Interfaces – wie Feign, aber nativ           |
| **Observability**            | Micrometer Tracing out of the box (ersetzt Sleuth)           |
| **SSL Bundles**              | TLS-Konfiguration gebündelt und wiederverwendbar             |
| **Docker Compose Support**   | `spring-boot-docker-compose` startet Container automatisch   |
| **GraalVM Native**           | Verbesserte AOT-Verarbeitung, weniger Reflection-Hints nötig |
| **Testcontainers**           | `@ServiceConnection` für automatische Testcontainer-Config   |

---

![bg center h:450](images/spring-boot-classic-layers.drawio.svg)

---

## Zusammenspiel der Schichten

![Zusammenspiel der Schichten](images/zusammenspiel-schichten.drawio.png)

- **Controller** empfängt HTTP-Request, validiert Eingabe, delegiert
- **Service** enthält Geschäftslogik (im klassischen Spring-Stil)
- **Repository** abstrahiert Datenbankzugriff über JPA
- **Entity** bildet die Datenbanktabelle ab

---

## Grenzen dieser Architektur

### Was passiert, wenn die Geschäftslogik wächst?

![Grenzen der Schichtarchitektur](images/grenzen-schichtarchitektur.drawio.png)

- Services werden zu **God Classes** mit hunderten Zeilen
- **Geschäftslogik** mischt sich mit Transaktions- und Infrastrukturcode
- Domain-Objekte sind **anämisch** – reine Datencontainer
- Abhängigkeiten zeigen nur nach unten → **Domain ist an JPA gekoppelt**

> Im weiteren Workshop werden wir diese Struktur hinterfragen
> und durch **Clean Architecture** ersetzen.

---

## Zusammenfassung

| Thema                      | Kernpunkte                                                   |
|----------------------------|--------------------------------------------------------------|
| **Spring Boot 4**          | Jakarta EE 11, Spring Framework 7, Java 17+                  |
| **Virtual Threads**        | Eine Zeile Config, synchroner Code skaliert I/O-lastige Apps |
| **RestClient**             | Fluent, synchron, ersetzt RestTemplate                       |
| **ProblemDetail**          | RFC 9457, standardisierte Fehlerantworten                    |
| **@MockitoBean**           | Ersetzt `@MockBean`, jetzt in Spring Framework               |
| **Strukturiertes Logging** | JSON-Logs nativ (ECS, Logstash, GELF)                        |

---

## Diskussion: Klassische Schichtarchitektur

> Wo liegen die Grenzen des klassischen Ansatzes?

- Ist die Trennung Controller / Service / Repository **ausreichend**?
- Was passiert, wenn **mehrere Services** sich gegenseitig aufrufen?
- Wo leben die **Geschäftsregeln** – im Service oder in der Entity?
- Wie **testbar** ist diese Struktur ohne Spring-Kontext?

> Diese Fragen beantworten wir in den nächsten Modulen –
> beginnend mit **DDD** als Alternative zum Anemic Domain Model.
