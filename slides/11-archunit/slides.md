---
marp: true
theme: default
paginate: true
header: "DDD & Clean Architecture mit Spring Boot 4"
footer: "CC BY-NC-SA 4.0, Alexander Erben"
---

# Modul 11 – ArchUnit

## Architekturregeln als ausführbare Tests

**Geschätzte Dauer:** ca. 50 Minuten

### Lernziele

- ArchUnit als Werkzeug für automatisierte Architektur-Governance kennen
- Architekturregeln als JUnit-Tests schreiben
- Clean-Architecture-Regeln mit ArchUnit durchsetzen
- Namenskonventionen und Annotation-Regeln formulieren
- Onion Architecture als vordefinierte Form nutzen
- Architektur-Baseline für bestehenden Code einsetzen

---

## Warum automatisierte Architektur-Governance?

### Das Problem

```
Woche 1:  Architektur sauber, alle halten sich dran       ✅
Woche 4:  "Nur diese eine Abkürzung..."                   ⚠️
Woche 12: Domain importiert JPA, Controller hat Logik      ❌
Woche 24: "Wir müssen die Architektur neu aufsetzen"       💀
```

### Die Lösung

- Architekturregeln als **ausführbare Tests** formulieren
- Laufen bei **jedem Build** — Verstöße brechen die Pipeline
- Keine zusätzliche Infrastruktur nötig — normaler JUnit-Test

> Architekturregeln, die nicht automatisch geprüft werden,
> werden **früher oder später** gebrochen.

---

## Was ist ArchUnit?

- **Java-Bibliothek** von TNG Technology Consulting
- Architekturregeln als **JUnit 5 Tests** formuliert
- Prüft: Paketabhängigkeiten, Annotationen, Vererbung, Namenskonventionen
- Läuft als **normaler Unit-Test** — `mvn test` genügt
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

## Grundlegende API

```java
@AnalyzeClasses(packages = "de.realestate")
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

- `@AnalyzeClasses` — welche Packages werden analysiert
- `@ArchTest` statt `@Test` — ArchUnit erkennt die Regel automatisch
- Fluent API: `classes().that()...should()...`

---

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

- Sichert die **Dependency Rule** der Clean Architecture ab
- Domain ist der innerste Ring — kennt nur `java.*` und sich selbst
- Verstöße brechen den Build sofort

---

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

- ArchUnit bietet **vordefinierte Architekturformen** an
- `onionArchitecture()` prüft automatisch alle Abhängigkeitsregeln
- Weniger Code als einzelne Regeln — aber weniger granulare Fehlermeldungen
- Alternative: `layeredArchitecture()` für klassische Schichtarchitekturen

---

## Layered Architecture – Alternative

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

## Cross-BC-Regeln: Bounded Context Isolation

```java
@ArchTest
static final ArchRule brokerage_does_not_access_acquisition_domain =
    noClasses()
        .that().resideInAPackage("..brokerage.domain..")
        .should().dependOnClassesThat()
            .resideInAPackage("..acquisition.domain..")
        .as("Brokerage domain must not directly access "
            + "Acquisition domain");

@ArchTest
static final ArchRule bcs_communicate_only_via_events =
    slices().matching("de.realestate.(*).domain..")
        .should().notDependOnEachOther()
        .as("Domain layers of different BCs "
            + "must not depend on each other");
```

- Stellt sicher, dass Bounded Contexts **isoliert** bleiben
- Kommunikation zwischen BCs nur über Events oder definierte APIs
- `slices()` prüft **alle** BC-Kombinationen auf einmal

---

## Architektur-Baseline: Legacy-Code schrittweise verbessern

### Problem: 47 bestehende Verstöße — Build bricht sofort

```java
// FreezingArchRule: "freeze" existing violations
@ArchTest
static final ArchRule domain_framework_free =
    FreezingArchRule.freeze(
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
                .resideInAPackage("org.springframework.."));
