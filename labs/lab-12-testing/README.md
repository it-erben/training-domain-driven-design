# Lab 12: Teststrategie - Tests auf allen Ebenen

In Lab 10 wurden bereits Domain-Unit-Tests (`BrokerageProcessTest`) und Web-Tests
(`ViewingControllerTest`) erstellt. In diesem Lab erweiterst du die bestehenden Tests
und fügst neue Testarten hinzu: Repository-Integrationstests, erweiterte ArchUnit-Regeln
und Tests für die in Lab 11 hinzugefügten Querschnittsthemen.

## Aufgabe

### Teil 1: Domain-Unit-Tests erweitern (kein Spring-Context!)

Erweitere die bestehende `BrokerageProcessTest`-Klasse um Tests für Geschäftsregeln,
die bisher nicht abgedeckt sind - reine JUnit-5-Tests:

1. Negativtest: `setStatusToNotaryAppointment()` wirft eine `IllegalStateException`, wenn kein angenommenes Angebot existiert
2. Happy Path: Angebot annehmen, dann Status auf NOTARTERMIN setzen - kein Fehler
3. Angebot annehmen: `acceptOffer()` setzt `accepted` auf `true` und der Status ändert sich korrekt

```java
// Ergänze in BrokerageProcessTest:

@Test
void test_setStatusToNotaryAppointment_withoutAcceptedOffer_throwsException() {
    // Arrange: create a new BrokerageProcess
    // Act & Assert: setStatusToNotaryAppointment() -> IllegalStateException
}

@Test
void test_setStatusToNotaryAppointment_withAcceptedOffer_succeeds() {
    // Arrange: receive and accept an offer
    // Act: setStatusToNotaryAppointment()
    // Assert: status is NOTARY_APPOINTMENT
}
```

Wichtig: Kein `@SpringBootTest`, kein `@ExtendWith(SpringExtension.class)` - reine Unit-Tests!

### Teil 2: Repository-Integrationstest

Erstelle einen `@DataJpaTest` für den `BrokerageProcessRepositoryAdapter`:

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

Hinweis: Falls du in Lab 11 JPA Auditing aktiviert hast (`@EnableJpaAuditing`),
muss die `AuditorAwareConfig` per `@Import` hinzugefügt werden oder ein
`@MockitoBean AuditorAware<String>` bereitgestellt werden.

### Teil 3: Web-Tests für Querschnittsthemen erweitern

Die bestehende `ViewingControllerTest`-Klasse aus Lab 10 testet bereits die
Basis-Szenarien (201, 400, 404). Erweitere sie um Tests für die in Lab 11
hinzugefügten Exception-Handler:

```java
// Ergänze in ViewingControllerTest:

@Test
void test_domainException_returns422() {
    // Arrange: Use Case wirft DomainException
    // Act: POST request
    // Assert: 422 Unprocessable Entity
}

@Test
void test_optimisticLockException_returns409() {
    // Arrange: Use Case wirft OptimisticLockException
    // Act: POST request
    // Assert: 409 Conflict
}
```

### Teil 4: ArchUnit-Regel erweitern

Erweitere die bestehende `ArchitectureTest`-Klasse um eine neue Regel:

```java
@ArchTest
static final ArchRule domain_events_should_be_records =
    classes()
        .that().resideInAPackage("..domain.event..")
        .should().beAssignableTo(Record.class);
```

Neue Regel: "Domain Events müssen Records sein" - alle Klassen im Package `..domain.event..` müssen Records sein.

### Bonus: Vollständiger Integrationstest

Erstelle einen `@SpringBootTest`-Integrationstest, der den vollständigen Ablauf überprüft:

1. Erstelle einen BrokerageProcess
2. Lege eine Besichtigung an (über den Use Case)
3. Überprüfe, dass der Prozess mit seiner Besichtigung gespeichert wurde
