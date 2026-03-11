---
marp: true
theme: default
paginate: true
header: "DDD & Clean Architecture mit Spring Boot 4"
footer: "CC BY-NC-SA 4.0, Alexander Erben"
---

# Modul 11 - ArchUnit

## Architekturregeln als ausführbare Tests

---

## Lernziele

- ArchUnit als Werkzeug für automatisierte Architektur-Governance kennen
- Architekturregeln als JUnit-Tests schreiben
- Clean-Architecture-Regeln mit ArchUnit durchsetzen
- Namenskonventionen und Annotation-Regeln formulieren
- Onion Architecture als vordefinierte Form nutzen
- Architektur-Baseline für bestehenden Code einsetzen
- jMolecules-Annotationen mit ArchUnit kombinieren

---

## Warum automatisierte Architektur-Governance?

### Das Problem

```
Woche 1:  Architektur sauber, alle halten sich dran       [OK]
Woche 4:  "Nur diese eine Abkürzung..."                   [WARNUNG]
Woche 12: Domain importiert JPA, Controller hat Logik     [FEHLER]
Woche 24: "Wir müssen die Architektur neu aufsetzen"      [GAME OVER]
```

---

## Die Lösung

- Architekturregeln als ausführbare Tests formulieren
- Laufen bei jedem Build - Verstöße brechen die Pipeline
- Keine zusätzliche Infrastruktur nötig - normaler JUnit-Test

> Architekturregeln, die nicht automatisch geprüft werden,
> werden früher oder später gebrochen.

---
<style scoped>section { font-size: 1.7em; }</style>

## Was ist ArchUnit?

- Java-Bibliothek von TNG Technology Consulting
- Architekturregeln als JUnit 5 Tests formuliert
- Prüft: Paketabhängigkeiten, Annotationen, Vererbung, Namenskonventionen
- Läuft als normaler Unit-Test - `mvn test` genügt
- Open Source, aktiv gepflegt (aktuell 1.3.x)

### Maven Dependency

```xml
<dependency>
    <groupId>com.tngtech.archunit</groupId>
    <artifactId>archunit-junit5</artifactId>
    <version>1.3.0</version>
    <scope>test</scope>
</dependency>
```

---
<style scoped>section { font-size: 1.7em; }</style>

## Grundlegende API

```java
@AnalyzeClasses(packages = "de.foerderung")
class ArchitectureRulesTest {

    @ArchTest
    static final ArchRule domain_has_no_spring_imports =
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
                .resideInAnyPackage(
                    "org.springframework..",
                    "jakarta.persistence..",
                    "jakarta.transaction..")
            .as("Domain must not have Spring/JPA dependencies");
}
```

- `@AnalyzeClasses` - welche Packages werden analysiert
- `@ArchTest` statt `@Test` - ArchUnit erkennt die Regel automatisch
- Fluent API: `classes().that()...should()...`

---
<style scoped>section { font-size: 1.7em; }</style>

## Die API im Überblick

```
ArchRuleDefinition
  ├── classes()        → Klassen selektieren
  ├── noClasses()      → Negativprüfung ("keine Klasse darf...")
  ├── methods()        → Methoden selektieren
  └── fields()         → Felder selektieren

Selektoren (.that())
  ├── resideInAPackage("..domain..")
  ├── areAnnotatedWith(Service.class)
  ├── haveSimpleNameEndingWith("Controller")
  ├── implement(SomeInterface.class)
  └── areNotInterfaces()

Bedingungen (.should())
  ├── onlyDependOnClassesThat(...)
  ├── notBeAnnotatedWith(...)
  ├── onlyBeAccessed().byAnyPackage(...)
  ├── haveSimpleNameEndingWith(...)
  └── resideInAPackage(...)
```

---
<style scoped>section { font-size: 1.7em; }</style>

## Regel 1: Domain ist Framework-frei

```java
@ArchTest
static final ArchRule domain_is_framework_free =
    noClasses()
        .that().resideInAPackage("..domain..")
        .should().dependOnClassesThat()
            .resideInAnyPackage(
                "org.springframework..",
                "jakarta.persistence..",
                "jakarta.transaction..",
                "com.fasterxml.jackson..")
        .as("Domain must not have framework dependencies");
```

- Sichert die Dependency Rule der Clean Architecture ab
- Domain ist der innerste Ring - kennt nur `java.*` und sich selbst
- Verstöße brechen den Build sofort

---
<style scoped>section { font-size: 1.7em; }</style>

## Regel 2: Domain kennt keine äußeren Ringe

