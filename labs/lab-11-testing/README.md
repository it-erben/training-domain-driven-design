# Lab 11: Teststrategie -- Tests auf allen Ebenen

## Lernziel

Domain-, Repository-, Web- und Architektur-Tests schreiben.

## Dauer

60 Minuten

## Voraussetzungen

- Lab 10 abgeschlossen
- Slides Modul 14

## Aufgabe

Ergaenze das Projekt um Tests auf verschiedenen Ebenen: Unit-Tests fuer die Domain-Logik, Integrationstests fuer das Repository, Web-Tests fuer den REST-Adapter und ArchUnit-Tests fuer die Architektur.

### Teil 1: Domain Unit Test (kein Spring-Kontext!)

Teste die Invariante des Aggregate Root `Vermittlungsvorgang` ohne Spring-Kontext -- reine JUnit-5-Tests:

1. **Negativtest:** `statusAufNotarterminSetzen()` wirft eine `IllegalStateException`, wenn kein angenommenes Angebot vorliegt
2. **Happy Path:** Angebot annehmen, dann Status auf NOTARTERMIN setzen -- kein Fehler
3. **Besichtigung hinzufuegen:** `besichtigungHinzufuegen()` erstellt eine Besichtigung und setzt den Status
4. **Angebot annehmen:** `angebotAnnehmen()` setzt `angenommen` auf `true`

```java
class VermittlungsvorgangTest {

    @Test
    void test_statusAufNotartermin_ohneAngenommenesAngebot_wirftException() {
        // Arrange: neuen Vermittlungsvorgang erstellen
        // Act & Assert: statusAufNotarterminSetzen() -> IllegalStateException
    }

    @Test
    void test_statusAufNotartermin_mitAngenommenemAngebot_erfolgreich() {
        // Arrange: Angebot hinzufuegen und annehmen
        // Act: statusAufNotarterminSetzen()
        // Assert: Status ist NOTARTERMIN
    }
}
```

**Wichtig:** Kein `@SpringBootTest`, kein `@ExtendWith(SpringExtension.class)` -- reine Unit-Tests!

### Teil 2: Repository Integration Test

Erstelle einen `@DataJpaTest` fuer den `VermittlungsvorgangRepositoryAdapter`:

```java
@DataJpaTest
@Import(VermittlungsvorgangRepositoryAdapter.class)
class VermittlungsvorgangRepositoryAdapterTest {

    @Autowired
    private VermittlungsvorgangRepositoryAdapter repository;

    @Test
    void test_saveAndFindById() {
        // Vermittlungsvorgang speichern und laden
    }

    @Test
    void test_besichtigungenWerdenPersistiert() {
        // Vermittlungsvorgang mit Besichtigung speichern
        // Laden und pruefen, dass Besichtigung vorhanden
    }
}
```

### Teil 3: Web/API Test

Erstelle einen `@WebMvcTest` fuer den `BesichtigungController`:

```java
@WebMvcTest(BesichtigungController.class)
class BesichtigungControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BesichtigungAnlegenUseCase useCase;

    @Test
    void test_besichtigungAnlegen_gueltigerRequest_201() {
        // POST /api/vermittlungsvorgaenge/{id}/besichtigungen mit gueltigem Body -> 201
    }

    @Test
    void test_besichtigungAnlegen_ungueltigerRequest_422() {
        // POST mit ungueltigem Body -> 422
    }

    @Test
    void test_besichtigungAnlegen_vorgangNichtGefunden_404() {
        // POST mit unbekanntem vorgangId -> 404
    }
}
```

### Teil 4: ArchUnit

Erweitere die ArchUnit-Tests aus Lab-08 um eine neue Regel:

```java
@ArchTest
static final ArchRule domain_events_should_be_records =
    classes()
        .that().resideInAPackage("..domain.event..")
        .should().beAssignableTo(Record.class);
```

**Neue Regel:** "Domain events should be records" -- alle Klassen im Package `..domain.event..` muessen Records sein.

### Bonus: Full-Integration-Test

Erstelle einen `@SpringBootTest` Full-Integration-Test, der den kompletten Flow testet:

1. Vermittlungsvorgang erstellen
2. Besichtigung anlegen (ueber den Use Case)
3. Pruefen, dass der Vorgang mit Besichtigung gespeichert wurde

## Verifikation

Fuehre alle Tests aus:

```bash
cd solution
mvn test
```

Alle Tests muessen gruen sein -- mindestens 8 Tests.

## Tipps

- **Domain-Tests** brauchen keinen Spring-Kontext und sind daher sehr schnell.
- **`@DataJpaTest`** startet nur den JPA-Layer mit einer eingebetteten H2-Datenbank.
- **`@WebMvcTest`** startet nur den Web-Layer und mockt alle Abhaengigkeiten.
- **ArchUnit** analysiert den kompilierten Bytecode und braucht keinen laufenden Kontext.
- Verwende `@MockBean` in `@WebMvcTest`, um die Abhaengigkeiten des Controllers zu mocken.
- In `@DataJpaTest` muessen Adapter-Klassen explizit mit `@Import` hinzugefuegt werden.
