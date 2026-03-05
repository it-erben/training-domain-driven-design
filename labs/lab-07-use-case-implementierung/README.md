# Lab 07: Use-Case-Implementierung - Besichtigung anlegen

## Lernziel

Einen Application Service als Use-Case-Orchestrator implementieren.

## Dauer

45 Minuten

## Voraussetzungen

- Lab 06 abgeschlossen
- Slides Modul 09

## Aufgabe

Implementiere den Use Case „Besichtigung anlegen" als Application Service. Der Use Case orchestriert den Aufruf der Domain-Logik und kümmert sich um die Persistenz.

### Schritt 1: Command-Objekt erstellen

Erstelle das Command-Objekt `CreateViewingCommand` als Java Record im Package `de.realestate.brokerage.application.command`:

```java
public record CreateViewingCommand(
    UUID processId,
    String prospectName,
    LocalDateTime appointmentDate
) {}
```

Das Command repräsentiert die Absicht des Aufrufers und enthält alle Daten, die der Use Case benötigt.

### Schritt 2: Ergebnisobjekt erstellen

Erstelle das Ergebnisobjekt `CreateViewingResult` als Java Record im selben Package:

```java
public record CreateViewingResult(
    UUID viewingId,
    UUID processId
) {}
```

### Schritt 3: Use Case implementieren

Erstelle den Application Service `CreateViewingUseCase` als `@Service` im Package `de.realestate.brokerage.application.service`:

```java
@Service
public class CreateViewingUseCase {

    private final BrokerageProcessRepository repository;

    // Constructor Injection

    @Transactional
    public CreateViewingResult create(CreateViewingCommand command) {
        // 1. Load the BrokerageProcess from the repository
        // 2. Call the domain method addViewing()
        // 3. Save the BrokerageProcess
        // 4. Return the result
    }
}
```

**Ablauf:**

1. Lade den `BrokerageProcess` anhand der ID aus dem Repository
2. Falls nicht gefunden: wirf eine `ProcessNotFoundException`
3. Rufe die Domain-Methode `addViewing(prospectName, appointmentDate)` auf dem Aggregate Root auf
4. Speichere den aktualisierten `BrokerageProcess` über das Repository
5. Gib ein `CreateViewingResult` mit der neuen Viewing-ID zurück

**Wichtig:** Der Use Case verwendet `@Transactional` zur Sicherstellung der Konsistenz. Die Geschäftslogik bleibt im Domain-Modell – der Use Case orchestriert nur.

### Schritt 4: Exception für nicht gefundenen Prozess

Erstelle die Exception `ProcessNotFoundException` im Package `de.realestate.brokerage.domain.model`:

```java
public class ProcessNotFoundException extends RuntimeException {
    public ProcessNotFoundException(UUID id) {
        super("BrokerageProcess with ID " + id + " not found");
    }
}
```

**Hinweis:** Die Exception liegt im Domain-Package, da sie ein Domänenkonzept darstellt („Es gibt keinen Vermittlungsprozess mit dieser ID").

### Bonus: Zweiter Use Case

Implementiere einen zweiten Use Case `CompleteViewingUseCase`:

- Command: `CompleteViewingCommand(UUID processId, UUID viewingId)`
- Lädt den BrokerageProcess, ruft `completeViewing(viewingId)` auf und speichert

## Verifikation

Schreibe einen Unit-Test für den Use Case mit einem gemockten Repository:

1. **Happy Path:** BrokerageProcess existiert, Viewing wird erstellt, Ergebnis wird zurückgegeben
2. **Nicht gefunden:** BrokerageProcess existiert nicht, `ProcessNotFoundException` wird geworfen

```bash
cd solution
mvn test
```

Alle Tests müssen grün sein.

## Tipps

- Der Use Case ist bewusst schlank gehalten – die Geschäftslogik liegt im Domain-Modell.
- Commands und Results sind unveränderlich (Records) und gehören zur Application-Schicht.
- `@Transactional` stellt sicher, dass bei einer Exception ein Rollback erfolgt.
- Das Repository-Interface kommt aus der Domain-Schicht – der Use Case hängt nur von der Abstraktion ab, nicht von der konkreten Implementierung.
