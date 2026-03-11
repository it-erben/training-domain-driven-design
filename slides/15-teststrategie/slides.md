---
marp: true
theme: default
paginate: true
header: "DDD & Clean Architecture mit Spring Boot 4"
footer: "CC BY-NC-SA 4.0, Alexander Erben"
---

# Modul 15 - Teststrategie

## Die Testpyramide für Clean Architecture

Geschätzte Dauer: ca. 75 Minuten

---

## Lernziele

- Die Testpyramide auf die Clean Architecture Ringe abbilden
- Domain Unit Tests ohne Spring Context schreiben
- Application Service Tests mit gemockten Ports implementieren
- Repository Integration Tests mit `@DataJpaTest` umsetzen
- Web/API Tests mit `@WebMvcTest` schreiben
- InMemory-Repositories als leichtgewichtige Test-Doubles einsetzen
- `@DisplayName`-Konvention und AAA-Muster anwenden
- Parametrisierte Tests mit `@ParameterizedTest` + `@CsvSource` schreiben

---

## Die Testpyramide

![h:450](images/testpyramide.drawio.svg)

---

| Ebene | Scope | Spring nötig? | Geschwindigkeit |
|-------|-------|---------------|-----------------|
| Domain Unit | Entities, VOs, Domain Services | Nein | < 10 ms |
| Application Service | Use Cases + gemockte Ports | Nein | < 50 ms |
| Integration | JPA, Controller | Teilweise | < 2 s |
| E2E | Gesamte Anwendung | Ja | > 5 s |

---
<style scoped>section { font-size: 1.7em; }</style>

## Domain Unit Tests - das Fundament

Die Basis der Pyramide: plain JUnit 5 + AssertJ, kein Spring Context.
Diese Tests prüfen Geschäftsinvarianten und Domänenregeln direkt -
und laufen in Millisekunden.

Was fällt darunter?

- Aggregate Root: Zustandsübergänge, Invarianten
- Value Objects: Erzeugung, Gleichheit, Berechnung
- Domain Services und Event-Erzeugung bei Zustandsänderungen

---
<style scoped>section { font-size: 1.3em; }</style>

## Aggregate Root testen

```java
class AntragsMappeTest {

    @Test
    void fluerstueck_hinzufuegen_funktioniert() {
        // Arrange
        var mappe = AntragsMappe.erstellen(
            new AntragId(UUID.randomUUID()),
            new RegistrierungsNummer("DZ-BW-2024-0042"),
            new Foerderbetrag(BigDecimal.ZERO, "EUR"));

        // Act
        var flurstueckId = mappe.flurstueckHinzufuegen(
            new FlurstueckNummer("BW-0012-0034-0001"),
            new BigDecimal("3.75"));

        // Assert
        assertThat(flurstueckId).isNotNull();
        assertThat(mappe.getFlurstuecke()).hasSize(1);
    }

    @Test
    void einreichen_fehlschlagen_wenn_kein_flurstueck() {
        var mappe = AntragsMappeFixture.leer();

        assertThatThrownBy(() -> mappe.einreichen())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Flurstück");
    }
}
```

---
<style scoped>section { font-size: 1.3em; }</style>

## Testkonventionen: @DisplayName und AAA

Lesbare Tests helfen dem Team, die Domäne zu verstehen — nicht nur zu verifizieren.

```java
class FoerderquoteTest {

    @Test
    @DisplayName("negativer Prozentsatz wird abgelehnt")
    void negativer_prozentsatz_wird_abgelehnt() {
        // Arrange
        BigDecimal negativerProzentsatz = new BigDecimal("-0.1");

        // Act & Assert
        assertThatThrownBy(() -> new Foerderquote(negativerProzentsatz))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Foerderquote");
    }
}
```

