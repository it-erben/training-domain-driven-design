---
marp: true
theme: default
paginate: true
header: "DDD & Clean Architecture mit Spring Boot 3"
footer: "© 2026 – Workshop S2090"
style: |
  section {
    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  }
  h1 {
    color: #2d6a4f;
  }
  h2 {
    color: #40916c;
  }
  code {
    background-color: #f0f0f0;
    border-radius: 4px;
    padding: 2px 6px;
  }
---

# Modul 14 – Teststrategie

## Die Testpyramide für Clean Architecture

**Geschätzte Dauer:** ca. 75 Minuten

### Lernziele

- Die Testpyramide auf die Clean Architecture Ringe abbilden
- Domain Unit Tests ohne Spring Context schreiben
- Application Service Tests mit gemockten Ports implementieren
- Repository Integration Tests mit `@DataJpaTest` umsetzen
- Web/API Tests mit `@WebMvcTest` schreiben
- InMemory-Repositories als leichtgewichtige Test-Doubles einsetzen

---

## Die Testpyramide

![Teststrategie](../diagrams/teststrategie-pyramide.drawio.png)

---

## Testpyramide → Clean Architecture Ringe

```
                    ┌─────────────┐
                    │   E2E Test  │  @SpringBootTest
                    │    (wenig)  │  Ring 1-4 komplett
                ┌───┴─────────────┴───┐
                │   Integration Tests  │  @DataJpaTest, @WebMvcTest
                │    (einige)          │  Ring 3 + 4
            ┌───┴─────────────────────┴───┐
            │   Application Service Tests  │  JUnit + Mocks
            │    (viele)                   │  Ring 2 (Ports gemockt)
        ┌───┴─────────────────────────────┴───┐
        │   Domain Unit Tests                  │  JUnit pur
        │    (sehr viele)                      │  Ring 1: reines Java
        └──────────────────────────────────────┘
```

| Ebene | Scope | Spring nötig? | Geschwindigkeit |
|-------|-------|---------------|-----------------|
| **Domain Unit** | Entities, VOs, Domain Services | Nein | < 10 ms |
| **Application Service** | Use Cases + gemockte Ports | Nein | < 50 ms |
| **Integration** | JPA, Controller | Teilweise | < 2 s |
| **E2E** | Gesamte Anwendung | Ja | > 5 s |

---

## Level 1: Domain Unit Tests

- **Kein Spring Context** — plain JUnit 5 + AssertJ
- Testen **Geschäftsinvarianten** und Domänenregeln
- Schnellste Tests in der Pyramide (Millisekunden)
- Höchste Aussagekraft für fachliche Korrektheit

### Was wird getestet?

- Aggregate Root: Zustandsübergänge, Invarianten
- Entity: Verhalten, Beziehungen
- Value Object: Erzeugung, Gleichheit, Berechnung
- Domain Service: Cross-Aggregate-Logik
- Domain Event: korrekte Erzeugung bei Zustandsänderung

---

## Domain Unit Test: Aggregate Root

```java
class VermittlungsvorgangTest {

    @Test
    void sollte_besichtigung_planen() {
        // Arrange
        var vorgang = Vermittlungsvorgang.erstellen(
            VorgangId.generate(),
            new ImmobilieId(UUID.randomUUID()),
            new KontaktId(UUID.randomUUID()));

        // Act
        var besichtigungId = vorgang.besichtigungPlanen(
            new KontaktId(UUID.randomUUID()),
            LocalDateTime.now().plusDays(3));

        // Assert
        assertThat(besichtigungId).isNotNull();
        assertThat(vorgang.getBesichtigungen()).hasSize(1);
    }

    @Test
    void sollte_fehler_werfen_wenn_vorgang_nicht_aktiv() {
        var vorgang = VermittlungsvorgangFixture.abgeschlossen();

        assertThatThrownBy(() -> vorgang.besichtigungPlanen(
                new KontaktId(UUID.randomUUID()),
                LocalDateTime.now().plusDays(1)))
            .isInstanceOf(VorgangNichtAktivException.class);
    }
}
```

---

## Domain Unit Test: Value Object

