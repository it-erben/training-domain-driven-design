# Lab 06: Use-Case-Implementierung - Besichtigung anlegen

## Lernziel

Application Service als Use-Case-Orchestrator implementieren.

## Dauer

45 Minuten

## Voraussetzungen

- Lab 05 abgeschlossen
- Slides Modul 08

## Aufgabe

Implementiere den Use Case "Besichtigung anlegen" als Application Service. Der Use Case orchestriert den Aufruf der Domain-Logik und kümmert sich um die Persistenz.

### Schritt 1: Command-Objekt erstellen

Erstelle das Command-Objekt `BesichtigungAnlegenCommand` als Java Record im Package `de.immobiliencrm.vermittlung.application.command`:

```java
public record BesichtigungAnlegenCommand(
    UUID vermittlungsvorgangId,
    String interessentName,
    LocalDateTime zeitpunkt
) {}
```

Das Command repräsentiert die Intention des Aufrufers und enthält alle Daten, die der Use Case benötigt.

### Schritt 2: Ergebnis-Objekt erstellen

Erstelle das Ergebnis-Objekt `BesichtigungAnlegenResult` als Java Record im selben Package:

```java
public record BesichtigungAnlegenResult(
    UUID besichtigungId,
    UUID vermittlungsvorgangId
) {}
```

### Schritt 3: Use Case implementieren

Erstelle den Application Service `BesichtigungAnlegenUseCase` als `@Service` im Package `de.immobiliencrm.vermittlung.application.service`:

```java
@Service
public class BesichtigungAnlegenUseCase {

    private final VermittlungsvorgangRepository repository;

    // Constructor Injection

    @Transactional
    public BesichtigungAnlegenResult anlegen(BesichtigungAnlegenCommand command) {
        // 1. Vermittlungsvorgang aus dem Repository laden
        // 2. Domain-Methode besichtigungHinzufügen() aufrufen
        // 3. Vermittlungsvorgang speichern
        // 4. Ergebnis zurückgeben
    }
}
```

**Flow:**

1. Lade den `Vermittlungsvorgang` anhand der ID aus dem Repository
2. Wenn nicht gefunden: wirf eine `VermittlungsvorgangNichtGefundenException`
3. Rufe die Domain-Methode `besichtigungHinzufügen(interessentName, zeitpunkt)` auf dem Aggregate Root auf
4. Speichere den aktualisierten `Vermittlungsvorgang` über das Repository
5. Gib ein `BesichtigungAnlegenResult` mit der neuen Besichtigungs-ID zurück

**Wichtig:** Der Use Case verwendet `@Transactional`, um die Konsistenz sicherzustellen. Die Geschäftslogik bleibt im Domain-Modell - der Use Case orchestriert nur.

### Schritt 4: Exception für nicht-gefundenen Vermittlungsvorgang

Erstelle die Exception `VermittlungsvorgangNichtGefundenException` im Package `de.immobiliencrm.vermittlung.domain.model`:

```java
public class VermittlungsvorgangNichtGefundenException extends RuntimeException {
    public VermittlungsvorgangNichtGefundenException(UUID id) {
        super("Vermittlungsvorgang mit ID " + id + " nicht gefunden");
    }
}
```

**Hinweis:** Die Exception liegt im Domain-Package, da sie ein fachliches Konzept repräsentiert ("es gibt keinen Vermittlungsvorgang mit dieser ID").

### Bonus: Zweiter Use Case

Implementiere einen zweiten Use Case `BesichtigungDurchführenUseCase`:

- Command: `BesichtigungDurchführenCommand(UUID vermittlungsvorgangId, UUID besichtigungId)`
- Lädt den Vermittlungsvorgang, ruft `besichtigungDurchführen(besichtigungId)` auf und speichert

## Verifikation

Schreibe einen Unit-Test des Use Case mit gemocktem Repository:

1. **Happy Path:** Vermittlungsvorgang existiert, Besichtigung wird angelegt, Ergebnis wird zurückgegeben
2. **Not Found:** Vermittlungsvorgang existiert nicht, `VermittlungsvorgangNichtGefundenException` wird geworfen

```bash
cd solution
mvn test
```

Alle Tests müssen grün sein.

## Tipps

- Der Use Case ist bewusst dünn gehalten - die Geschäftslogik steckt im Domain-Modell.
- Commands und Results sind immutable (Records) und gehören zur Application-Schicht.
- `@Transactional` sorgt dafür, dass bei einer Exception ein Rollback stattfindet.
- Das Repository-Interface stammt aus der Domain-Schicht - der Use Case hängt nur von der Abstraktion ab, nicht von der konkreten Implementierung.