- **AAA-Muster:** Arrange / Act / Assert — Kommentare helfen beim Lesen
- **@DisplayName:** Fachliche Beschreibung in Deutsch — spricht Ubiquitous Language
- Testnamen sind ausführbare Spezifikationen der Fachregeln
- Methodennamen in `snake_case` oder `camelCase` — Team-Konvention wählen und durchhalten

---
<style scoped>section { font-size: 1.35em; }</style>

## Parametrisierte Tests: viele Szenarien, ein Test

```java
@DisplayName("verschiedene Foerderquoten liefern korrekte Förderbeträge")
@ParameterizedTest(name = "Foerderquote {0} ergibt Betrag {1} EUR")
@CsvSource({
    "0.00,   0.00",
    "0.35, 3500.00",
    "1.00, 10000.00"
})
void shouldCalculateCorrectAmountForDifferentQuotes(
        String quotePct, String expectedBetrag) {

    // Arrange
    var betrag = new Foerderbetrag(new BigDecimal("10000"), "EUR");
    var quote  = new Foerderquote(new BigDecimal(quotePct));

    // Act
    var ergebnis = betrag.anwenden(quote);

    // Assert
    assertThat(ergebnis.betrag())
        .isEqualByComparingTo(new BigDecimal(expectedBetrag));
}
```

- `@CsvSource` — kompakte Tabellenform, gut lesbar im Code
- `@ParameterizedTest(name = "...")` — jeder Fall erscheint mit eigenem Namen im Report
- Deckt Grenzfälle (0, 100%) mit minimalem Boilerplate ab

---

## Value Objects und Berechnung testen

```java
class FlurstueckNummerTest {

    @Test
    void soll_leere_nummer_ablehnen() {
        assertThatThrownBy(() -> new FlurstueckNummer(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("FlurstueckNummer");
    }

    @Test
    void gleiche_nummern_sind_gleich() {
        var n1 = new FlurstueckNummer("BW-0012-0034-0001");
        var n2 = new FlurstueckNummer("BW-0012-0034-0001");
        assertThat(n1).isEqualTo(n2);
    }
}

class FoerderbetragTest {

    @Test
    void foerderquote_wird_korrekt_berechnet() {
        var betrag = new Foerderbetrag(new BigDecimal("10000"), "EUR");
        var quote = new Foerderquote(new BigDecimal("0.35")); // 35 %
        // Foerderbetrag.anwenden(Foerderquote) ist definiert in Modul 06
        var ergebnis = betrag.anwenden(quote);
        assertThat(ergebnis.betrag())
            .isEqualByComparingTo(new BigDecimal("3500.00"));
    }
}
```

---
<style scoped>section { font-size: 1.7em; }</style>

## Werden die richtigen Events erzeugt?

```java
@Test
void fluerstueck_hinzufuegen_erzeugt_domain_event() {
    var mappe = AntragsMappeFixture.aktiv();

    mappe.flurstueckHinzufuegen(
        new FlurstueckNummer("BW-0012-0034-0001"),
        new BigDecimal("3.75"));

    assertThat(mappe.domainEvents())
        .hasSize(1)
        .first()
        .isInstanceOf(FlurstueckHinzugefuegt.class);
}
```

- Tests prüfen, dass Zustandsänderungen Domain Events erzeugen
- Kein Spring Context, kein EventPublisher — nur die Event-Liste prüfen
- Das ist der größte Vorteil des Event Collection Patterns

---
<style scoped>section { font-size: 1.7em; }</style>

## Application Service Tests

Hier zeigt sich ein Vorteil der Clean Architecture: Der Application Service
hängt nur von Ports ab. Ports lassen sich einfach mocken oder durch
InMemory-Implementierungen ersetzen - kein Spring Context nötig.

---
<style scoped>section { font-size: 1.7em; }</style>

