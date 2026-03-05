# Lab 05: Tactical DDD - Implementing Building Blocks

## Learning Objective

Implement Aggregate Root, Entities, Value Objects, and Domain Events in Java.

## Duration

90 minutes

## Prerequisites

- Lab 02 completed
- Slides Module 06

## Task

Model and implement the Bounded Context "Brokerage Process".

### Step 1: Create Value Objects as Java Records

Create the following Value Objects as Java Records in the package `de.realestate.brokerage.domain.model`:

**Address**

```java
public record Address(String street, String postalCode, String city) {
    // Compact constructor with validation:
    // - All fields must not be null or blank
}
```

**AskingPrice**

```java
public record AskingPrice(BigDecimal amount, String currency) {
    // Compact constructor with validation:
    // - amount must be greater than 0
    // - currency must not be null or blank
}
```

**Commission**

```java
public record Commission(BigDecimal percentage) {
    // Compact constructor with validation:
    // - percentage must be greater than 0 and less than or equal to 100
}
```

### Step 2: Domain Events as Records

Create the following Domain Events as Records in the package `de.realestate.brokerage.domain.event`:

```java
public record ViewingCompleted(
    UUID brokerageProcessId,
    UUID viewingId,
    LocalDateTime timestamp
) {}

public record OfferReceived(
    UUID brokerageProcessId,
    BigDecimal offerAmount,
    LocalDateTime timestamp
) {}

public record OfferAccepted(
    UUID brokerageProcessId,
    UUID offerId,
    LocalDateTime timestamp
) {}
```

### Step 3: Entity Viewing (within the aggregate)

Create the Entity `Viewing` in the package `de.realestate.brokerage.domain.model`:

- Fields: `id` (UUID), `prospectName` (String), `timestamp` (LocalDateTime), `notes` (String), `completed` (boolean)
- Method: `complete()` sets `completed` to `true`

### Step 4: Entity Offer (within the aggregate)

Create the Entity `Offer` in the package `de.realestate.brokerage.domain.model`:

- Fields: `id` (UUID), `prospectName` (String), `amount` (BigDecimal), `receivedAt` (LocalDateTime), `accepted` (boolean)
- Method: `accept()` sets `accepted` to `true`

### Step 5: Aggregate Root BrokerageProcess

Create the Aggregate Root class `BrokerageProcess` in the package `de.realestate.brokerage.domain.model`:

**Fields:**

- `id` (UUID)
- `propertyId` (UUID)
- `address` (Address)
- `askingPrice` (AskingPrice)
- `commission` (Commission)
- `status` (Enum: NEW, IN_MARKETING, VIEWING, OFFER_PHASE, NOTARY_APPOINTMENT, COMPLETED)
- `viewings` (List\<Viewing\>)
- `offers` (List\<Offer\>)
- `domainEvents` (List\<Object\>, transient)

**Methods:**

- `addViewing(String prospectName, LocalDateTime timestamp, String notes)` - adds a new viewing and sets the status to VIEWING
- `completeViewing(UUID viewingId)` - marks a viewing as completed and raises a `ViewingCompleted` event
- `receiveOffer(String prospectName, BigDecimal amount)` - adds a new offer, sets the status to OFFER_PHASE, and raises an `OfferReceived` event
- `acceptOffer(UUID offerId)` - accepts an offer and raises an `OfferAccepted` event
- `setStatusToNotaryAppointment()` - sets the status to NOTARY_APPOINTMENT; throws an `IllegalStateException` if no accepted offer exists

**Invariant:**

`setStatusToNotaryAppointment()` may only be called when at least one accepted offer exists. Otherwise, an `IllegalStateException` is thrown.

### Step 6: Repository Interface

Create the interface `BrokerageProcessRepository` in the package `de.realestate.brokerage.domain.port`:

```java
public interface BrokerageProcessRepository {
    Optional<BrokerageProcess> findById(UUID id);
    BrokerageProcess save(BrokerageProcess brokerageProcess);
    void deleteById(UUID id);
}
```

**Important:** Do not use any Spring imports in this interface. It is a pure Java interface.

### Bonus: Factory Method

Implement a static factory method on `BrokerageProcess`:

```java
public static BrokerageProcess create(
    UUID propertyId,
    Address address,
    AskingPrice askingPrice,
    Commission commission
) {
    // Creates a new BrokerageProcess with status NEW
}
```

## Verification

Write a unit test that verifies the invariant:

1. Create a new `BrokerageProcess`
2. Call `setStatusToNotaryAppointment()` - an `IllegalStateException` must be thrown
3. Add an offer and accept it
4. Call `setStatusToNotaryAppointment()` again - this time it must succeed

```bash
cd solution
mvn test
```

All tests must be green.

## Tips

- Value Objects are best represented as Records in Java - they are automatically immutable and have `equals()`/`hashCode()`.
- Domain Events are collected in the Aggregate Root and published only when saving.
- The Repository interface belongs to the domain layer and must not have any framework dependencies.
