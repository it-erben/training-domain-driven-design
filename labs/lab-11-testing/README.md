# Lab 11: Test Strategy - Tests at All Levels

## Learning Objective

Write domain, repository, web, and architecture tests.

## Duration

60 minutes

## Prerequisites

- Lab 10 completed
- Slides Module 14

## Task

Add tests at various levels to the project: unit tests for domain logic, integration tests for the repository, web tests for the REST adapter, and ArchUnit tests for architecture.

### Part 1: Domain Unit Test (no Spring context!)

Test the invariants of the Aggregate Root `BrokerageProcess` without a Spring context -- pure JUnit 5 tests:

1. **Negative test:** `setStatusToNotaryAppointment()` throws an `IllegalStateException` when no accepted offer exists
2. **Happy path:** Accept an offer, then set status to NOTARTERMIN -- no error
3. **Add viewing:** `addViewing()` creates a Viewing and updates the status
4. **Accept offer:** `acceptOffer()` sets `accepted` to `true`

```java
class BrokerageProcessTest {

    @Test
    void test_setStatusToNotaryAppointment_withoutAcceptedOffer_throwsException() {
        // Arrange: create a new BrokerageProcess
        // Act & Assert: setStatusToNotaryAppointment() -> IllegalStateException
    }

    @Test
    void test_setStatusToNotaryAppointment_withAcceptedOffer_succeeds() {
        // Arrange: add and accept an offer
        // Act: setStatusToNotaryAppointment()
        // Assert: status is NOTARTERMIN
    }
}
```

**Important:** No `@SpringBootTest`, no `@ExtendWith(SpringExtension.class)` -- pure unit tests!

### Part 2: Repository Integration Test

Create a `@DataJpaTest` for the `BrokerageProcessRepositoryAdapter`:

```java
@DataJpaTest
@Import(BrokerageProcessRepositoryAdapter.class)
class BrokerageProcessRepositoryAdapterTest {

    @Autowired
    private BrokerageProcessRepositoryAdapter repository;

    @Test
    void test_saveAndFindById() {
        // Save and load a BrokerageProcess
    }

    @Test
    void test_viewingsArePersisted() {
        // Save a BrokerageProcess with a Viewing
        // Load and verify the Viewing is present
    }
}
```

### Part 3: Web/API Test

Create a `@WebMvcTest` for the `ViewingController`:

```java
@WebMvcTest(ViewingController.class)
class ViewingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateViewingUseCase useCase;

    @Test
    void test_createViewing_validRequest_201() {
        // POST /api/brokerage-processes/{id}/viewings with valid body -> 201
    }

    @Test
    void test_createViewing_invalidRequest_422() {
        // POST with invalid body -> 422
    }

    @Test
    void test_createViewing_processNotFound_404() {
        // POST with unknown processId -> 404
    }
}
```

### Part 4: ArchUnit

Extend the ArchUnit tests from Lab 08 with a new rule:

```java
@ArchTest
static final ArchRule domain_events_should_be_records =
    classes()
        .that().resideInAPackage("..domain.event..")
        .should().beAssignableTo(Record.class);
```

**New rule:** "Domain events should be records" -- all classes in the `..domain.event..` package must be records.

### Bonus: Full Integration Test

Create a `@SpringBootTest` full integration test that verifies the complete flow:

1. Create a BrokerageProcess
2. Schedule a viewing (via the use case)
3. Verify that the process with its viewing has been saved

## Verification

Run all tests:

```bash
cd solution
mvn test
```

All tests must pass -- at least 8 tests.

## Tips

- **Domain tests** do not need a Spring context and are therefore very fast.
- **`@DataJpaTest`** only starts the JPA layer with an embedded H2 database.
- **`@WebMvcTest`** only starts the web layer and mocks all dependencies.
- **ArchUnit** analyzes the compiled bytecode and does not need a running context.
- Use `@MockBean` in `@WebMvcTest` to mock the controller's dependencies.
- In `@DataJpaTest`, adapter classes must be explicitly added via `@Import`.
