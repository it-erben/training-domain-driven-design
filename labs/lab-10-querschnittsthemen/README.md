# Lab 10: Querschnittsthemen - Locking, Exception Handling, Auditing

## Lernziel

Optimistic Locking, globales Exception Handling und JPA Auditing implementieren.

## Dauer

60 Minuten

## Voraussetzungen

- Lab 09 abgeschlossen
- Slides Modul 13

## Aufgabe

Implementiere drei wichtige Querschnittsthemen für eine produktionsreife Anwendung.

### Teil 1: Optimistic Locking mit @Version

Füge ein Versionsfeld zum JPA-Entity `JpaVermittlungsvorgang` hinzu:

```java
@Version
private Long version;
```

Schreibe einen Test, der eine `OptimisticLockException` provoziert:

1. Lade denselben `Vermittlungsvorgang` zweimal
2. Ändere und speichere die erste Instanz
3. Ändere und speichere die zweite Instanz - es muss eine `OptimisticLockException` geworfen werden

### Teil 2: Globales Exception Handling erweitern

Erstelle oder erweitere den `GlobalExceptionHandler` mit `@RestControllerAdvice`:

| Exception | HTTP-Status | Beschreibung |
|---|---|---|
| `DomainException` | 422 Unprocessable Entity | Fachliche Fehler aus der Domain |
| `EntityNotFoundException` | 404 Not Found | Entity nicht gefunden |
| `OptimisticLockException` | 409 Conflict | Konkurrierender Zugriff |

Erstelle dazu eine eigene `DomainException` im Package `domain.model`:

```java
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }
}
```

### Teil 3: JPA Auditing aktivieren

1. Aktiviere JPA Auditing auf der Application-Klasse:

```java
@SpringBootApplication
@EnableJpaAuditing
public class ImmobilienCrmApplication { ... }
```

2. Füge Audit-Felder zum JPA-Entity `JpaVermittlungsvorgang` hinzu:

```java
@CreatedDate
private LocalDateTime createdDate;

@LastModifiedDate
private LocalDateTime lastModifiedDate;

@CreatedBy
private String createdBy;
```

3. Erstelle eine `AuditorAware<String>`-Bean:

```java
@Configuration
public class AuditorAwareConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.of("system");
    }
}
```

## Verifikation

Führe die Tests aus:

```bash
cd solution
mvn test
```

Alle Tests müssen grün sein:
- ArchUnit-Tests prüfen die Architekturregeln
- Integrationstests prüfen die Context-Integration
- JPA-Auditing-Tests prüfen, dass `createdDate` beim Speichern gesetzt wird

## Tipps

- `@Version` verwendet Optimistic Locking: Beim Speichern wird geprüft, ob die Version noch übereinstimmt. Falls nicht, wird eine `OptimisticLockException` geworfen.
- `@CreatedDate` und `@LastModifiedDate` erfordern `@EnableJpaAuditing` auf der Konfiguration und `@EntityListeners(AuditingEntityListener.class)` auf dem JPA-Entity.
- Der `GlobalExceptionHandler` mit `@RestControllerAdvice` fängt Exceptions zentral ab und gibt einheitliche HTTP-Responses zurück.
- `DomainException` ist eine eigene Exception-Klasse, die in der Domain-Schicht definiert wird und Spring-frei bleibt.
