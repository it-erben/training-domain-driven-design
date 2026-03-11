# Lab 12: Teststrategie - Tests auf allen Ebenen

In den bisherigen Labs wurden bereits Domain-Unit-Tests (`AntragsMappeTest`) und
Web-Tests (`FlurstueckControllerTest`) erstellt. In diesem Lab erweiterst du die
bestehenden Tests und fügst neue Testarten hinzu: Repository-Integrationstests,
erweiterte ArchUnit-Regeln und Tests für die in Lab 11 hinzugefügten
Querschnittsthemen.

## Aufgabe

### Teil 1: Domain-Unit-Tests erweitern (kein Spring-Context!)

Erweitere die bestehende `AntragsMappeTest`-Klasse um Tests für Geschäftsregeln,
die bisher nicht abgedeckt sind - reine JUnit-5-Tests:

1. Negativtest: `einreichen()` wirft eine `IllegalStateException`, wenn kein
   Flurstück existiert
2. Happy Path: Flurstück hinzufügen, dann einreichen - kein Fehler
3. Nachweis akzeptieren: `nachweisAkzeptieren()` setzt `akzeptiert` auf `true`
   und erzeugt das korrekte Domain Event

```java
// Ergänze in AntragsMappeTest:

@Test
void test_einreichen_ohneFlurstueck_wirftException() {
    // Arrange: create a new AntragsMappe
    // Act & Assert: einreichen() -> IllegalStateException
}

@Test
void test_einreichen_mitFlurstueck_erfolgreich() {
    // Arrange: Flurstück hinzufügen
    // Act: einreichen()
    // Assert: status is EINGEREICHT
}
```

Wichtig: Kein `@SpringBootTest`, kein `@ExtendWith(SpringExtension.class)` -
reine Unit-Tests!

### Teil 2: Repository-Integrationstest

Erstelle einen `@DataJpaTest` für den `AntragsMappeRepositoryAdapter`:

```java
@DataJpaTest
@Import(AntragsMappeRepositoryAdapter.class)
class AntragsMappeRepositoryAdapterTest {

    @Autowired
    private AntragsMappeRepositoryAdapter repository;

    @Test
    void test_saveAndFindById() {
        // Save and load an AntragsMappe
    }

    @Test
    void test_flurstueckeArePersisted() {
        // Save an AntragsMappe with a Flurstück
        // Load and verify the Flurstück is present
    }
}
```

Hinweis: Falls du in Lab 11 JPA Auditing aktiviert hast (`@EnableJpaAuditing`),
muss die `AuditorAwareConfig` per `@Import` hinzugefügt werden oder ein
`@MockitoBean AuditorAware<String>` bereitgestellt werden.

### Teil 3: Web-Tests für Querschnittsthemen erweitern

Die bestehende `FlurstueckControllerTest`-Klasse testet bereits die
Basis-Szenarien (201, 400, 404). Erweitere sie um Tests für die in Lab 11
hinzugefügten Exception-Handler:

```java
// Ergänze in FlurstueckControllerTest:

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

Neue Regel: "Domain Events müssen Records sein" - alle Klassen im Package
`..domain.event..` müssen Records sein.

### Bonus: Vollständiger Integrationstest

Erstelle einen `@SpringBootTest`-Integrationstest, der den vollständigen Ablauf
überprüft:

1. Erstelle eine AntragsMappe
2. Füge ein Flurstück hinzu (über den Use Case)
3. Überprüfe, dass die AntragsMappe mit ihrem Flurstück gespeichert wurde