```

- Erster Lauf: alle Verstöße werden in `archunit_store/` gespeichert
- Folgende Läufe: nur **neue** Verstöße brechen den Build
- Bestehende Verstöße werden schrittweise abgebaut
- Datei `archunit_store/` in `.gitignore` aufnehmen oder committen (Team-Entscheidung)

> **Ideal für Legacy-Projekte:** Regeln sofort einführen,
> ohne alle bestehenden Verstöße auf einmal fixen zu müssen.

---

## Alle Regeln zusammen

```java
@AnalyzeClasses(packages = "de.realestate")
class CleanArchitectureTest {

    // Dependency rules
    @ArchTest static final ArchRule r1 = /* domain_is_framework_free */;
    @ArchTest static final ArchRule r2 = /* domain_does_not_know_outer_rings */;
    @ArchTest static final ArchRule r3 = /* application_does_not_know_infrastructure */;

    // Annotation rules
    @ArchTest static final ArchRule r4 = /* rest_controller_only_in_adapter_web */;
    @ArchTest static final ArchRule r5 = /* entity_only_in_infrastructure */;
    @ArchTest static final ArchRule r6 = /* transactional_only_in_application */;

    // Naming conventions
    @ArchTest static final ArchRule r7 = /* controllers_named_controller */;

    // Predefined architecture form
    @ArchTest static final ArchRule r8 = /* onion_architecture */;

    // Cross-BC isolation
    @ArchTest static final ArchRule r9 = /* bcs_communicate_only_via_events */;
}
```

---

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

- ArchUnit-Tests laufen mit **jedem Build**
- Fehlgeschlagene Regeln brechen die Pipeline
- Ergebnisse in den normalen **Surefire Test-Reports** sichtbar
- Keine zusätzliche Konfiguration nötig

> ArchUnit-Tests sind **schnell** (< 1 Sekunde) — sie analysieren
> Bytecode, starten keinen Spring-Kontext.

---

## Best Practices

| Praxis | Empfehlung |
|--------|-----------|
| **Zeitpunkt** | Regeln **früh** im Projekt einführen, nicht nachträglich |
| **Beschreibungen** | Aussagekräftige `.as()`-Texte verwenden |
| **Legacy** | `FreezingArchRule` nutzen, Verstöße schrittweise abbauen |
| **Team** | Regeln gemeinsam definieren, im Team-Review diskutieren |
| **Granularität** | Lieber **viele kleine** Regeln als eine große |
| **Konventionen** | Namensregeln für DTOs, Controller, Services, Events |
| **ArchUnit vs. Modulith** | ArchUnit für feine Regeln, Spring Modulith für Modul-Grenzen |

> **Unser Ansatz im Workshop:** ArchUnit für Schicht-Regeln,
> Spring Modulith (Modul 12) für BC-Grenzen und Event-Kommunikation.

---

## 🎯 Hands-on: Lab-09

### Aufgabe

Schreibt ArchUnit-Tests für das Immobilien-CRM:

1. Maven Dependency hinzufügen
2. Regel: Domain darf keine Spring/JPA-Abhängigkeiten haben
3. Regel: Domain kennt keine äußeren Ringe
4. Regel: `@RestController` nur in `adapter.web`
5. Regel: `@Entity` nur in `infrastructure`
6. Onion Architecture als vordefinierte Form testen
7. Tests ausführen und Ergebnisse prüfen

> **Dauer:** ca. 25 Minuten
> Details und Aufgabenstellung im **Lab-09**

---

## 💬 Diskussion

> Welche Architekturregeln würdet ihr in eurem Projekt einführen?

- Habt ihr heute schon Architekturregeln — und werden sie eingehalten?
- Wo würdet ihr `FreezingArchRule` einsetzen?
- Welche **Namenskonventionen** gelten in euren Projekten?
- Wie unterscheidet sich ArchUnit von Code-Review in der Praxis?
- Wo ist die Grenze zwischen sinnvoller Governance und **Over-Engineering**?
