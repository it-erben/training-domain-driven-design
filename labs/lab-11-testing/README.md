# Lab 11: Teststrategie - Tests auf allen Ebenen

## Lernziel

Domain-, Repository-, Web- und Architektur-Tests schreiben.

## Dauer

60 Minuten

## Voraussetzungen

- Lab 10 abgeschlossen
- Slides Modul 15

## Aufgabe

Füge dem Projekt Tests auf verschiedenen Ebenen hinzu: Unit-Tests für Domain-Logik, Integrationstests für das Repository, Web-Tests für den REST-Adapter und ArchUnit-Tests für die Architektur.

### Teil 1: Domain-Unit-Test (kein Spring-Context!)

Teste die Invarianten des Aggregate Root `BrokerageProcess` ohne Spring-Context – reine JUnit-5-Tests:

1. **Negativtest:** `setStatusToNotaryAppointment()` wirft eine `IllegalStateException`, wenn kein angenommenes Angebot existiert
2. **Happy Path:** Angebot annehmen, dann Status auf NOTARTERMIN setzen – kein Fehler
3. **Besichtigung hinzufügen:** `addViewing()` erstellt eine Viewing und aktualisiert den Status
4. **Angebot annehmen:** `acceptOffer()` setzt `accepted` auf `true`

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

**Wichtig:** Kein `@SpringBootTest`, kein `@ExtendWith(SpringExtension.class)` – reine Unit-Tests!

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

### Teil 3: Web-/API-Test

Erstelle einen `@WebMvcTest` für den `ViewingController`:

```java
@WebMvcTest(ViewingController.class)
class ViewingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
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

### Teil 4: ArchUnit

Erweitere die ArchUnit-Tests aus Lab 08 um eine neue Regel:

```java
@ArchTest
static final ArchRule domain_events_should_be_records =
    classes()
        .that().resideInAPackage("..domain.event..")
        .should().beAssignableTo(Record.class);
```

**Neue Regel:** „Domain Events müssen Records sein" – alle Klassen im Package `..domain.event..` müssen Records sein.

### Bonus: Vollständiger Integrationstest

Erstelle einen `@SpringBootTest`-Integrationstest, der den vollständigen Ablauf überprüft:

1. Erstelle einen BrokerageProcess
2. Lege eine Besichtigung an (über den Use Case)
3. Überprüfe, dass der Prozess mit seiner Besichtigung gespeichert wurde

## Verifikation

Führe alle Tests aus:

```bash
cd ../../solutions/lab-11-testing
mvn test
```

Alle Tests müssen grün sein – mindestens 8 Tests.

## Tipps

- **Domain-Tests** benötigen keinen Spring-Context und sind daher sehr schnell.
- **`@DataJpaTest`** startet nur die JPA-Schicht mit einer eingebetteten H2-Datenbank.
- **`@WebMvcTest`** startet nur die Web-Schicht und mockt alle Abhängigkeiten.
- **ArchUnit** analysiert den kompilierten Bytecode und benötigt keinen laufenden Context.
- Verwende `@MockitoBean` in `@WebMvcTest`, um die Abhängigkeiten des Controllers zu mocken.
- Bei `@DataJpaTest` müssen Adapter-Klassen explizit per `@Import` hinzugefügt werden.
