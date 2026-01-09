# Lab 10: Spring Modulith - Modularen Monolithen strukturieren

Euer Immobilien-CRM hat bereits zwei Bounded Contexts (Acquisition und Brokerage),
die über `ContractSigned`-Events kommunizieren. Bisher nutzt ihr
Springs `ApplicationEventPublisher` mit `@EventListener` direkt.

In diesem Lab strukturiert ihr den Monolithen mit Spring Modulith, damit:

- Modul-Grenzen automatisch verifiziert werden
- Modul-Interaktionen mit der Scenario-API getestet werden
- Die Modulstruktur als Dokumentation generiert wird
- Die Event Publication Registry für At-Least-Once Delivery vorbereitet ist

---

## Aufgabe

### Schritt 1: Spring-Modulith-Dependencies hinzufügen

Erweitere die `pom.xml` um Spring Modulith:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.modulith</groupId>
            <artifactId>spring-modulith-bom</artifactId>
            <version>2.0.3</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <!-- Existing dependencies ... -->

    <!-- Spring Modulith -->
    <dependency>
        <groupId>org.springframework.modulith</groupId>
        <artifactId>spring-modulith-starter-core</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.modulith</groupId>
        <artifactId>spring-modulith-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <!-- Event Publication Registry (At-Least-Once Delivery) -->
    <dependency>
        <groupId>org.springframework.modulith</groupId>
        <artifactId>spring-modulith-events-jdbc</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.modulith</groupId>
        <artifactId>spring-modulith-events-jackson</artifactId>
    </dependency>
</dependencies>
```

### Schritt 2: Modul-Verifikationstest schreiben und scheitern lassen

Erstelle die Testklasse `ModulithStructureTest` im Package `de.realestate` unter `src/test/java`:

```java
class ModulithStructureTest {

    ApplicationModules modules =
        ApplicationModules.of(RealEstateCrmApplication.class);

    @Test
    void verifyModuleStructure() {
        modules.verify();
    }

    @Test
    void documentModuleStructure() {
        new Documenter(modules)
            .writeDocumentation();
    }
}
```

Führe den Test aus. Er wird fehlschlagen, weil der `ContractSignedListener` im
Brokerage-Modul auf das *interne* Package `de.realestate.acquisition.domain.event`
zugreift. Spring Modulith betrachtet alle Subpackages als modulintern.

### Schritt 3: Event in die öffentliche Modul-API verschieben

Verschiebe `ContractSigned` von `de.realestate.acquisition.domain.event`
nach `de.realestate.acquisition` (dem Modul-Root-Package):

```java
package de.realestate.acquisition;

public record ContractSigned(
    UUID contractId,
    UUID propertyId,
    LocalDateTime closedAt
) {}
```

Passe alle Imports an:
- `CloseContractUseCase`
- `ContractSignedListener`

Lösche die alte Datei `de.realestate.acquisition.domain.event.ContractSigned`.

Führe den Verifikationstest erneut aus - er sollte jetzt grün sein.

### Schritt 4: @ApplicationModule mit erlaubten Abhängigkeiten

Erstelle `package-info.java` für jedes Modul:

`de/realestate/acquisition/package-info.java`:

```java
@ApplicationModule
package de.realestate.acquisition;

import org.springframework.modulith.ApplicationModule;
```

`de/realestate/brokerage/package-info.java`:

```java
@ApplicationModule(allowedDependencies = {"acquisition"})
package de.realestate.brokerage;

import org.springframework.modulith.ApplicationModule;
```

`de/realestate/property/package-info.java`:

```java
@ApplicationModule
package de.realestate.property;

import org.springframework.modulith.ApplicationModule;
```

### Schritt 5: Event Publication Registry konfigurieren

Erweitere die `application.yml`, damit die Event Publication Registry
ihre Tabelle automatisch anlegt:

```yaml
spring:
  modulith:
    events:
      jdbc:
        schema-initialization:
          enabled: true
```

Damit werden Events in einer DB-Tabelle persistiert. In Kombination mit
`@TransactionalEventListener` ermöglicht dies At-Least-Once Delivery -
fehlgeschlagene Event-Verarbeitungen werden beim Neustart automatisch wiederholt.

### Schritt 6: @ApplicationModuleTest mit Scenario-API

Erstelle einen Modulith-Integrationstest für das Brokerage-Modul:

```java
@ApplicationModuleTest
class BrokerageModuleTest {

    @Autowired
    private BrokerageProcessRepository repository;

    @Test
    void shouldCreateBrokerageProcessOnContractSigned(Scenario scenario) {
        UUID contractId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();

        scenario.publish(new ContractSigned(
                    contractId, propertyId, LocalDateTime.now()))
                .andWaitForStateChange(
                    () -> repository.findAll().stream()
                        .filter(p -> p.getPropertyId().equals(propertyId))
                        .findFirst()
                        .orElse(null),
                    Objects::nonNull)
                .andVerify(process ->
                    assertThat(process.getPropertyId()).isEqualTo(propertyId));
    }
}
```

Dieser Test:
- Startet nur das Brokerage-Modul (nicht die ganze Anwendung)
- Publiziert ein Event und wartet auf den erwarteten Zustandswechsel
- Kein `Thread.sleep()` nötig

### Bonus: Dokumentation generieren

Führe den `documentModuleStructure()`-Test aus und prüfe die generierten
PlantUML-Diagramme in `target/spring-modulith-docs/`.

### Bonus: @TransactionalEventListener

Ersetze `@EventListener` im `ContractSignedListener` durch
`@TransactionalEventListener(phase = AFTER_COMMIT)`, damit das Event erst nach
erfolgreichem Commit verarbeitet wird. Die Event Publication Registry stellt
dann sicher, dass fehlgeschlagene Verarbeitungen beim Neustart wiederholt werden.

Hinweis: Beim Umstieg auf `@TransactionalEventListener` muss der
`ContextIntegrationTest` als Verifikation dienen (`@SpringBootTest` mit
vollem Transaktionskontext), da `Scenario.publish()` kein Transaktions-Commit
simuliert.

## Tipps

- Spring Modulith erkennt Top-Level-Packages unter der `@SpringBootApplication`-Klasse
  automatisch als Module. Subpackages (wie `domain/`, `application/`, `adapter/`) gelten
  als modulintern.
- Events gehören zur öffentlichen API eines Moduls und müssen daher im
  Root-Package des Moduls liegen.
- `@ApplicationModule(allowedDependencies = {...})` deklariert explizit, welche
  Module referenziert werden dürfen. Ohne diese Angabe sind alle erlaubt.
- ArchUnit und Spring Modulith ergänzen sich: ArchUnit prüft feine Schicht-Regeln
  *innerhalb* eines Moduls, Spring Modulith prüft die Grenzen *zwischen* Modulen.
- Die Event Publication Registry nutzt die bestehende Datenquelle (H2) und erstellt
  automatisch die benötigte Tabelle.