```java
@ArchTest
static final ArchRule domain_does_not_know_outer_rings =
    noClasses()
        .that().resideInAPackage("..domain..")
        .should().dependOnClassesThat()
            .resideInAnyPackage(
                "..application..",
                "..infrastructure..",
                "..adapter..")
        .as("Domain must not access outer rings");
```

```java
@ArchTest
static final ArchRule application_does_not_know_infrastructure =
    noClasses()
        .that().resideInAPackage("..application..")
        .should().dependOnClassesThat()
            .resideInAnyPackage(
                "..infrastructure..",
                "..adapter..")
        .as("Application must not access Infrastructure/Adapter");
```

---
<style scoped>section { font-size: 1.7em; }</style>

## Regel 3: Annotationen am richtigen Ort

```java
@ArchTest
static final ArchRule rest_controller_only_in_adapter_web =
    noClasses()
        .that().resideOutsideOfPackage("..adapter.web..")
        .should().beAnnotatedWith(RestController.class)
        .as("@RestController only allowed in adapter.web");

@ArchTest
static final ArchRule entity_only_in_infrastructure =
    noClasses()
        .that().resideOutsideOfPackage("..infrastructure..")
        .should().beAnnotatedWith(
            jakarta.persistence.Entity.class)
        .as("@Entity only allowed in infrastructure");

@ArchTest
static final ArchRule transactional_only_in_application =
    noClasses()
        .that().resideOutsideOfPackage("..application..")
        .should().beAnnotatedWith(Transactional.class)
        .as("@Transactional only allowed in application");
```

---
<style scoped>section { font-size: 1.7em; }</style>

## Regel 4: Namenskonventionen

```java
@ArchTest
static final ArchRule controllers_named_controller =
    classes()
        .that().areAnnotatedWith(RestController.class)
        .should().haveSimpleNameEndingWith("Controller")
        .as("REST controllers should end with 'Controller'");

@ArchTest
static final ArchRule request_dtos_named_request =
    classes()
        .that().resideInAPackage("..adapter.web..")
        .and().haveSimpleNameEndingWith("Request")
        .should().beRecords()
        .as("Request DTOs should be records");

@ArchTest
static final ArchRule services_implement_port =
    classes()
        .that().resideInAPackage("..application.service..")
        .and().areAnnotatedWith(Service.class)
        .should().implement(
            resideInAPackage("..application.port.."))
        .as("Application services should implement a port");
```

---
<style scoped>section { font-size: 1.7em; }</style>

## Vordefinierte Architekturform: Onion Architecture

```java
@ArchTest
static final ArchRule onion_architecture =
    Architectures.onionArchitecture()
        .domainModels("..domain.model..", "..domain.event..")
        .domainServices("..domain.port..")
        .applicationServices("..application..")
        .adapter("web", "..adapter.web..")
        .adapter("persistence", "..infrastructure.persistence..")
        .adapter("config", "..infrastructure.config..");
```

- ArchUnit bietet vordefinierte Architekturformen an
- `onionArchitecture()` prüft automatisch alle Abhängigkeitsregeln
- Weniger Code als einzelne Regeln - aber weniger granulare Fehlermeldungen
- Alternative: `layeredArchitecture()` für klassische Schichtarchitekturen

---
<style scoped>section { font-size: 1.7em; }</style>

## Layered Architecture - Alternative

```java
@ArchTest
static final ArchRule layered_architecture =
    Architectures.layeredArchitecture()
        .consideringOnlyDependenciesInLayers()
        .layer("Adapter").definedBy("..adapter..")
        .layer("Application").definedBy("..application..")
        .layer("Infrastructure").definedBy("..infrastructure..")
        .layer("Domain").definedBy("..domain..")
        .whereLayer("Adapter")
            .mayNotBeAccessedByAnyLayer()
        .whereLayer("Infrastructure")
            .mayNotBeAccessedByAnyLayer()
        .whereLayer("Application")
            .mayOnlyBeAccessedByLayers("Adapter")
        .whereLayer("Domain")
            .mayOnlyBeAccessedByLayers(
                "Application", "Infrastructure", "Adapter");
```

> `onionArchitecture()` ist passender für Clean Architecture,
> `layeredArchitecture()` für traditionelle Schichten.

---
<style scoped>section { font-size: 1.7em; }</style>

## Cross-BC-Regeln: Bounded Context Isolation

```java
@ArchTest
static final ArchRule antragstellung_greift_nicht_auf_auszahlung_zu =
    noClasses()
        .that().resideInAPackage("..antragstellung.domain..")
        .should().dependOnClassesThat()
            .resideInAPackage("..auszahlung.domain..")
        .as("Antragstellung domain darf nicht direkt auf "
            + "Auszahlung domain zugreifen");

@ArchTest
static final ArchRule bcs_kommunizieren_nur_ueber_events =
    slices().matching("de.foerderung.(*).domain..")
        .should().notDependOnEachOther()
        .as("Domain-Schichten verschiedener BCs "
            + "duerfen nicht direkt voneinander abhaengen");
```