```java
class AdresseTest {

    @Test
    void sollte_leere_strasse_ablehnen() {
        assertThatThrownBy(() -> new Adresse("", "50667", "Köln"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Straße");
    }

    @Test
    void sollte_gleiche_adressen_als_equal_erkennen() {
        var a1 = new Adresse("Domstraße 1", "50667", "Köln");
        var a2 = new Adresse("Domstraße 1", "50667", "Köln");
        assertThat(a1).isEqualTo(a2);
    }
}

class PreisvorstellungTest {

    @Test
    void sollte_provision_korrekt_berechnen() {
        var preis = new Preisvorstellung(BigDecimal.valueOf(300_000));
        var provision = preis.berechneProvision(new Provisionssatz(3.57));
        assertThat(provision.betrag())
            .isEqualByComparingTo(BigDecimal.valueOf(10_710.00));
    }
}
```

---

## Domain Unit Test: Event-Erzeugung

```java
@Test
void sollte_domain_event_erzeugen_bei_besichtigungsplanung() {
    var vorgang = VermittlungsvorgangFixture.aktiv();

    vorgang.besichtigungPlanen(
        new KontaktId(UUID.randomUUID()),
        LocalDateTime.now().plusDays(3));

    assertThat(vorgang.domainEvents())
        .hasSize(1)
        .first()
        .isInstanceOf(BesichtigungGeplantEvent.class);
}
```

- Tests prüfen, dass Zustandsänderungen **Domain Events erzeugen**
- Kein Spring Context, kein EventPublisher — nur die Event-Liste prüfen
- Das ist der größte Vorteil des **Event Collection Patterns**

---

## Level 2: Application Service Tests

### Der Schlüssel-Vorteil von Clean Architecture

- Application Service hängt nur von **Ports** (Interfaces) ab
- Ports lassen sich einfach **mocken** oder durch **InMemory** ersetzen
- **Kein Spring Context** nötig — plain JUnit + Mockito

```java
class BesichtigungAnlegenUseCaseTest {

    private final VermittlungsvorgangRepository repository =
        new InMemoryVermittlungsvorgangRepository();
    private final DomainEventDispatcher eventDispatcher =
        mock(DomainEventDispatcher.class);
    private final BesichtigungAnlegenUseCase useCase =
        new BesichtigungAnlegenUseCase(repository, eventDispatcher);

    @Test
    void sollte_besichtigung_anlegen() {
        var vorgang = VermittlungsvorgangFixture.aktiv();
        repository.save(vorgang);

        var result = useCase.execute(new BesichtigungAnlegenCommand(
            vorgang.getId(), new KontaktId(UUID.randomUUID()),
            LocalDateTime.now().plusDays(3)));

        assertThat(result).isNotNull();
        verify(eventDispatcher).dispatchAll(anyList());
    }
}
```

---

## InMemory-Repository als Test-Double

```java
public class InMemoryVermittlungsvorgangRepository
        implements VermittlungsvorgangRepository {

    private final Map<VorgangId, Vermittlungsvorgang> store =
        new ConcurrentHashMap<>();

    @Override
    public void save(Vermittlungsvorgang vorgang) {
        store.put(vorgang.getId(), vorgang);
    }

    @Override
    public Optional<Vermittlungsvorgang> findById(VorgangId id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Vermittlungsvorgang> findByStatus(VorgangStatus status) {
        return store.values().stream()
            .filter(v -> v.getStatus() == status)
            .toList();
    }

    @Override
    public VorgangId nextId() { return VorgangId.generate(); }

    @Override
    public void delete(Vermittlungsvorgang vorgang) {
        store.remove(vorgang.getId());
    }
}
```

> InMemory-Repos sind **leichter als Mocks**: kein Stubbing nötig,
> realistische Abfragen möglich, wiederverwendbar.

---

## Application Service Test: Fehlerfall

```java
@Test
void sollte_fehler_werfen_wenn_vorgang_nicht_existiert() {
    // Repository ist leer — findById gibt Optional.empty()

    assertThatThrownBy(() -> useCase.execute(
            new BesichtigungAnlegenCommand(
                new VorgangId(UUID.randomUUID()),
                new KontaktId(UUID.randomUUID()),
                LocalDateTime.now().plusDays(1))))
        .isInstanceOf(VorgangNichtGefunden.class);

    verifyNoInteractions(eventDispatcher);
}
```

- Kein Spring Context, kein `@MockitoBean`
- Test läuft in **< 50 ms**
- InMemory-Repo liefert `Optional.empty()` → Exception
- Prüft, dass **keine Events dispatched** werden bei Fehler

