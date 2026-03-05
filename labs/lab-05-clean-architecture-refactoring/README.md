# Lab 05: Clean Architecture Refactoring

## Learning Objective

Refactor the code from Lab 04 into a Clean Architecture package structure.

## Duration

60 minutes

## Prerequisites

- Lab 04 completed
- Slides Module 06 and 07

## Task

Refactor the code from Lab 04 into the following package structure:

```
de.realestate.brokerage/
├── domain/
│   ├── model/        (Aggregate Root, Entities, Value Objects, Enum)
│   ├── port/         (Repository Interface = Outbound Port)
│   └── event/        (Domain Events)
├── application/
│   └── service/      (Application Services)
└── infrastructure/
    └── persistence/  (JPA implementation of the Repository)
```

### Step 1: Create Package Structure

Create the package structure shown above under `de.realestate.brokerage`.

### Step 2: Populate the Domain Layer

Move the following classes from Lab 04 into the corresponding packages:

- `domain/model/`: `Address`, `AskingPrice`, `Commission`, `ProcessStatus`, `Viewing`, `Offer`, `BrokerageProcess`
- `domain/port/`: `BrokerageProcessRepository` (pure Java interface)
- `domain/event/`: `ViewingCompleted`, `OfferReceived`, `OfferAccepted`

**Important:** NO Spring imports in the entire `domain` layer! The domain layer must use only standard Java classes.

### Step 3: Create JPA Mapping in Infrastructure

Create the following classes in the `infrastructure/persistence/` package:

**JpaBrokerageProcess** - JPA `@Entity` with Jakarta Persistence annotations:

- All fields from the domain model as JPA-compatible fields
- `@Id` and `@GeneratedValue` for the ID
- `@ElementCollection` for `viewings` and `offers`
- Methods `toModel()` and `static fromModel()` for converting between domain model and JPA entity

**JpaViewing** - `@Embeddable` with JPA fields

**JpaOffer** - `@Embeddable` with JPA fields

**JpaBrokerageProcessRepository** - Interface extending `JpaRepository<JpaBrokerageProcess, UUID>`

**BrokerageProcessRepositoryAdapter** - `@Component`, implements the domain interface `BrokerageProcessRepository`:

- Injects `JpaBrokerageProcessRepository`
- Maps between domain objects and JPA entities

### Step 4: Create the Application Service

Create `BrokerageProcessApplicationService` in the `application/service/` package:

- `@Service`, `@Transactional`
- Injects `BrokerageProcessRepository` (domain port interface) via constructor injection
- Methods:
  - `create(UUID propertyId, Address address, AskingPrice askingPrice, Commission commission)` - creates and persists a new BrokerageProcess
  - `findById(UUID id)` - returns `Optional<BrokerageProcess>`

## Verification

1. Project compiles:

```bash
cd solution
mvn compile
```

2. No Spring imports in `domain/`:

```bash
grep -r "org.springframework" src/main/java/de/realestate/brokerage/domain/
```

This command must return no results.

3. The Application Service can receive the repository port via constructor injection.

4. Tests pass:

```bash
mvn test
```

## Bonus

Verify with a simple grep/find that no `org.springframework` imports exist in `domain/`:

```bash
find src/main/java/de/realestate/brokerage/domain -name "*.java" \
  -exec grep -l "org.springframework" {} \;
```

The result must be empty -- no matches at all.

## Tips

- The domain layer knows neither Spring nor JPA. It contains pure Java.
- The infrastructure layer implements the domain layer's ports and handles technical persistence.
- The application layer orchestrates use cases and uses the domain layer's ports.
- The adapter in the infrastructure layer handles the mapping between domain model and JPA entity. This keeps the domain model free from technical annotations.
