# Lab 04: Taktisches DDD - Building Blocks implementieren

Modelliere und implementiere den Bounded Context "Antragstellung"
(Förderantragsmappe).

Einordnung im Kurs: In Lab 03 wurde die Domäne bewusst breiter in mehrere
Bounded Contexts zerlegt. Ab diesem Lab vertiefen wir aus Zeitgründen
exemplarisch einen ausgewählten Context und führen die übrigen nicht
parallel weiter.

## Schritt 1: Typed IDs und Value Objects als Java Records

Erstelle die folgenden Typed IDs und Value Objects als Java Records im Package
`de.foerderung.antragstellung.domain.model`.

Typed IDs eliminieren Primitive Obsession — statt überall `UUID` zu verwenden,
haben Entities eigene ID-Typen mit fachlicher Bedeutung.

AntragId (*Identität der AntragsMappe*)

```java
public record AntragId(UUID value) {
    public AntragId {
        Objects.requireNonNull(value, "AntragId darf nicht null sein");
    }
    public static AntragId generate() {
        return new AntragId(UUID.randomUUID());
    }
}
```

FlurstueckId und NachweisId folgen demselben Muster.

Foerderbetrag (*Beantragter Förderbetrag*)

```java
public record Foerderbetrag(BigDecimal betrag, String waehrung) {
    public Foerderbetrag {
        // betrag must not be null and must not be negative
        // waehrung must not be null or blank
    }
}
```

Foerderquote (*Förderanteil als Dezimalfaktor, z.B. 0.35 = 35%*)

```java
public record Foerderquote(BigDecimal prozentsatz) {
    public Foerderquote {
        // prozentsatz must not be null
        // prozentsatz must be between 0 and 1 (inclusive)
    }
}
```

RegistrierungsNummer (*Registrierungsnummer mit Formatvalidierung*)

```java
public record RegistrierungsNummer(String wert) {
    private static final Pattern FORMAT = Pattern.compile("DZ-[A-Z]{2}-\\d{4}-\\d{4}");

    public RegistrierungsNummer {
        Objects.requireNonNull(wert);
        if (!FORMAT.matcher(wert).matches()) {
            throw new IllegalArgumentException(
                "RegistrierungsNummer muss dem Format DZ-XX-YYYY-NNNN entsprechen: " + wert);
        }
    }
}
```

FlurstueckNummer (*Kataster-Parzellen-Kennung*)

```java
public record FlurstueckNummer(String wert) {
    public FlurstueckNummer {
        Objects.requireNonNull(wert, "FlurstueckNummer darf nicht null sein");
        if (wert.isBlank()) throw new IllegalArgumentException(
            "FlurstueckNummer darf nicht leer sein");
    }
}
```

## Schritt 2: Domain Events als Records

Erstelle die folgenden Domain Events als Records im Package
`de.foerderung.antragstellung.domain.event`. Domain Events beschreiben etwas,
das fachlich passiert ist — daher Vergangenheitsform.

Events verwenden Typed IDs und `Instant` als Zeitstempel:

```java
public sealed interface AntragEvent permits
        AntragsmappeErstellt, FlurstueckHinzugefuegt,
        NachweisEingereicht, NachweisAkzeptiert,
        AntragsmappeEingereicht {}

public record AntragsmappeErstellt(  // AntragsMappe wurde erstellt
        AntragId antragsmappeId,
        RegistrierungsNummer registrierungsNummer,
        Instant occurredAt
) implements AntragEvent {}

public record FlurstueckHinzugefuegt(  // Flurstück wurde zur Mappe hinzugefügt
        AntragId antragsmappeId,
        FlurstueckId flurstueckId,
        FlurstueckNummer flurstueckNummer,
        BigDecimal flaeche,
        Instant occurredAt
) implements AntragEvent {}

public record NachweisEingereicht(  // Nachweis wurde eingereicht
        AntragId antragsmappeId,
        NachweisId nachweisId,
        String dokumentTyp,
        Instant occurredAt
) implements AntragEvent {}

public record NachweisAkzeptiert(  // Nachweis wurde akzeptiert
        AntragId antragsmappeId,
        NachweisId nachweisId,
        Instant occurredAt
) implements AntragEvent {}

public record AntragsmappeEingereicht(  // AntragsMappe wurde eingereicht
        AntragId antragsmappeId,
        Instant occurredAt
) implements AntragEvent {}
```

## Schritt 3: Entity Flurstueck (*Katasterparzelle*)