---

## Level 3: Repository Integration Tests

- `@DataJpaTest` startet **nur JPA-relevante** Beans
- H2 In-Memory-Datenbank für schnelle Ausführung
- Testet **JPA-Mappings** und **Custom Queries** in der Infrastruktur

```java
@DataJpaTest
@Import(VorgangMapper.class)
class JpaVermittlungsvorgangRepositoryTest {

    @Autowired private VorgangSpringDataRepository springDataRepo;
    @Autowired private VorgangMapper mapper;

    private JpaVermittlungsvorgangRepository repository;

    @BeforeEach
    void setUp() {
        repository = new JpaVermittlungsvorgangRepository(
            springDataRepo, mapper);
    }

    @Test
    void sollte_vorgang_speichern_und_laden() {
        var vorgang = VermittlungsvorgangFixture.aktiv();
        repository.save(vorgang);

        var result = repository.findById(vorgang.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(VorgangStatus.AKTIV);
    }
}
```

---

## Level 4: Web/API Tests mit @WebMvcTest

```java
@WebMvcTest(BesichtigungController.class)
class BesichtigungControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private BesichtigungPlanen anlegenUseCase;

    @Test
    void sollte_besichtigung_anlegen_und_201_liefern() throws Exception {
        var expectedId = new BesichtigungId(UUID.randomUUID());
        when(anlegenUseCase.planen(any())).thenReturn(expectedId);

        mockMvc.perform(post("/api/v1/vermittlung/besichtigungen")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "vorgangId": "550e8400-e29b-41d4-a716-446655440000",
                      "interessentId": "660e8400-e29b-41d4-a716-446655440000",
                      "termin": "2026-04-15T14:00:00"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.besichtigungId")
                .value(expectedId.value().toString()));
    }
}
```

> Siehe auch Modul 09 für weitere `@WebMvcTest`-Beispiele.

---

## Level 5: Full Integration Tests

- Startet den **gesamten** ApplicationContext
- Testet das Zusammenspiel aller Schichten end-to-end
- **Sparsam einsetzen** — langsam und ressourcenintensiv

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class VermittlungIntegrationTest {

    @Autowired private TestRestTemplate restTemplate;

    @Test
    void sollte_besichtigung_anlegen_und_abrufen() {
        // Arrange: Vorgang über API erstellen
        var vorgangResponse = restTemplate.postForEntity(
            "/api/v1/vermittlung/vorgaenge", createVorgangRequest(),
            VorgangResponse.class);
        assertThat(vorgangResponse.getStatusCode())
            .isEqualTo(HttpStatus.CREATED);

        // Act: Besichtigung für den Vorgang anlegen
        var besichtigungResponse = restTemplate.postForEntity(
            "/api/v1/vermittlung/besichtigungen",
            createBesichtigungRequest(vorgangResponse.getBody().id()),
            BesichtigungResponse.class);

        // Assert
        assertThat(besichtigungResponse.getStatusCode())
            .isEqualTo(HttpStatus.CREATED);
    }
}
```

---

## Test Fixtures – Wiederverwendbare Testdaten

```java
public class VermittlungsvorgangFixture {

    public static Vermittlungsvorgang aktiv() {
        return Vermittlungsvorgang.erstellen(
            VorgangId.generate(),
            new ImmobilieId(UUID.randomUUID()),
            new KontaktId(UUID.randomUUID()));
    }

    public static Vermittlungsvorgang mitBesichtigung() {
        var vorgang = aktiv();
        vorgang.besichtigungPlanen(
            new KontaktId(UUID.randomUUID()),
            LocalDateTime.now().plusDays(3));
        return vorgang;
    }