- Stellt sicher, dass Bounded Contexts isoliert bleiben
- Kommunikation zwischen BCs nur über Events oder definierte APIs
- `slices()` prüft alle BC-Kombinationen auf einmal
- **Typisch in gewachsenen Systemen:** Zahlreiche MDBs kommunizieren ohne diese Isolation!

---
<style scoped>section { font-size: 1.7em; }</style>

## Architektur-Baseline: Legacy-Code schrittweise verbessern

### Problem: Zahlreiche MDBs nutzen `AntragsmappeAenderung` direkt — Build würde sofort brechen

```java
// FreezingArchRule: "freeze" existing violations
@ArchTest
static final ArchRule keine_direkten_mdb_importe =
    FreezingArchRule.freeze(
        noClasses()
            .that().resideInAPackage("..mdb..")
            .should().dependOnClassesThat()
                .resideInAPackage("..antragstellung.."));
```

- Erster Lauf: alle bestehenden Verstöße werden in `archunit_store/` gespeichert
- Folgende Läufe: nur *neue* Verstöße brechen den Build — keine neuen MDBs ohne ACL!
- Bestehende Verstöße werden schrittweise abgebaut
- `archunit_store/` in git committen → Team sieht Fortschritt

> **Sofort einsetzbar in bestehenden Modulen:** Regeln einführen, ohne alle Abhängigkeiten auf einmal refactorn zu müssen.

---
<style scoped>section { font-size: 1.7em; }</style>

## Integration in CI/CD

```yaml
# azure-pipelines.yml (excerpt)
- task: Maven@4
  inputs:
    mavenPomFile: 'pom.xml'
    goals: 'verify'
    publishJUnitResults: true
    testResultsFiles: '**/surefire-reports/TEST-*.xml'
```

- ArchUnit-Tests laufen mit jedem Build
- Fehlgeschlagene Regeln brechen die Pipeline
- Ergebnisse in den normalen Surefire Test-Reports sichtbar
- Keine zusätzliche Konfiguration nötig

> ArchUnit-Tests sind schnell (< 1 Sekunde) - sie analysieren
> Bytecode, starten keinen Spring-Kontext.

---

## jMolecules: DDD-Annotations + ArchUnit

jMolecules ergänzt ArchUnit um eine ausdrucksstarke DDD-Annotation-Bibliothek.
Klassen kommunizieren ihre Rolle direkt im Code — und ArchUnit prüft die Konsistenz automatisch.

```xml
<dependency>
    <groupId>org.jmolecules</groupId>
    <artifactId>jmolecules-ddd</artifactId>
    <version>1.9.0</version>
</dependency>
<dependency>
    <groupId>org.jmolecules.integrations</groupId>
    <artifactId>jmolecules-archunit</artifactId>
    <version>1.9.0</version>
    <scope>test</scope>
</dependency>
```

---
<style scoped>section { font-size: 1.6em; }</style>

## jMolecules: Annotationen im Domain-Modell

```java
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Entity;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jmolecules.ddd.annotation.Identity;
import org.jmolecules.ddd.annotation.Association;

@AggregateRoot
public class AntragsMappe {
    @Identity
    private final AntragId id;

    @Association
    private BetriebsinhaberRef betriebsinhaber; // Referenz per ID, nicht per Objekt
}

@ValueObject
public record Foerderbetrag(BigDecimal betrag, String waehrung) { }

@Entity
public class Flurstueck {
    @Identity
    private final FlurstueckId id;
}
```

---
<style scoped>section { font-size: 1.6em; }</style>

## jMolecules ArchUnit-Integration

```java
import static org.jmolecules.archunit.JMoleculesDddRules.*;
import static org.jmolecules.archunit.JMoleculesArchitectureRules.*;

@AnalyzeClasses(packages = "de.foerderung")
class JMoleculesArchitectureTest {

    // Prüft alle DDD-Regeln: AggregateRoot, Entity, ValueObject-Constraints
    @ArchTest
    ArchRule ddd_rules = JMoleculesDddRules.all();

    // Prüft Layering gemäß jMolecules-Schichtenmodell
    @ArchTest
    ArchRule architecture_rules =
        JMoleculesArchitectureRules.ensureLayering();
}
```

- `JMoleculesDddRules.all()` prüft u.a.: Aggregate-Roots haben eine `@Identity`, Value Objects sind immutabel, keine direkten Aggregate-Referenzen
- Kombinierbar mit eigenen ArchUnit-Regeln
- Annotationen dienen gleichzeitig als Dokumentation und als prüfbare Constraints