Erstelle die Entity `Flurstueck` im Package
`de.foerderung.antragstellung.domain.model`:

- Felder: `id` (FlurstueckId), `nummer` (FlurstueckNummer), `flaeche` (BigDecimal),
  `bemerkung` (String), `geprueft` (boolean)
- Methode: `pruefen()` setzt `geprueft` auf `true`

Auch Entities sollten ihre eigenen Basis-Invarianten schützen, z.B.
`nummer` darf nicht `null` sein und `flaeche` muss größer als 0 sein.

Entities haben eine Identität (`id`) und einen Lebenszyklus. Im Gegensatz zu
Value Objects werden sie anhand ihrer ID verglichen, nicht anhand ihrer Werte.

## Schritt 4: Entity Nachweis (*Fördernachweis*)

Erstelle die Entity `Nachweis` im Package
`de.foerderung.antragstellung.domain.model`:

- Felder: `id` (NachweisId), `dokumentTyp` (String), `eingereichtVon` (String),
  `eingereichtAm` (Instant), `akzeptiert` (boolean)
- Methode: `akzeptieren()` setzt `akzeptiert` auf `true`

Auch hier gilt: `dokumentTyp` darf nicht leer sein und `eingereichtVon`
darf nicht leer sein.

## Schritt 5: Aggregate Root AntragsMappe (*Förderantragsmappe*)

Erstelle die Aggregate-Root-Klasse `AntragsMappe` im Package
`de.foerderung.antragstellung.domain.model`.

Der Aggregate Root ist der einzige Einstiegspunkt für Änderungen am
Aggregat. Alle Zustandsänderungen an Flurstücken und Nachweisen laufen über
Methoden der AntragsMappe — nie direkt von außen.

In Java bedeutet das praktisch: Mutierende Methoden auf Child-Entities sollten
nicht öffentlich sein (z. B. package-private), sonst kann das Aggregat trotz
`unmodifiableList()` von außen umgangen werden.

Felder:

- `id` (AntragId)
- `registrierungsNummer` (RegistrierungsNummer)
- `beantragteFoerderung` (Foerderbetrag)
- `foerderquote` (Foerderquote)
- `status` (Enum `AntragStatus`: NEU, IN_BEARBEITUNG, EINGEREICHT,
  ABGESCHLOSSEN)
- `flurstuecke` (List\<Flurstueck\>)
- `nachweise` (List\<Nachweis\>)
- `domainEvents` (List\<AntragEvent\>)

Methoden:

| Methode                                                        | Beschreibung                                                            | Rückgabe        |
|----------------------------------------------------------------|-------------------------------------------------------------------------|-----------------|
| `flurstueckHinzufuegen(FlurstueckNummer, flaeche, bemerkung)`  | Neues Flurstück hinzufügen, Status → IN_BEARBEITUNG, Event erzeugen     | `FlurstueckId`  |
| `flurstueckPruefen(FlurstueckId)`                              | Flurstück als geprüft markieren                                         | `void`          |
| `nachweisEinreichen(dokumentTyp, eingereichtVon)`              | Neuen Nachweis anlegen, `NachweisEingereicht`-Event                     | `NachweisId`    |
| `nachweisAkzeptieren(NachweisId)`                              | Nachweis akzeptieren, `NachweisAkzeptiert`-Event                        | `void`          |
| `einreichen()`                                                 | Status → EINGEREICHT, `AntragsmappeEingereicht`-Event                   | `void`          |

