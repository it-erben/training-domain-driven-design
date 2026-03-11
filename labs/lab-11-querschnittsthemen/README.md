# Lab 11: Querschnittsthemen - Locking, Exception Handling, Auditing

Implementiere drei wichtige Querschnittsthemen für eine produktionsreife
Anwendung.

## Teil 1: Optimistic Locking mit @Version

Füge ein Versionsfeld zur JPA-Entity `JpaAntragsMappe` hinzu:

```java

@Version
private Long version;
```

Schreibe einen Test, der eine `OptimisticLockException` provoziert:

1. Lade dieselbe `AntragsMappe` zweimal
2. Ändere und speichere die erste Instanz
3. Ändere und speichere die zweite Instanz - eine `OptimisticLockException` muss
   geworfen werden

## Teil 2: Globale Fehlerbehandlung erweitern

Erstelle oder erweitere den `GlobalExceptionHandler` mit
`@RestControllerAdvice`:

| Exception                 | HTTP-Status              | Beschreibung                    |
|---------------------------|--------------------------|---------------------------------|
| `DomainException`         | 422 Unprocessable Entity | Fachliche Fehler aus der Domäne |
| `EntityNotFoundException` | 404 Not Found            | Entity nicht gefunden           |
| `OptimisticLockException` | 409 Conflict             | Gleichzeitiger Zugriff          |

Erstelle eine eigene `DomainException` im Package `domain.model`:

```java
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }
}
```

## Teil 3: JPA Auditing aktivieren

1. Aktiviere JPA Auditing auf der Application-Klasse:

```java

@SpringBootApplication
@EnableJpaAuditing
public class FoerderantragApplication { ...
}
```

2. Füge Audit-Felder zur JPA-Entity `JpaAntragsMappe` hinzu:

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

## Tipps

- `@Version` nutzt Optimistic Locking: Beim Speichern wird geprüft, ob die
  Version noch übereinstimmt. Falls nicht, wird eine `OptimisticLockException`
  geworfen.
- `@CreatedDate` und `@LastModifiedDate` benötigen `@EnableJpaAuditing` auf der
  Konfiguration und `@EntityListeners(AuditingEntityListener.class)` auf der
  JPA-Entity.
- Der `GlobalExceptionHandler` mit `@RestControllerAdvice` fängt Exceptions
  zentral ab und gibt konsistente HTTP-Antworten zurück.
- `DomainException` ist eine eigene Exception-Klasse in der Domain-Schicht, die
  Spring-frei bleibt.