```java
class FlurstueckHinzufuegenServiceTest {

    private final AntragsMappeRepository repository =
        new InMemoryAntragsMappeRepository();
    private final ApplicationEventPublisher eventPublisher =
        mock(ApplicationEventPublisher.class);
    private final FlurstueckHinzufuegenService useCase =
        new FlurstueckHinzufuegenService(repository, eventPublisher);

    @Test
    void flurstueck_wird_hinzugefuegt() {
        var mappe = AntragsMappeFixture.aktiv();
        repository.save(mappe);

        var result = useCase.hinzufuegen(new FlurstueckHinzufuegenCommand(
            mappe.getId(), new FlurstueckNummer("BW-0012-0034-0001"),
            new BigDecimal("3.75")));

        assertThat(result).isNotNull();
        verify(eventPublisher, atLeastOnce()).publishEvent(any());
    }
}
```

---
<style scoped>section { font-size: 1.7em; }</style>

## Leichtgewichtiger als Mocks: InMemory-Repositories

```java
public class InMemoryAntragsMappeRepository
        implements AntragsMappeRepository {

    private final Map<AntragId, AntragsMappe> store =
        new ConcurrentHashMap<>();

    @Override
    public void save(AntragsMappe mappe) {
        store.put(mappe.getId(), mappe);
    }

    @Override
    public Optional<AntragsMappe> findById(AntragId id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<AntragsMappe> findByRegistrierungsNummer(
            RegistrierungsNummer nr) {
        return store.values().stream()
            .filter(m -> m.getRegistrierungsNummer().equals(nr))
            .findFirst();
    }

    @Override
    public void delete(AntragsMappe mappe) {
        store.remove(mappe.getId());
    }
}
```

> InMemory-Repos sind leichter als Mocks: kein Stubbing nötig,
> realistische Abfragen möglich, wiederverwendbar.

---
<style scoped>section { font-size: 1.7em; }</style>

## Was passiert bei fehlenden Daten?

```java
@Test
void soll_exception_werfen_wenn_antragsmappe_nicht_gefunden() {
    // Repository ist leer — findById liefert Optional.empty()

    assertThatThrownBy(() -> useCase.hinzufuegen(
            new FlurstueckHinzufuegenCommand(
                new AntragId(UUID.randomUUID()),
                new FlurstueckNummer("BW-0012-0034-0001"),
                new BigDecimal("3.75"))))
        .isInstanceOf(AntragsmappeNichtGefundenException.class);

    verifyNoInteractions(eventPublisher);
}
```

- Kein Spring Context, kein `@MockitoBean`
- Test läuft in < 50 ms
- InMemory repo returns `Optional.empty()` → Exception
- Prüft, dass keine Events dispatched werden bei Fehler

---

## Repository-Tests mit @DataJpaTest

Ab hier verlassen wir die reine Unit-Ebene. `@DataJpaTest` startet nur
JPA-relevante Beans mit H2 In-Memory-Datenbank und testet JPA-Mappings
und Custom Queries in der Infrastruktur.

---
<style scoped>section { font-size: 1.5em; }</style>

```java
@DataJpaTest
@Import(AntragsMappeMapper.class)
class JpaAntragsMappeRepositoryTest {

    @Autowired private AntragsMappeSpringDataRepository springDataRepo;
    @Autowired private AntragsMappeMapper mapper;

    private JpaAntragsMappeRepository repository;

    @BeforeEach
    void setUp() {
        repository = new JpaAntragsMappeRepository(
            springDataRepo, mapper);
    }

    @Test
    void soll_antragsmappe_speichern_und_laden() {
        var mappe = AntragsMappeFixture.mitFlurstueck(); // Status: IN_BEARBEITUNG
        repository.save(mappe);

        var result = repository.findById(mappe.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(AntragStatus.IN_BEARBEITUNG);
    }
}
```

---
<style scoped>section { font-size: 1.3em; }</style>

## HTTP-Kontrakte prüfen mit @WebMvcTest

