# Lab 04: Taktisches DDD - Building Blocks implementieren

Modelliere und implementiere den Bounded Context "Vermittlungsprozess"
(Brokerage Process).

Einordnung im Kurs: In Lab 03 wurde die Domäne bewusst breiter in mehrere
Bounded Contexts zerlegt. Ab diesem Lab vertiefen wir aus Zeitgründen
exemplarisch einen ausgewählten Context und führen die übrigen nicht
parallel weiter.

## Schritt 1: Value Objects als Java Records erstellen

Erstelle die folgenden Value Objects als Java Records im Package
`de.realestate.brokerage.domain.model`.

Value Objects sind unveränderlich, durch ihre Werte definiert (nicht durch
eine ID), und schützen ihre eigenen Invarianten im Constructor.

AskingPrice (*Angebotspreis*)

```java
public record AskingPrice(BigDecimal amount, String currency) {
    public AskingPrice {
        // amount must not be null and must be greater than 0
        // currency must not be null or blank
    }
}
```

Commission (*Maklerprovision*)

```java
public record Commission(BigDecimal percentage) {
    public Commission {
        // percentage must not be null
        // percentage must be greater than 0 and less than or equal to 100
    }
}
```

## Schritt 2: Domain Events als Records

Erstelle die folgenden Domain Events als Records im Package
`de.realestate.brokerage.domain.event`. Domain Events beschreiben etwas,
das fachlich passiert ist - daher Vergangenheitsform.

```java

public interface BrokerageEvent {
}

public record ViewingCompleted( // Besichtigung abgeschlossen
        UUID brokerageProcessId,
        UUID viewingId,
        LocalDateTime timestamp
) implements BrokerageEvent {}

public record OfferReceived( // Käuferangebot erhalten
        UUID brokerageProcessId,
        BigDecimal offerAmount,
        LocalDateTime timestamp
) implements BrokerageEvent {}

public record OfferAccepted( // Käuferangebot angenommen
        UUID brokerageProcessId,
        UUID offerId,
        LocalDateTime timestamp
) implements BrokerageEvent {}
```

## Schritt 3: Entity Viewing (*Besichtigung*)

Erstelle die Entity `Viewing` im Package
`de.realestate.brokerage.domain.model`:

- Felder: `id` (UUID), `prospectName` (String), `scheduledAt` (LocalDateTime),
  `notes` (String), `completed` (boolean)
- Methode: `complete()` setzt `completed` auf `true`

Auch Entities sollten ihre eigenen Basis-Invarianten schützen, z.B.
`prospectName` darf nicht `null` oder leer sein.

Entities haben eine Identität (`id`) und einen Lebenszyklus. Im Gegensatz zu
Value Objects werden sie anhand ihrer ID verglichen, nicht anhand ihrer Werte.

## Schritt 4: Entity Offer (*Käuferangebot*)

Erstelle die Entity `Offer` im Package
`de.realestate.brokerage.domain.model`:

- Felder: `id` (UUID), `prospectName` (String), `amount` (BigDecimal),
  `receivedAt` (LocalDateTime), `accepted` (boolean)
- Methode: `accept()` setzt `accepted` auf `true`

Auch hier gilt: `prospectName` darf nicht leer sein und `amount` muss größer
als `0` sein.

## Schritt 5: Aggregate Root BrokerageProcess (*Vermittlungsvorgang*)

Erstelle die Aggregate-Root-Klasse `BrokerageProcess` im Package
`de.realestate.brokerage.domain.model`.

Der Aggregate Root ist der einzige Einstiegspunkt für Änderungen am
Aggregat. Alle Zustandsänderungen an Viewings und Offers laufen über Methoden
des BrokerageProcess - nie direkt von außen.

In Java bedeutet das praktisch: Mutierende Methoden auf Child-Entities sollten
nicht öffentlich sein (z. B. package-private), sonst kann das Aggregat trotz
`unmodifiableList()` von außen umgangen werden.

Felder:

- `id` (UUID)
- `propertyId` (UUID)
- `askingPrice` (AskingPrice)
- `commission` (Commission)
- `status` (Enum: NEW, IN_MARKETING, VIEWING, OFFER_PHASE, NOTARY_APPOINTMENT,
  COMPLETED)
- `viewings` (List\<Viewing\>)
- `offers` (List\<Offer\>)
- `domainEvents` (List\<BrokerageEvent\>)

Methoden:

| Methode                                      | Beschreibung                                                | Rückgabe  |
|----------------------------------------------|-------------------------------------------------------------|-----------|
| `addViewing(prospectName, timestamp, notes)` | Neue Besichtigung hinzufügen, Status → VIEWING              | `Viewing` |
| `completeViewing(viewingId)`                 | Besichtigung abschließen, `ViewingCompleted`-Event erzeugen | `void`    |
| `receiveOffer(prospectName, amount)`         | Neues Angebot, Status → OFFER_PHASE, `OfferReceived`-Event  | `Offer`   |
| `acceptOffer(offerId)`                       | Angebot annehmen, `OfferAccepted`-Event                     | `void`    |
| `setStatusToNotaryAppointment()`             | Status → NOTARY_APPOINTMENT                                 | `void`    |

Domain Events sammeln: Der Aggregate Root sammelt Domain Events in einer
transienten Liste. Events werden beim Aufruf der Geschäftsmethoden erzeugt und
erst später (beim Speichern) veröffentlicht. Dafür braucht es zusätzlich:

- `getDomainEvents()` - gibt eine unveränderliche Kopie der Liste zurück
- `clearDomainEvents()` - leert die Liste nach der Veröffentlichung

Invariante:

`setStatusToNotaryAppointment()` darf nur aufgerufen werden, wenn mindestens
ein angenommenes Angebot existiert. Andernfalls wird eine
`IllegalStateException` geworfen.

## Schritt 6: Repository-Interface

Erstelle das Interface `BrokerageProcessRepository` im Package
`de.realestate.brokerage.domain.port`:

```java
public interface BrokerageProcessRepository {
    Optional<BrokerageProcess> findById(UUID id);

    BrokerageProcess save(BrokerageProcess brokerageProcess);

    void deleteById(UUID id);
}
```

Wichtig: Keine Spring-Imports in diesem Interface verwenden. Es ist ein
reines Java-Interface (ein "Port" im Sinne der Hexagonalen Architektur).
Die konkrete Implementierung (der "Adapter") folgt in Lab 05.

## Bonus: Factory-Methode

Implementiere eine statische Factory-Methode auf `BrokerageProcess`:

```java
public static BrokerageProcess create(
        UUID propertyId,
        AskingPrice askingPrice,
        Commission commission
) {
    // Creates a new BrokerageProcess with a random UUID and status NEW
}
```

Factory-Methoden drücken die Erzeugungsabsicht fachlich aus und stellen
sicher, dass das Objekt von Anfang an in einem gültigen Zustand ist.

## Optional: Rekonstitution für Lab 05 vorbereiten

Da im nächsten Lab ein Persistenz-Adapter folgt, ist es sinnvoll, schon jetzt
einen zweiten Fabrikpfad für bereits gespeicherte Aggregate vorzusehen:

```java
public static BrokerageProcess reconstitute(
        UUID id,
        UUID propertyId,
        AskingPrice askingPrice,
        Commission commission,
        ProcessStatus status,
        List<Viewing> viewings,
        List<Offer> offers
) {
    // Restores an existing aggregate without creating domain events
}
```

Dasselbe Muster kann auch für Child-Entities sinnvoll sein, wenn ein Adapter
deren Zustand (z. B. `completed` oder `accepted`) aus Persistenz laden muss.

## Tests

Schreibe Unit-Tests, die folgende Szenarien abdecken:

### Value Objects

1. Gültige Werte → Objekt wird erzeugt
2. Ungültige Werte (null, blank, negativ) → Exception wird geworfen
3. Zwei Value Objects mit gleichen Werten → `equals()` gibt `true` zurück

### Aggregate Root - Invariante

1. Erstelle einen neuen `BrokerageProcess`
2. Rufe `setStatusToNotaryAppointment()` auf - eine `IllegalStateException`
   muss geworfen werden
3. Füge ein Angebot hinzu und nimm es an
4. Rufe `setStatusToNotaryAppointment()` erneut auf - diesmal muss es
   erfolgreich sein

### Aggregate Root - Kompletter Fluss

1. Besichtigung hinzufügen → Status ist VIEWING
2. Besichtigung abschließen → `ViewingCompleted`-Event in der Event-Liste
3. Angebot empfangen → Status ist OFFER_PHASE, `OfferReceived`-Event
4. Angebot annehmen → `OfferAccepted`-Event
5. Notartermin setzen → Status ist NOTARY_APPOINTMENT

### Zusätzliche Randfälle

1. Leerer Interessentenname bei `Viewing` oder `Offer` → Exception
2. Angebot mit `amount <= 0` → Exception
3. Bereits gespeichertes Aggregat über `reconstitute(...)` laden → Zustand wird
   wiederhergestellt, `domainEvents` bleibt leer

## Hinweise

- Value Objects werden in Java am besten als Records abgebildet - sie sind
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
