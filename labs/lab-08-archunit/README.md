# Lab 08: ArchUnit -- Architekturregeln als Tests

## Lernziel

Architekturregeln mit ArchUnit automatisiert pruefen.

## Dauer

45 Minuten

## Voraussetzungen

- Lab 07 abgeschlossen
- Slides Modul 10

## Aufgabe

Ergaenze das Projekt um ArchUnit-Tests, die sicherstellen, dass die Clean-Architecture-Regeln eingehalten werden.

### Schritt 1: ArchUnit Dependency hinzufuegen

Fuege die ArchUnit-Dependency zur `pom.xml` hinzu:

```xml
<dependency>
    <groupId>com.tngtech.archunit</groupId>
    <artifactId>archunit-junit5</artifactId>
    <version>1.3.0</version>
    <scope>test</scope>
</dependency>
```

### Schritt 2: Testklasse erstellen

Erstelle die Testklasse `ArchitectureTest` im Package `de.immobiliencrm.architecture` unter `src/test/java`:

```java
@AnalyzeClasses(packages = "de.immobiliencrm")
class ArchitectureTest {
    // Regeln hier definieren
}
```

### Schritt 3: Regel 1 -- Domain darf nicht auf Infrastructure oder Adapter zugreifen

Die Domain-Schicht darf keine Abhaengigkeiten auf die Infrastructure- oder Adapter-Schicht haben:

```java
@ArchTest
static final ArchRule domain_should_not_depend_on_infrastructure_or_adapter =
    noClasses()
        .that().resideInAPackage("..domain..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("..infrastructure..", "..adapter..");
```

### Schritt 4: Regel 2 -- Keine Spring-Framework-Klassen in Domain

Die Domain-Schicht darf keine Spring-Framework-Klassen verwenden:

```java
@ArchTest
static final ArchRule domain_should_not_use_spring =
    noClasses()
        .that().resideInAPackage("..domain..")
        .should().dependOnClassesThat()
        .resideInAPackage("org.springframework..");
```

### Schritt 5: Regel 3 -- Adapter.Web darf nicht direkt auf Domain.Model zugreifen

Der Web-Adapter darf nicht direkt auf `domain.model` zugreifen, sondern nur ueber die Application-Schicht:

```java
@ArchTest
static final ArchRule web_adapter_should_not_access_domain_model_directly =
    noClasses()
        .that().resideInAPackage("..adapter.web..")
        .should().dependOnClassesThat()
        .resideInAPackage("..domain.model..");
```

### Schritt 6: Regel 4 -- Nur Adapter.Web darf @RestController verwenden

Nur Klassen im Package `adapter.web` duerfen die Annotation `@RestController` verwenden:

```java
@ArchTest
static final ArchRule only_web_adapter_should_use_rest_controller =
    classes()
        .that().areAnnotatedWith(RestController.class)
        .should().resideInAPackage("..adapter.web..");
```

### Bonus: Regel fuer @Transactional

Stelle sicher, dass `@Transactional` nur in `application.service` verwendet wird:

```java
@ArchTest
static final ArchRule transactional_only_in_application_service =
    noClasses()
        .that().resideOutsideOfPackage("..application.service..")
        .should().beAnnotatedWith(Transactional.class);
```

## Verifikation

Fuehre die Tests aus:

```bash
cd solution
mvn test
```

Alle ArchUnit-Tests muessen gruen sein.

## Tipps

- ArchUnit analysiert den kompilierten Bytecode -- deshalb muss das Projekt vorher kompiliert werden.
- Verwende `@AnalyzeClasses(packages = "de.immobiliencrm")`, um alle Klassen im Projekt zu analysieren.
- ArchUnit-Regeln koennen auch mit `@ArchTest` als Felder definiert werden -- das ist uebersichtlicher als einzelne Testmethoden.
- Falls eine Regel fehlschlaegt, zeigt ArchUnit genau an, welche Klasse gegen welche Regel verstoesst.