```java
@WebMvcTest(FlurstueckController.class)
class FlurstueckControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private FlurstueckHinzufuegen hinzufuegenUseCase;

    @Test
    void soll_flurstueck_erstellen_und_201_zurueckgeben() throws Exception {
        var expectedId = new FlurstueckId(UUID.randomUUID());
        when(hinzufuegenUseCase.hinzufuegen(any())).thenReturn(
            new FlurstueckHinzufuegenResult(expectedId,
                new AntragId(UUID.randomUUID()),
                new FlurstueckNummer("BW-0012-0034-0001"),
                new BigDecimal("3.75")));

        mockMvc.perform(post("/api/v1/antragstellung/flurstuecke")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "antragsmappeId": "550e8400-e29b-41d4-a716-446655440000",
                      "flurstueckNummer": "BW-0012-0034-0001",
                      "flaeche": 3.75
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.flurstueckId")
                .value(expectedId.value().toString()));
    }
}
```

> Siehe auch Modul 10 für weitere `@WebMvcTest`-Beispiele.

---
<style scoped>section { font-size: 1.2em; }</style>

## Die Spitze der Pyramide: Full Integration Tests

Hier startet der gesamte ApplicationContext. Diese Tests sind langsam und
ressourcenintensiv, prüfen dafür aber das Zusammenspiel aller Schichten.

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class AntragstellungIntegrationTest {

    @Autowired private TestRestTemplate restTemplate;

    @Test
    void flurstueck_hinzufuegen_und_laden() {
        // Arrange: AntragsMappe anlegen via API
        var mappeResponse = restTemplate.postForEntity(
            "/api/v1/antragstellung/antragsmappen", createMappeRequest(),
            AntragsmappeResponse.class);
        assertThat(mappeResponse.getStatusCode())
            .isEqualTo(HttpStatus.CREATED);

        // Act: Flurstück hinzufügen
        var flurstueckResponse = restTemplate.postForEntity(
            "/api/v1/antragstellung/flurstuecke",
            createFlurstueckRequest(mappeResponse.getBody().id()),
            FlurstueckResponse.class);

        // Assert
        assertThat(flurstueckResponse.getStatusCode())
            .isEqualTo(HttpStatus.CREATED);
    }
}
```

---
<style scoped>section { font-size: 1.2em; }</style>

## Test Fixtures - Wiederverwendbare Testdaten

```java
public class AntragsMappeFixture {

    public static AntragsMappe aktiv() {
        return AntragsMappe.erstellen(
            new AntragId(UUID.randomUUID()),
            new RegistrierungsNummer("DZ-BW-2024-0042"),
            new Foerderbetrag(new BigDecimal("50000"), "EUR"));
    }

    public static AntragsMappe mitFlurstueck() {
        var mappe = aktiv();
        mappe.flurstueckHinzufuegen(
            new FlurstueckNummer("BW-0012-0034-0001"),
            new BigDecimal("3.75"));
        return mappe;
    }

