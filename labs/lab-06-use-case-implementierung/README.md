# Lab 06: Use-Case-Implementierung

Implementiere zwei Use Cases als Application Services im Antragstellung-Kontext:

1. Flurstück hinzufügen (`FlurstueckHinzufuegenService`)
2. Flurstück prüfen (`FlurstueckPruefenService`)

Beide Use Cases folgen dem gleichen Grundmuster: Aggregate laden, Domain-Methode
aufrufen, Aggregate speichern. Die Geschäftslogik bleibt dabei vollständig im
Domain-Modell.

## Schritt 1: Inbound Port Interfaces definieren

Erstelle im Package `de.foerderung.antragstellung.application.port` zwei
Interfaces, die den Use Case als Vertrag definieren:

```java
public interface FlurstueckHinzufuegen {
    FlurstueckHinzufuegenResult hinzufuegen(FlurstueckHinzufuegenCommand command);
}
```

```java
public interface FlurstueckPruefen {
    void pruefen(FlurstueckPruefenCommand command);
}
```

Inbound Ports sind die öffentliche API der Application-Schicht. Der REST-Adapter
(Lab 07) wird später nur diese Interfaces injizieren — nie die Service-Klasse
direkt.

## Schritt 2: Command-Objekte erstellen

Erstelle im Package `de.foerderung.antragstellung.application.command` zwei
Command-Records mit Typed IDs:

FlurstueckHinzufuegenCommand:

```java
public record FlurstueckHinzufuegenCommand(
        AntragId antragsmappeId,
        FlurstueckNummer flurstueckNummer,
        BigDecimal flaeche
) {
    public FlurstueckHinzufuegenCommand {
        Objects.requireNonNull(antragsmappeId);
        Objects.requireNonNull(flurstueckNummer, "Flurstuecknummer ist erforderlich");
    }
}
```

FlurstueckPruefenCommand:

```java
public record FlurstueckPruefenCommand(
        AntragId antragsmappeId,
        FlurstueckId flurstueckId
) {
    public FlurstueckPruefenCommand {
        Objects.requireNonNull(antragsmappeId);
        Objects.requireNonNull(flurstueckId);
    }
}
```

## Schritt 3: Ergebnisobjekt erstellen

Erstelle `FlurstueckHinzufuegenResult` als Record im selben Package mit
Typed IDs:

```java
public record FlurstueckHinzufuegenResult(
        FlurstueckId flurstueckId,
        AntragId antragsmappeId,
        FlurstueckNummer flurstueckNummer,
        BigDecimal flaeche
) {}
```

Hinweis: `FlurstueckPruefenService` gibt kein Ergebnis zurück (`void`), da die
Zustandsveränderung keine neue Ressource erzeugt.

## Schritt 4: Exception erstellen

Erstelle `AntragsmappeNichtGefundenException` als `RuntimeException` im Package
`de.foerderung.antragstellung.domain.model`. Der Konstruktor nimmt eine
`AntragId` entgegen:

```java
public class AntragsmappeNichtGefundenException extends RuntimeException {
    public AntragsmappeNichtGefundenException(AntragId antragsmappeId) {
        super("AntragsMappe mit ID " + antragsmappeId + " nicht gefunden");
    }
}
```

## Schritt 5: FlurstueckHinzufuegenService implementieren

Erstelle den Application Service als `@Service` im Package
`de.foerderung.antragstellung.application.service`. Der Service **implementiert
das Port Interface**:

```java
@Service
@Transactional
public class FlurstueckHinzufuegenService implements FlurstueckHinzufuegen {

    private final AntragsMappeRepository repository;

    @Override
    public FlurstueckHinzufuegenResult hinzufuegen(FlurstueckHinzufuegenCommand cmd) {
        var mappe = repository.findById(cmd.antragsmappeId())
            .orElseThrow(() -> new AntragsmappeNichtGefundenException(
                cmd.antragsmappeId()));

        var flurstueckId = mappe.flurstueckHinzufuegen(
            cmd.flurstueckNummer(), cmd.flaeche(), null);

        repository.save(mappe);

        return new FlurstueckHinzufuegenResult(
            flurstueckId, mappe.getId(),
            cmd.flurstueckNummer(), cmd.flaeche());
    }
}
```

## Schritt 6: FlurstueckPruefenService implementieren

Erstelle den Application Service `FlurstueckPruefenService` als `@Service` mit
`@Transactional`, implementiert `FlurstueckPruefen`:

```java
@Service
@Transactional
public class FlurstueckPruefenService implements FlurstueckPruefen {

    private final AntragsMappeRepository repository;

    @Override
    public void pruefen(FlurstueckPruefenCommand cmd) {
        var mappe = repository.findById(cmd.antragsmappeId())
            .orElseThrow(() -> new AntragsmappeNichtGefundenException(
                cmd.antragsmappeId()));

        mappe.flurstueckPruefen(cmd.flurstueckId());

        repository.save(mappe);
    }
}
```

Beachte: Die Domain-Methode `flurstueckPruefen()` wirft selbst eine
`IllegalArgumentException`, wenn die Flurstück-ID nicht existiert.

## Tests

Schreibe Unit-Tests für beide Use Cases mit einem gemockten Repository
(`@ExtendWith(MockitoExtension.class)`).

FlurstueckHinzufuegenServiceTest:

1. *Happy Path:* AntragsMappe existiert → Flurstück wird erstellt → Ergebnis
   enthält korrekte IDs → `save()` wird aufgerufen
2. *AntragsMappe nicht gefunden:* `AntragsmappeNichtGefundenException` wird
   geworfen → `save()` wird nicht aufgerufen

FlurstueckPruefenServiceTest:

3. *Happy Path:* Flurstück wird als geprüft markiert → `save()` wird aufgerufen
4. *AntragsMappe nicht gefunden:* `AntragsmappeNichtGefundenException` wird
   geworfen → `save()` wird nicht aufgerufen
5. *Flurstück nicht gefunden:* `IllegalArgumentException` wird aus der Domain
   geworfen → `save()` wird nicht aufgerufen

## Hinweise

- Die Use Cases sind bewusst schlank — die Geschäftslogik liegt im
  Domain-Modell.
- **Inbound Port Interfaces** entkoppeln die API von der Implementierung.
  Der Controller (Lab 07) injiziert das Interface, nicht die Service-Klasse.
- Commands und Results verwenden **Typed IDs** (`AntragId`, `FlurstueckId`),
  um Verwechslungen auf Compile-Ebene zu verhindern.
- `@Transactional` stellt sicher, dass bei einer Exception ein Rollback erfolgt.
- Das Repository-Interface kommt aus der Domain-Schicht (Port) — der Use Case
  hängt nur von der Abstraktion ab, nicht von der konkreten Implementierung.
