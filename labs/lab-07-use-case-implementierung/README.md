# Lab 07: Use Case Implementation - Create Viewing

## Learning Objective

Implement an Application Service as a use case orchestrator.

## Duration

45 minutes

## Prerequisites

- Lab 06 completed
- Slides Module 09

## Task

Implement the "Create Viewing" use case as an Application Service. The use case orchestrates the domain logic invocation and handles persistence.

### Step 1: Create the Command Object

Create the command object `CreateViewingCommand` as a Java Record in the package `de.realestate.brokerage.application.command`:

```java
public record CreateViewingCommand(
    UUID processId,
    String prospectName,
    LocalDateTime appointmentDate
) {}
```

The command represents the caller's intention and contains all the data the use case needs.

### Step 2: Create the Result Object

Create the result object `CreateViewingResult` as a Java Record in the same package:

```java
public record CreateViewingResult(
    UUID viewingId,
    UUID processId
) {}
```

### Step 3: Implement the Use Case

Create the Application Service `CreateViewingUseCase` as a `@Service` in the package `de.realestate.brokerage.application.service`:

```java
@Service
public class CreateViewingUseCase {

    private final BrokerageProcessRepository repository;

    // Constructor Injection

    @Transactional
    public CreateViewingResult create(CreateViewingCommand command) {
        // 1. Load the BrokerageProcess from the repository
        // 2. Call the domain method addViewing()
        // 3. Save the BrokerageProcess
        // 4. Return the result
    }
}
```

**Flow:**

1. Load the `BrokerageProcess` by ID from the repository
2. If not found: throw a `ProcessNotFoundException`
3. Call the domain method `addViewing(prospectName, appointmentDate)` on the Aggregate Root
4. Save the updated `BrokerageProcess` via the repository
5. Return a `CreateViewingResult` with the new viewing ID

**Important:** The use case uses `@Transactional` to ensure consistency. The business logic remains in the domain model - the use case only orchestrates.

### Step 4: Exception for Process Not Found

Create the exception `ProcessNotFoundException` in the package `de.realestate.brokerage.domain.model`:

```java
public class ProcessNotFoundException extends RuntimeException {
    public ProcessNotFoundException(UUID id) {
        super("BrokerageProcess with ID " + id + " not found");
    }
}
```

**Note:** The exception resides in the domain package because it represents a domain concept ("there is no brokerage process with this ID").

### Bonus: Second Use Case

Implement a second use case `CompleteViewingUseCase`:

- Command: `CompleteViewingCommand(UUID processId, UUID viewingId)`
- Loads the BrokerageProcess, calls `completeViewing(viewingId)`, and saves

## Verification

Write a unit test for the use case with a mocked repository:

1. **Happy Path:** BrokerageProcess exists, viewing is created, result is returned
2. **Not Found:** BrokerageProcess does not exist, `ProcessNotFoundException` is thrown

```bash
cd solution
mvn test
```

All tests must pass.

## Tips

- The use case is intentionally kept thin - the business logic resides in the domain model.
- Commands and Results are immutable (Records) and belong to the application layer.
- `@Transactional` ensures a rollback occurs in case of an exception.
- The repository interface comes from the domain layer - the use case depends only on the abstraction, not on the concrete implementation.