    public static AntragsMappe leer() {
        return AntragsMappe.erstellen(
            new AntragId(UUID.randomUUID()),
            new RegistrierungsNummer("DZ-BW-2024-9999"),
            new Foerderbetrag(BigDecimal.ZERO, "EUR"));
    }
}
```

- Jede Methode erzeugt ein valides Objekt im gewünschten Zustand
- Verwendet Domain-Methoden (nicht Reflection oder Builder)
- Wiederverwendbar in Domain-, Application- und Integration-Tests

---
<style scoped>section { font-size: 1.8em; }</style>

## Testverteilung: Empfehlung

| Level | Anteil | Geschwindigkeit | Was wird geprüft? |
|-------|--------|-----------------|-------------------|
| Domain Unit | ~60% | ms | Geschäftsregeln, Invarianten |
| Application Service | ~20% | < 50 ms | Use-Case-Orchestrierung |
| Repository (@DataJpaTest) | ~10% | < 1 s | JPA-Mapping, Custom Queries |
| Web (@WebMvcTest) | ~5% | < 2 s | HTTP-Kontrakte, Serialisierung |
| E2E (@SpringBootTest) | ~5% | > 5 s | Gesamtes Zusammenspiel |

> Clean Architecture ermöglicht ~80% der Tests ohne Spring Context.
> Das ist der größte praktische Vorteil.

---

## Reflexion: Prüft euer Verständnis

1. Warum sollen ~60% der Tests Domain Unit Tests sein — und nicht Integration Tests?
2. Was ist der Vorteil eines InMemory-Repositories gegenüber einem Mock?
3. Wann braucht ihr `@SpringBootTest` wirklich — und wann reicht JUnit 5 allein?

---

## Zusammenfassung

> Clean Architecture macht Testen einfacher: Wenn eure Domäne frei von Framework-Abhängigkeiten ist, könnt ihr den wertvollsten Code mit den schnellsten Tests abdecken.

- `@DisplayName("fachliche Aussage auf Deutsch")` — Tests als ausführbare Spezifikation
- `@ParameterizedTest` + `@CsvSource` — Grenzfälle kompakt, lesbar, wartbar
- InMemory-Repositories > Mocks für Application-Service-Tests
- Domain Unit Tests (~60%): kein Spring, läuft in Millisekunden

![h:200](images/vergleich-ohne-mit-clean-architecture.drawio.svg)

### Zum Nachlesen

- JUnit 5 User Guide: `@ParameterizedTest`, `@CsvSource`, `@DisplayName`
- AssertJ Docs: `assertThatThrownBy`, Soft Assertions
- Santana, „Domain-Driven Design with Java" (2026), Kap. 4: Testing and Validating DDD Applications

---

## Hands-on: Lab 12

### Aufgabe

1. Domain Unit Test: `AntragsMappe` Zustandsübergang testen
2. Domain Unit Test: Value Object Validierung und Gleichheit testen
3. Application Service Test: Use Case mit InMemory-Repository testen
4. Repository Integration Test: Custom Query mit `@DataJpaTest`
5. Web/API Test: `@WebMvcTest` für `FlurstueckController`-Endpunkt
6. Bonus: `AntragsMappeFixture`-Klasse erstellen

> Dauer: ca. 45 Minuten

---

## Diskussion: Teststrategie in der Praxis

> Ein typisches Problem in gewachsenen Spring-Boot-Projekten:
> **"`@SpringBootTest` wird reflexartig eingesetzt, auch wo es nicht nötig ist."**
> Ziele: schnellere Tests, keine flaky Tests, bessere Wartbarkeit.

- Wie sieht eure aktuelle Testpyramide aus - oder ist es eher ein "Test-Eisbecher"?
- Wie viel Prozent eurer Tests laufen ohne Spring Context?
- Welche Tests in euren Modulen könnten heute Domain Unit Tests sein, statt `@SpringBootTest`?
- Nutzt ihr InMemory-Repositories als Test-Doubles?
- **Bei Clean Architecture:** Warum sind ~60% der Tests reine Domain Unit Tests möglich?
- Schreibt ihr Tests vor oder nach dem Produktivcode?

**Tests und Implementierungsdetails:**

> Santana, „Domain-Driven Design with Java" (2026), Kap. 12 (S. 203):
> *„Tests that tightly couple themselves to implementation details rather than
> business behavior become a maintenance burden: every internal refactoring breaks
> a test, even if the observable behavior is unchanged."*

- Welche eurer Tests prüfen **Verhalten** — und welche prüfen nur interne Implementierung?
- Wie unterscheidet sich ein Test auf `AntragsMappe.einreichen()` von einem Mock-Test
  auf `save()` im Repository?
- Was passiert, wenn ein Test für ein refaktoriertes Aggregate bricht, obwohl die
  fachliche Regel dieselbe geblieben ist?
- Ist ein Test, der intern umstrukturierten Code bricht, ein Zeichen für schlechten Code
  — oder für schlechten Test?
