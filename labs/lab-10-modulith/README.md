# Lab 10: Spring Modulith - Modularen Monolithen strukturieren

Die Förderantragsverwaltung hat bereits zwei Bounded Contexts (Antragstellung
und Fachliche Prüfung), die über `AntragsmappeEingereicht`-Events kommunizieren.
Bisher nutzt ihr Springs `ApplicationEventPublisher` mit
`@TransactionalEventListener` direkt.

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

Erstelle die Testklasse `ModulithStructureTest` im Package `de.foerderung`
unter `src/test/java`:

```java
class ModulithStructureTest {

    ApplicationModules modules =
        ApplicationModules.of(FoerderantragApplication.class);

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

Führe den Test aus. Er wird fehlschlagen, weil der
`AntragstellungEventListener` im Prüfungs-Modul auf das *interne* Package
`de.foerderung.antragstellung.internal.domain` zugreift. Spring Modulith
betrachtet alle Subpackages als modulintern.

### Schritt 3: Event in die öffentliche Modul-API verschieben

Verschiebe `AntragsmappeEingereicht` von
`de.foerderung.antragstellung.internal.domain.event`
nach `de.foerderung.antragstellung` (dem Modul-Root-Package):

```java
package de.foerderung.antragstellung;

public record AntragsmappeEingereicht(
    UUID antragsmappeId,
    String registrierungsNummer,
    Instant eingereichtAm
) {}
```

Passe alle Imports an:
- `AntragEinreichenService`
- `AntragstellungEventListener`

Führe den Verifikationstest erneut aus - er sollte jetzt grün sein.

### Schritt 4: @ApplicationModule mit erlaubten Abhängigkeiten

Erstelle `package-info.java` für jedes Modul:

`de/foerderung/antragstellung/package-info.java`:

```java
@ApplicationModule
package de.foerderung.antragstellung;

import org.springframework.modulith.ApplicationModule;
```

`de/foerderung/pruefung/package-info.java`:

```java
@ApplicationModule(allowedDependencies = {"antragstellung"})
package de.foerderung.pruefung;

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

Erstelle einen Modulith-Integrationstest für das Prüfungs-Modul:

```java
@ApplicationModuleTest
class PruefungModuleTest {

    @Autowired
    private PruefvorgangRepository repository;

    @Test
    void shouldCreatePruefvorgangOnAntragEingereicht(Scenario scenario) {
        UUID mappeId = UUID.randomUUID();
        String regNr = "DE-ELER-2026-001";

        scenario.publish(new AntragsmappeEingereicht(
                    mappeId, regNr, Instant.now()))
                .andWaitForStateChange(
                    () -> repository.findByAntragsReferenz(
                        new AntragsReferenz(mappeId))
                        .orElse(null),
                    Objects::nonNull)
                .andVerify(pruefvorgang ->
                    assertThat(pruefvorgang.getRegistrierungsNummer().value())
                        .isEqualTo(regNr));
    }
}
```

Dieser Test:
- Startet nur das Prüfungs-Modul (nicht die ganze Anwendung)
- Publiziert ein Event und wartet auf den erwarteten Zustandswechsel
- Kein `Thread.sleep()` nötig

### Bonus: Dokumentation generieren

Führe den `documentModuleStructure()`-Test aus und prüfe die generierten
PlantUML-Diagramme in `target/spring-modulith-docs/`.

### Bonus: @ApplicationModuleListener

Ersetze `@TransactionalEventListener(phase = AFTER_COMMIT)` im
`AntragstellungEventListener` durch `@ApplicationModuleListener` (Spring
Modulith 1.2+). Das ist der empfohlene Standard für Inter-Modul-Events.
Die Event Publication Registry stellt dann sicher, dass fehlgeschlagene
Verarbeitungen beim Neustart wiederholt werden.

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

---

## Modulstruktur der Förderantragsverwaltung

### Wie die Architektur in Modulith-Konventionen aussieht

```
de.foerderung/
├── antragstellung/
│   ├── AntragsmappeEingereicht.java     ← öffentliche API (Event Record)
│   ├── AntragsmappeGeaendert.java       ← öffentliche API (Event Record)
│   └── internal/
│       ├── domain/
│       │   ├── AntragsMappe.java        ← Aggregate Root
│       │   ├── Flurstück.java           ← Entity
│       │   └── AenderungsArt.java       ← Value Object
│       ├── application/
│       │   └── AntragEinreichenService.java
│       └── adapter/
│           ├── web/AntragsMappeController.java
│           └── persistence/JpaAntragsMappeRepository.java
├── pruefung/
│   ├── PruefungAbgeschlossen.java       ← öffentliche API
│   └── internal/
│       ├── domain/
│       │   └── Pruefvorgang.java
│       ├── application/
│       │   └── PruefungStartenService.java
│       └── adapter/
│           └── acl/
│               ├── AntragstellungEventTranslator.java  ← ACL Translator
│               └── AntragstellungEventListener.java    ← ACL Listener
├── auszahlung/
│   └── internal/
│       └── adapter/
│           └── acl/
│               └── PruefungAbgeschlossenListener.java
└── auswertung/
    └── internal/
        └── adapter/
            └── acl/
                └── AntragsmappeEventListener.java
```

### Was Spring Modulith prüft — und was verletzt wäre

```java
@Test
void modulithStructureIsValid() {
    ApplicationModules.of(FoerderantragApplication.class).verify();
}
// Dieser Test würde bei direktem Import von
// 'antragstellung.internal.domain.AntragsMappe' aus dem 'auswertung'-Modul
// FEHLSCHLAGEN — genau das, was bei direktem ObjectMessage-Cast passiert.
```