Wichtig: `flurstueckHinzufuegen()` gibt nur die `FlurstueckId` zurück — nicht
die Entity selbst. Innere Objekte dürfen nicht direkt nach außen weitergegeben
werden (Aggregate-Regel #2, vgl. Slides Modul 06).

Domain Events sammeln: Der Aggregate Root sammelt Domain Events in einer Liste.
Events werden beim Aufruf der Geschäftsmethoden erzeugt und
erst später (beim Speichern) veröffentlicht. Dafür braucht es zusätzlich:

- `getDomainEvents()` — gibt eine unveränderliche Kopie der Liste zurück
- `clearDomainEvents()` — leert die Liste nach der Veröffentlichung

Invariante:

`einreichen()` darf nur aufgerufen werden, wenn mindestens ein Flurstück
existiert. Andernfalls wird eine `IllegalStateException` geworfen
(vgl. Slides Modul 06).

## Schritt 6: Factory-Methoden

Implementiere zwei statische Factory-Methoden auf `AntragsMappe`:

```java
public static AntragsMappe erstellen(
        RegistrierungsNummer registrierungsNummer,
        Foerderbetrag beantragteFoerderung,
        Foerderquote foerderquote
) {
    // Creates a new AntragsMappe with AntragId.generate() and status NEU
    // Registers an AntragsmappeErstellt domain event
}
```

```java
public static AntragsMappe rekonstruieren(
        AntragId id,
        RegistrierungsNummer registrierungsNummer,
        Foerderbetrag beantragteFoerderung,
        Foerderquote foerderquote,
        AntragStatus status,
        List<Flurstueck> flurstuecke,
        List<Nachweis> nachweise
) {
    // Restores an existing aggregate without creating domain events
}
```

`erstellen()` drückt die Erzeugungsabsicht fachlich aus und registriert
ein `AntragsmappeErstellt`-Event. `rekonstruieren()` wird vom
Persistenz-Adapter aufgerufen und erzeugt keine Events.

Dasselbe Muster kann auch für Child-Entities sinnvoll sein, wenn ein Adapter
deren Zustand (z. B. `geprueft` oder `akzeptiert`) aus Persistenz laden muss.

## Schritt 7: Repository-Interface

Erstelle das Interface `AntragsMappeRepository` im Package
`de.foerderung.antragstellung.domain.port`:

```java
public interface AntragsMappeRepository {
    Optional<AntragsMappe> findById(AntragId id);

    AntragsMappe save(AntragsMappe antragsMappe);

    void deleteById(AntragId id);
}
```

Wichtig: Keine Spring-Imports in diesem Interface verwenden. Es ist ein
reines Java-Interface (ein "Port" im Sinne der Hexagonalen Architektur).
Die konkrete Implementierung (der "Adapter") folgt in Lab 05.

## Tests

Schreibe Unit-Tests, die folgende Szenarien abdecken:

### Value Objects

1. Gültige Werte → Objekt wird erzeugt
2. Ungültige Werte (null, blank, negativ) → Exception wird geworfen
3. Zwei Value Objects mit gleichen Werten → `equals()` gibt `true` zurück
4. `Foerderquote` mit Wert > 1 → Exception (Bereich 0-1)
5. `RegistrierungsNummer` mit falschem Format → Exception

### Aggregate Root - Factory und Events

1. `erstellen()` erzeugt ein `AntragsmappeErstellt`-Event
2. `rekonstruieren()` erzeugt keine Events

### Aggregate Root - Invariante

1. Erstelle eine neue `AntragsMappe`
2. Rufe `einreichen()` auf — eine `IllegalStateException`
   muss geworfen werden
3. Füge ein Flurstück hinzu
4. Rufe `einreichen()` erneut auf — diesmal muss es
   erfolgreich sein

### Aggregate Root - Kompletter Fluss

1. Flurstück hinzufügen → Status ist IN_BEARBEITUNG, `FlurstueckHinzugefuegt`-Event mit allen Feldern
2. Flurstück prüfen → Flurstück ist als geprüft markiert
3. Nachweis einreichen → `NachweisEingereicht`-Event
4. Nachweis akzeptieren → `NachweisAkzeptiert`-Event
5. Einreichen → Status ist EINGEREICHT, `AntragsmappeEingereicht`-Event

### Zusätzliche Randfälle

1. Leere FlurstueckNummer bei `Flurstueck` → Exception
2. Nachweis mit leerem `dokumentTyp` → Exception
3. Nicht vorhandenes Flurstück prüfen → Exception
4. Nicht vorhandenen Nachweis akzeptieren → Exception

## Hinweise

- Typed IDs vermeiden Verwechslungen (z. B. versehentlich eine `FlurstueckId`
  als `AntragId` übergeben) und drücken die fachliche Rolle des Parameters aus.
- Value Objects werden in Java am besten als Records abgebildet — sie sind
  automatisch unveränderlich und haben `equals()`/`hashCode()`.
- Der Compact Constructor wird bei Records für
  Validierung im Constructor verwendet.
- Das Repository-Interface gehört zur Domain-Schicht und darf keine
  Framework-Abhängigkeiten haben.
- Listen, die nach außen gegeben werden, sollten mit
  `Collections.unmodifiableList()` geschützt werden.
- Wichtig: `unmodifiableList()` schützt nur die Liste, nicht die enthaltenen
  Entities. Wenn Child-Entities veränderlich sind, sollten mutierende Methoden
  nicht öffentlich sein.
