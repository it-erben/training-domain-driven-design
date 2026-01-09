# Lab 08: ArchUnit - Architekturregeln als Tests

Füge dem Brokerage-Bounded-Context ArchUnit-Tests hinzu, die sicherstellen,
dass die Clean-Architecture-Regeln eingehalten werden.

Hinweis: Das Lab baut auf dem in den vorherigen Labs eingeführten Package
`de.realestate.brokerage` auf. Ältere CRUD-Beispiele außerhalb dieses Contexts
sind hier nicht Gegenstand der Architekturregeln.

## Schritt 1: ArchUnit-Dependency hinzufügen

Füge die ArchUnit-Dependency zur `pom.xml` hinzu:

```xml
<dependency>
    <groupId>com.tngtech.archunit</groupId>
    <artifactId>archunit-junit5</artifactId>
    <version>1.3.0</version>
    <scope>test</scope>
</dependency>
```

## Schritt 2: Testklasse erstellen

Erstelle die Testklasse `ArchitectureTest` im Package `de.realestate.architecture` unter `src/test/java`:

```java
@AnalyzeClasses(packages = "de.realestate.brokerage")
class ArchitectureTest {
    // Define rules here
}
```

## Schritt 3: Regel 1 - Domain darf nicht von Infrastructure oder Adapter abhängen

Die Domain-Schicht darf keine Abhängigkeiten zur Infrastruktur- oder Adapter-Schicht haben:

```java
@ArchTest
static final ArchRule domain_should_not_depend_on_infrastructure_or_adapter =
    noClasses()
        .that().resideInAPackage("..domain..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("..infrastructure..", "..adapter..");
```

## Schritt 4: Regel 2 - Keine Spring-Framework-Klassen in der Domain

Die Domain-Schicht darf keine Spring-Framework-Klassen verwenden:

```java
@ArchTest
static final ArchRule domain_has_no_spring_imports =
    noClasses()
        .that().resideInAPackage("..domain..")
        .should().dependOnClassesThat()
        .resideInAPackage("org.springframework..");
```

## Schritt 5: Regel 3 - Adapter.Web darf nicht direkt auf Domain.Model zugreifen

REST-Controller im Web-Adapter dürfen nicht direkt auf `domain.model`
zugreifen, sondern nur über die Application-Schicht:

```java
@ArchTest
static final ArchRule rest_controller_should_not_access_domain_model_directly =
    noClasses()
        .that().resideInAPackage("..adapter.web..")
        .and().areAnnotatedWith(RestController.class)
        .should().dependOnClassesThat()
        .resideInAPackage("..domain.model..");
```

## Schritt 6: Regel 4 - Nur Adapter.Web darf @RestController verwenden

Nur Klassen im Package `adapter.web` dürfen die Annotation `@RestController` verwenden:

```java
@ArchTest
static final ArchRule only_web_adapter_should_use_rest_controller =
    classes()
        .that().areAnnotatedWith(RestController.class)
        .should().resideInAPackage("..adapter.web..");
```

## Bonus: Regel für @Transactional

Stelle sicher, dass `@Transactional` nur in `application.service` verwendet wird:

```java
@ArchTest
static final ArchRule transactional_only_in_application_service =
    noClasses()
        .that().resideOutsideOfPackage("..application.service..")
        .should().beAnnotatedWith(Transactional.class);
```

Alle ArchUnit-Tests müssen am Ende grün sein.
