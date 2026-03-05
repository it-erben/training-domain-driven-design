# Lab 10: Context Integration - Connecting Bounded Contexts

## Learning Objective

Implement event-based communication between bounded contexts.

## Duration

60 minutes

## Prerequisites

- Lab 09 completed
- Slides Modules 12 and 13

## Task

Create a second bounded context "Acquisition" and connect it to the existing Brokerage BC via domain events.

### Step 1: Create a Minimal Acquisition BC

Create the entity `BrokerageContract` in the package `de.realestate.acquisition.domain.model`:

- Fields: `id` (UUID), `ownerId` (UUID), `propertyId` (UUID), `closedAt` (LocalDateTime)
- Method: `close()` sets `closedAt` to the current timestamp

```java
public class BrokerageContract {

    private final UUID id;
    private final UUID ownerId;
    private final UUID propertyId;
    private LocalDateTime closedAt;

    // Constructor, factory method, getters
    // close() sets closedAt = LocalDateTime.now()
}
```

### Step 2: Create Integration Event

Create the integration event `ContractSigned` as a record in the package `de.realestate.acquisition.domain.event`:

```java
public record ContractSigned(
    UUID contractId,
    UUID propertyId,
    LocalDateTime closedAt
) {}
```

### Step 3: Application Service in the Acquisition BC

Create the service `CloseContractUseCase` in the package `de.realestate.acquisition.application.service`:

- Inject `BrokerageContractRepository` and `ApplicationEventPublisher`
- Method `close(UUID contractId)`:
  1. Load the BrokerageContract
  2. Call `close()`
  3. Save
  4. Publish the `ContractSigned` event via `ApplicationEventPublisher`

```java
@Service
public class CloseContractUseCase {

    private final BrokerageContractRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    // Constructor Injection

    @Transactional
    public void close(UUID contractId) {
        // 1. Load
        // 2. close()
        // 3. Save
        // 4. Publish event
    }
}
```

### Step 4: Event Listener in the Brokerage BC

Create the listener `ContractSignedListener` in the package `de.realestate.brokerage.application.listener`:

```java
@Component
public class ContractSignedListener {

    private final BrokerageProcessRepository repository;

    // Constructor Injection

    @EventListener
    public void handle(ContractSigned event) {
        // Create a new BrokerageProcess
        // using the propertyId from the event
        // Save
    }
}
```

### Step 5: Test

Write an integration test that verifies the entire flow:

1. Create a `BrokerageContract`
2. Close it (via the use case)
3. Verify that a `BrokerageProcess` was automatically created

### Bonus: TransactionalEventListener

Replace `@EventListener` with `@TransactionalEventListener(phase = AFTER_COMMIT)` to ensure that the event is only processed after the transaction has been successfully committed.

## Verification

Run the integration test:

```bash
cd solution
mvn test
```

The test must confirm that after closing a BrokerageContract, a BrokerageProcess is automatically created.

## Tips

- Spring's `ApplicationEventPublisher` is well suited for communication between bounded contexts within a monolith.
- The event belongs to the publishing BC (Acquisition) -- the consuming BC (Brokerage) imports it.
- Make sure that the listener in the Brokerage BC has no direct dependency on the Acquisition domain model -- only on the event.
- `@TransactionalEventListener(phase = AFTER_COMMIT)` ensures that the event is only processed when the transaction was successful.