    public static Vermittlungsvorgang abgeschlossen() {
        var vorgang = aktiv();
        // Vorgang durch alle Phasen führen...
        vorgang.abschliessen();
        return vorgang;
    }
}
```

- Jede Methode erzeugt ein **valides** Objekt im gewünschten Zustand
- Verwendet **Domain-Methoden** (nicht Reflection oder Builder)
- Wiederverwendbar in Domain-, Application- und Integration-Tests

---

## Test-Namenskonventionen

### Empfohlenes Schema (deutsch)

```
sollte_[erwartetes Verhalten]_wenn_[Bedingung]
```

| Testname | Beschreibung |
|----------|-------------|
| `sollte_besichtigung_planen()` | Happy Path |
| `sollte_fehler_werfen_wenn_vorgang_nicht_aktiv()` | Invariante |
| `sollte_domain_event_erzeugen_bei_besichtigung()` | Event-Prüfung |
| `sollte_404_liefern_wenn_vorgang_nicht_existiert()` | HTTP-Kontrakt |
| `sollte_409_bei_optimistic_locking_konflikt()` | Locking |

- Tests als **lebende Dokumentation** der Geschäftsregeln
- Konsistente Sprache im gesamten Team
- `@DisplayName` für lesbaren Test-Report optional

---

## AAA-Pattern: Arrange, Act, Assert

```java
@Test
void sollte_provision_korrekt_berechnen() {
    // Arrange – Testdaten vorbereiten
    var preis = new Preisvorstellung(BigDecimal.valueOf(300_000));
    var satz = new Provisionssatz(3.57);

    // Act – genau EINE Aktion ausführen
    var provision = preis.berechneProvision(satz);

    // Assert – Ergebnis prüfen
    assertThat(provision.betrag())
        .isEqualByComparingTo(BigDecimal.valueOf(10_710.00));
}
```

- **Arrange:** Setup, Fixtures, Test-Doubles
- **Act:** Genau **eine** Aktion pro Test
- **Assert:** Erwartung prüfen — möglichst spezifisch
- Kommentare `// Arrange`, `// Act`, `// Assert` verbessern Lesbarkeit

---

## Testverteilung: Empfehlung

| Level | Anteil | Geschwindigkeit | Was wird geprüft? |
|-------|--------|-----------------|-------------------|
| **Domain Unit** | ~60% | ms | Geschäftsregeln, Invarianten |
| **Application Service** | ~20% | < 50 ms | Use-Case-Orchestrierung |
| **Repository (@DataJpaTest)** | ~10% | < 1 s | JPA-Mapping, Custom Queries |
| **Web (@WebMvcTest)** | ~5% | < 2 s | HTTP-Kontrakte, Serialisierung |
| **E2E (@SpringBootTest)** | ~5% | > 5 s | Gesamtes Zusammenspiel |

> **Clean Architecture ermöglicht** ~80% der Tests ohne Spring Context.
> Das ist der **größte praktische Vorteil**.

---

## 🎯 Key Takeaway

> Clean Architecture macht Testen einfach:
> Wenn eure Domäne frei von Framework-Abhängigkeiten ist,
> könnt ihr den **wertvollsten Code** mit den **schnellsten Tests** abdecken.

```
Ohne Clean Architecture        Mit Clean Architecture
┌────────────────────┐         ┌────────────────────┐
│ 80% @SpringBootTest│         │ 5% @SpringBootTest │
│ 10% @WebMvcTest    │         │ 5% @WebMvcTest     │
│ 10% Unit Tests     │         │ 10% @DataJpaTest   │
│                    │         │ 20% App Service     │
│ → langsam, fragil  │         │ 60% Domain Unit    │
└────────────────────┘         │ → schnell, stabil  │
                               └────────────────────┘
```

---

## 🎯 Hands-on: Lab-12

### Aufgabe

1. Domain Unit Test: `Vermittlungsvorgang` Zustandsübergang testen
2. Domain Unit Test: Value Object Validierung und Gleichheit testen
3. Application Service Test: Use Case mit InMemory-Repository testen
4. Repository Integration Test: Custom Query mit `@DataJpaTest`
5. Web/API Test: `@WebMvcTest` für Controller-Endpunkt
6. **Bonus:** Test Fixture-Klasse erstellen

> **Dauer:** ca. 45 Minuten
> Details und Aufgabenstellung im **Lab-12**

---

## 💬 Diskussion

> Welche Teststrategie verfolgt ihr aktuell?

- Wie sieht eure aktuelle **Testpyramide** aus — oder ist es eher ein "Test-Eisbecher"?
- Wie viel Prozent eurer Tests laufen **ohne Spring Context**?
- Nutzt ihr **InMemory-Repositories** als Test-Doubles?
- Welche Test-Ebene bereitet euch die **meisten Probleme**?
- Schreibt ihr Tests **vor** oder **nach** dem Produktivcode (TDD)?
