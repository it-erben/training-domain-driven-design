# Lab 04: Taktisches DDD - Building Blocks implementieren

## Lernziel

Aggregate Root, Entities, Value Objects und Domain Events in Java implementieren.

## Dauer

90 Minuten

## Voraussetzungen

- Lab 01 abgeschlossen
- Slides Modul 05

## Aufgabe

Modelliere und implementiere den Bounded Context "Vermittlungsprozess".

### Schritt 1: Value Objects als Java Records erstellen

Erstelle die folgenden Value Objects als Java Records im Package `de.immobiliencrm.vermittlung.domain.model`:

**Adresse**

```java
public record Adresse(String strasse, String plz, String ort) {
    // Compact Constructor mit Validierung:
    // - Alle Felder dürfen nicht null oder leer sein
}
```

**Preisvorstellung**

```java
public record Preisvorstellung(BigDecimal betrag, String währung) {
    // Compact Constructor mit Validierung:
    // - betrag muss größer als 0 sein
    // - währung darf nicht null oder leer sein
}
```

**Provision**

```java
public record Provision(BigDecimal prozentsatz) {
    // Compact Constructor mit Validierung:
    // - prozentsatz muss größer als 0 und kleiner oder gleich 100 sein
}
```

### Schritt 2: Domain Events als Records

Erstelle die folgenden Domain Events als Records im Package `de.immobiliencrm.vermittlung.domain.event`:

```java
public record BesichtigungDurchgeführt(
    UUID vermittlungsvorgangId,
    UUID besichtigungId,
    LocalDateTime zeitpunkt
) {}

public record AngebotEingegangen(
    UUID vermittlungsvorgangId,
    BigDecimal angebotsBetrag,
    LocalDateTime zeitpunkt
) {}

public record AngebotAngenommen(
    UUID vermittlungsvorgangId,
    UUID angebotId,
    LocalDateTime zeitpunkt
) {}
```

### Schritt 3: Entity Besichtigung (innerhalb des Aggregats)

Erstelle die Entity `Besichtigung` im Package `de.immobiliencrm.vermittlung.domain.model`:

- Felder: `id` (UUID), `interessentName` (String), `zeitpunkt` (LocalDateTime), `notizen` (String), `durchgeführt` (boolean)
- Methode: `durchführen()` setzt `durchgeführt` auf `true`

### Schritt 4: Entity Angebot (innerhalb des Aggregats)

Erstelle die Entity `Angebot` im Package `de.immobiliencrm.vermittlung.domain.model`:

- Felder: `id` (UUID), `interessentName` (String), `betrag` (BigDecimal), `eingegangen` (LocalDateTime), `angenommen` (boolean)
- Methode: `annehmen()` setzt `angenommen` auf `true`

### Schritt 5: Aggregate Root Vermittlungsvorgang

Erstelle die Aggregate Root Klasse `Vermittlungsvorgang` im Package `de.immobiliencrm.vermittlung.domain.model`:

**Felder:**

- `id` (UUID)
- `immobilieId` (UUID)
- `adresse` (Adresse)
- `preisvorstellung` (Preisvorstellung)
- `provision` (Provision)
- `status` (Enum: NEU, IN_VERMARKTUNG, BESICHTIGUNG, ANGEBOT_PHASE, NOTARTERMIN, ABGESCHLOSSEN)
- `besichtigungen` (List\<Besichtigung\>)
- `angebote` (List\<Angebot\>)
- `domainEvents` (List\<Object\>, transient)

**Methoden:**

- `besichtigungHinzufügen(String interessentName, LocalDateTime zeitpunkt, String notizen)` - fügt eine neue Besichtigung hinzu und setzt Status auf BESICHTIGUNG
- `besichtigungDurchführen(UUID besichtigungId)` - markiert eine Besichtigung als durchgeführt und erzeugt ein `BesichtigungDurchgeführt`-Event
- `angebotEntgegennehmen(String interessentName, BigDecimal betrag)` - fügt ein neues Angebot hinzu, setzt Status auf ANGEBOT_PHASE und erzeugt ein `AngebotEingegangen`-Event
- `angebotAnnehmen(UUID angebotId)` - nimmt ein Angebot an und erzeugt ein `AngebotAngenommen`-Event
- `statusAufNotarterminSetzen()` - setzt den Status auf NOTARTERMIN; wirft eine `IllegalStateException`, wenn kein angenommenes Angebot vorliegt

**Invariante:**

`statusAufNotarterminSetzen()` darf nur aufgerufen werden, wenn mindestens ein angenommenes Angebot existiert. Andernfalls wird eine `IllegalStateException` geworfen.

### Schritt 6: Repository Interface

Erstelle das Interface `VermittlungsvorgangRepository` im Package `de.immobiliencrm.vermittlung.domain.port`:

```java
public interface VermittlungsvorgangRepository {
    Optional<Vermittlungsvorgang> findById(UUID id);
    Vermittlungsvorgang save(Vermittlungsvorgang vermittlungsvorgang);
    void deleteById(UUID id);
}
```

**Wichtig:** Keine Spring-Imports in diesem Interface verwenden. Es handelt sich um ein reines Java-Interface.

### Bonus: Factory-Methode

Implementiere eine statische Factory-Methode auf `Vermittlungsvorgang`:

```java
public static Vermittlungsvorgang erstellen(
    UUID immobilieId,
    Adresse adresse,
    Preisvorstellung preisvorstellung,
    Provision provision
) {
    // Erzeugt einen neuen Vermittlungsvorgang mit Status NEU
}
```

## Verifikation

Schreibe einen Unit-Test, der die Invariante prüft:

1. Erstelle einen neuen `Vermittlungsvorgang`
2. Rufe `statusAufNotarterminSetzen()` auf - es muss eine `IllegalStateException` geworfen werden
3. Füge ein Angebot hinzu und nimm es an
4. Rufe `statusAufNotarterminSetzen()` erneut auf - jetzt muss es erfolgreich sein

```bash
cd solution
mvn test
```

Alle Tests müssen grün sein.

## Tipps

- Value Objects sind in Java am besten als Records abbildbar - sie sind automatisch immutable und haben `equals()`/`hashCode()`.
- Domain Events werden im Aggregate Root gesammelt und erst beim Speichern veröffentlicht.
- Das Repository-Interface gehört zur Domain-Schicht und darf keine Framework-Abhängigkeiten haben.