---

## ArchUnit in der Praxis

### Das Problem ohne Architektur-Governance

> *"Es gibt keine Dokumentation der Architektur als Code. Architekturprinzipien —
> Layer-Abhängigkeiten, keine zyklischen Abhängigkeiten, Annotationsvorgaben —
> sollten modelliert und automatisch prüfbar sein."*
> — Internes Architektur-Review

### Konkrete ArchUnit-Regeln aus bestehenden Modulen

```java
// Kein direkt instanziierter ObjectMapper — nur per Dependency Injection
@ArchTest
static final ArchRule kein_neuer_objectmapper =
    noClasses()
        .should().callConstructor(ObjectMapper.class)
        .as("ObjectMapper nicht direkt instanziieren — nur per @Bean / @Inject");

// Services dürfen nur auf ihr eigenes Repository zugreifen
@ArchTest
static final ArchRule service_greift_nur_auf_eigenes_repo_zu =
    noClasses()
        .that().resideInAPackage("..antragstellung.application..")
        .should().dependOnClassesThat()
            .resideInAPackage("..pruefung.infrastructure..")
        .as("Ein Service darf nicht auf das Repository eines fremden Aggregates zugreifen");
```

- `ObjectMapper`-Regel: Verhindert Konfigurationsfehler durch inkonsistente ObjectMapper-Instanzen
- Service/Repository-Isolation: Erzwingt Aggregate-Grenzen — kein Service darf "am eigenen Repo vorbeikoppeln"

---
<style scoped>section { font-size: 1.7em; }</style>

## FreezingArchRule: Best Practice für bestehende Module

> *"Die FreezingArchRule soll an einem Pilotmodul
> evaluiert werden — als Best Practice für alle weiteren Module."*
> *Ziel: Regeln einführen, ohne den Build sofort zu brechen.*

```java
// Schritt 1: Baseline erfassen (erste Ausführung speichert alle Verstöße)
@ArchTest
static final ArchRule baseline_kein_entitymanager_in_services =
    FreezingArchRule.freeze(
        noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat()
                .haveFullyQualifiedName(
                    "jakarta.persistence.EntityManager")
            .as("EntityManager nicht direkt in Application Services verwenden"));
```

```
Erstlauf: 4 Verstöße → gespeichert in archunit_store/
Folgeläufe: Nur NEUE Verstöße brechen den Build
→ Team kann schrittweise sanieren, ohne Stillstand
```

> `archunit_store/` in Git committen: Team sieht historischen Fortschritt der Sanierung.

---

## Hands-on: Lab-08

### ArchUnit-Tests für die Förderantragsverwaltung

---

## Zusammenfassung

- ArchUnit prüft Architekturregeln automatisch bei jedem Build — kein manuelles Review
- `FreezingArchRule` ermöglicht schrittweise Migration ohne sofortigen Build-Break
- jMolecules ergänzt ArchUnit: Annotationen dokumentieren DDD-Rollen und prüfen sie gleichzeitig
- `JMoleculesDddRules.all()` deckt typische Aggregate/Entity/ValueObject-Fehler ab

### Zum Nachlesen

- ArchUnit Docs: [archunit.org](https://www.archunit.org/userguide/html/000_Index.html)
- jMolecules: [github.com/xmolecules/jmolecules](https://github.com/xmolecules/jmolecules)
- Santana, „Domain-Driven Design with Java" (2026), Kap. 4: Testing and Validating DDD Applications

---

## Diskussion

> Welche Architekturregeln brauchen wir für unsere Module?

- Wie verhindern wir, dass neue Module das Webhook-Antimuster wiederholen?
- Welche ArchUnit-Regel würde verhindern, dass Domain-Klassen JPA-Annotationen bekommen?
- Wie unterscheidet sich ArchUnit von Code-Review in der Praxis?
- **FreezingArchRule als Einstieg:** Wie könnte sie in einem bestehenden Modul schrittweise MDB-Abhängigkeiten isolieren helfen?
- Wo ist die Grenze zwischen sinnvoller Governance und Over-Engineering?
- **Konkrete Regeln aus unserem Alltag:**
  - Welche Coding-Konventionen in euren Projekten würden von einer ArchUnit-Regel profitieren?
    *(Beispiel: "Kein direkt instanziierter ObjectMapper" — einfach umsetzbar, sofort wertvoll)*
  - Wie verhindert ihr, dass ein Service direkt auf das Repository eines anderen Aggregates zugreift?
  - Welche ArchUnit-Regel würde die "Services dürfen nur auf ihr eigenes Repo zugreifen"-Anforderung durchsetzen?
