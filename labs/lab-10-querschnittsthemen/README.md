# Lab 10: Cross-Cutting Concerns - Locking, Exception Handling, Auditing

## Learning Objective

Implement optimistic locking, global exception handling, and JPA auditing.

## Duration

60 minutes

## Prerequisites

- Lab 09 completed
- Slides Module 13

## Task

Implement three important cross-cutting concerns for a production-ready application.

### Part 1: Optimistic Locking with @Version

Add a version field to the JPA entity `JpaBrokerageProcess`:

```java
@Version
private Long version;
```

Write a test that provokes an `OptimisticLockException`:

1. Load the same `BrokerageProcess` twice
2. Modify and save the first instance
3. Modify and save the second instance -- an `OptimisticLockException` must be thrown

### Part 2: Extend Global Exception Handling

Create or extend the `GlobalExceptionHandler` with `@RestControllerAdvice`:

| Exception | HTTP Status | Description |
|---|---|---|
| `DomainException` | 422 Unprocessable Entity | Business errors from the domain |
| `EntityNotFoundException` | 404 Not Found | Entity not found |
| `OptimisticLockException` | 409 Conflict | Concurrent access |

Create a custom `DomainException` in the `domain.model` package:

```java
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }
}
```

### Part 3: Enable JPA Auditing

1. Enable JPA Auditing on the application class:

```java
@SpringBootApplication
@EnableJpaAuditing
public class RealEstateCrmApplication { ... }
```

2. Add audit fields to the JPA entity `JpaBrokerageProcess`:

```java
@CreatedDate
private LocalDateTime createdDate;

@LastModifiedDate
private LocalDateTime lastModifiedDate;

@CreatedBy
private String createdBy;
```

3. Create an `AuditorAware<String>` bean:

```java
@Configuration
public class AuditorAwareConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.of("system");
    }
}
```

## Verification

Run the tests:

```bash
cd solution
mvn test
```

All tests must pass:
- ArchUnit tests verify architecture rules
- Integration tests verify context integration
- JPA auditing tests verify that `createdDate` is set on save

## Tips

- `@Version` uses optimistic locking: on save, it checks whether the version still matches. If not, an `OptimisticLockException` is thrown.
- `@CreatedDate` and `@LastModifiedDate` require `@EnableJpaAuditing` on the configuration and `@EntityListeners(AuditingEntityListener.class)` on the JPA entity.
- The `GlobalExceptionHandler` with `@RestControllerAdvice` catches exceptions centrally and returns consistent HTTP responses.
- `DomainException` is a custom exception class defined in the domain layer that remains Spring-free.
