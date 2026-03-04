# Lab 06: Use-Case-Implementierung -- Besichtigung anlegen

## Lernziel

Application Service als Use-Case-Orchestrator implementieren.

## Dauer

45 Minuten

## Voraussetzungen

- Lab 05 abgeschlossen
- Slides Modul 08

## Aufgabe

Implementiere den Use Case "Besichtigung anlegen" als Application Service. Der Use Case orchestriert den Aufruf der Domain-Logik und kuemmert sich um die Persistenz.

### Schritt 1: Command-Objekt erstellen

Erstelle das Command-Objekt `BesichtigungAnlegenCommand` als Java Record im Package `de.immobiliencrm.vermittlung.application.command`:

```java
public record BesichtigungAnlegenCommand(
    UUID vermittlungsvorgangId,
    String interessentName,
    LocalDateTime zeitpunkt
) {}
```

Das Command repraesentiert die Intention des Aufrufers und enthaelt alle Daten, die der Use Case benoetigt.

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
        // 2. Domain-Methode besichtigungHinzufuegen() aufrufen
        // 3. Vermittlungsvorgang speichern
        // 4. Ergebnis zurueckgeben
    }
}
```

**Flow:**

1. Lade den `Vermittlungsvorgang` anhand der ID aus dem Repository
2. Wenn nicht gefunden: wirf eine `VermittlungsvorgangNichtGefundenException`
3. Rufe die Domain-Methode `besichtigungHinzufuegen(interessentName, zeitpunkt)` auf dem Aggregate Root auf
4. Speichere den aktualisierten `Vermittlungsvorgang` ueber das Repository
5. Gib ein `BesichtigungAnlegenResult` mit der neuen Besichtigungs-ID zurueck

**Wichtig:** Der Use Case verwendet `@Transactional`, um die Konsistenz sicherzustellen. Die Geschaeftslogik bleibt im Domain-Modell -- der Use Case orchestriert nur.

### Schritt 4: Exception fuer nicht-gefundenen Vermittlungsvorgang

Erstelle die Exception `VermittlungsvorgangNichtGefundenException` im Package `de.immobiliencrm.vermittlung.domain.model`:

```java
public class VermittlungsvorgangNichtGefundenException extends RuntimeException {
    public VermittlungsvorgangNichtGefundenException(UUID id) {
        super("Vermittlungsvorgang mit ID " + id + " nicht gefunden");
    }
}
```

**Hinweis:** Die Exception liegt im Domain-Package, da sie ein fachliches Konzept repraesentiert ("es gibt keinen Vermittlungsvorgang mit dieser ID").

### Bonus: Zweiter Use Case

Implementiere einen zweiten Use Case `BesichtigungDurchfuehrenUseCase`:

- Command: `BesichtigungDurchfuehrenCommand(UUID vermittlungsvorgangId, UUID besichtigungId)`
- Laedt den Vermittlungsvorgang, ruft `besichtigungDurchfuehren(besichtigungId)` auf und speichert

## Verifikation

Schreibe einen Unit-Test des Use Case mit gemocktem Repository:

1. **Happy Path:** Vermittlungsvorgang existiert, Besichtigung wird angelegt, Ergebnis wird zurueckgegeben
2. **Not Found:** Vermittlungsvorgang existiert nicht, `VermittlungsvorgangNichtGefundenException` wird geworfen

```bash
cd solution
mvn test
```

Alle Tests muessen gruen sein.

## Tipps

- Der Use Case ist bewusst duenn gehalten -- die Geschaeftslogik steckt im Domain-Modell.
- Commands und Results sind immutable (Records) und gehoeren zur Application-Schicht.
- `@Transactional` sorgt dafuer, dass bei einer Exception ein Rollback stattfindet.
- Das Repository-Interface stammt aus der Domain-Schicht -- der Use Case haengt nur von der Abstraktion ab, nicht von der konkreten Implementierung.
