# Lab 05: Taktisches DDD - Building Blocks implementieren

## Lernziel

Aggregate Root, Entities, Value Objects und Domain Events in Java implementieren.

## Dauer

90 Minuten

## Voraussetzungen

- Lab 02 abgeschlossen
- Slides Modul 06

## Aufgabe

Modelliere und implementiere den Bounded Context „Vermittlungsprozess" (Brokerage Process).

### Schritt 1: Value Objects als Java Records erstellen

Erstelle die folgenden Value Objects als Java Records im Package `de.realestate.brokerage.domain.model`:

**Address**

```java
public record Address(String street, String postalCode, String city) {
    // Compact constructor with validation:
    // - All fields must not be null or blank
}
```

**AskingPrice**

```java
public record AskingPrice(BigDecimal amount, String currency) {
    // Compact constructor with validation:
    // - amount must be greater than 0
    // - currency must not be null or blank
}
```

**Commission**

```java
public record Commission(BigDecimal percentage) {
    // Compact constructor with validation:
    // - percentage must be greater than 0 and less than or equal to 100
}
```

### Schritt 2: Domain Events als Records

Erstelle die folgenden Domain Events als Records im Package `de.realestate.brokerage.domain.event`:

```java
public record ViewingCompleted(
    UUID brokerageProcessId,
    UUID viewingId,
    LocalDateTime timestamp
) {}

public record OfferReceived(
    UUID brokerageProcessId,
    BigDecimal offerAmount,
    LocalDateTime timestamp
) {}

public record OfferAccepted(
    UUID brokerageProcessId,
    UUID offerId,
    LocalDateTime timestamp
) {}
```

### Schritt 3: Entity Viewing (innerhalb des Aggregats)

Erstelle die Entity `Viewing` im Package `de.realestate.brokerage.domain.model`:

- Felder: `id` (UUID), `prospectName` (String), `timestamp` (LocalDateTime), `notes` (String), `completed` (boolean)
- Methode: `complete()` setzt `completed` auf `true`

### Schritt 4: Entity Offer (innerhalb des Aggregats)

Erstelle die Entity `Offer` im Package `de.realestate.brokerage.domain.model`:

- Felder: `id` (UUID), `prospectName` (String), `amount` (BigDecimal), `receivedAt` (LocalDateTime), `accepted` (boolean)
- Methode: `accept()` setzt `accepted` auf `true`

### Schritt 5: Aggregate Root BrokerageProcess

Erstelle die Aggregate-Root-Klasse `BrokerageProcess` im Package `de.realestate.brokerage.domain.model`:

**Felder:**

- `id` (UUID)
- `propertyId` (UUID)
- `address` (Address)
- `askingPrice` (AskingPrice)
- `commission` (Commission)
- `status` (Enum: NEW, IN_MARKETING, VIEWING, OFFER_PHASE, NOTARY_APPOINTMENT, COMPLETED)
- `viewings` (List\<Viewing\>)
- `offers` (List\<Offer\>)
- `domainEvents` (List\<Object\>, transient)

**Methoden:**

- `addViewing(String prospectName, LocalDateTime timestamp, String notes)` – fügt eine neue Besichtigung hinzu und setzt den Status auf VIEWING
- `completeViewing(UUID viewingId)` – markiert eine Besichtigung als abgeschlossen und erzeugt ein `ViewingCompleted`-Event
- `receiveOffer(String prospectName, BigDecimal amount)` – fügt ein neues Angebot hinzu, setzt den Status auf OFFER_PHASE und erzeugt ein `OfferReceived`-Event
- `acceptOffer(UUID offerId)` – nimmt ein Angebot an und erzeugt ein `OfferAccepted`-Event
- `setStatusToNotaryAppointment()` – setzt den Status auf NOTARY_APPOINTMENT; wirft eine `IllegalStateException`, wenn kein angenommenes Angebot existiert

**Invariante:**

`setStatusToNotaryAppointment()` darf nur aufgerufen werden, wenn mindestens ein angenommenes Angebot existiert. Andernfalls wird eine `IllegalStateException` geworfen.

### Schritt 6: Repository-Interface

Erstelle das Interface `BrokerageProcessRepository` im Package `de.realestate.brokerage.domain.port`:

```java
public interface BrokerageProcessRepository {
    Optional<BrokerageProcess> findById(UUID id);
    BrokerageProcess save(BrokerageProcess brokerageProcess);
    void deleteById(UUID id);
}
```

**Wichtig:** Keine Spring-Imports in diesem Interface verwenden. Es ist ein reines Java-Interface.

### Bonus: Factory-Methode

Implementiere eine statische Factory-Methode auf `BrokerageProcess`:

```java
public static BrokerageProcess create(
    UUID propertyId,
    Address address,
    AskingPrice askingPrice,
    Commission commission
) {
    // Creates a new BrokerageProcess with status NEW
}
```

## Verifikation

Schreibe einen Unit-Test, der die Invariante überprüft:

1. Erstelle einen neuen `BrokerageProcess`
2. Rufe `setStatusToNotaryAppointment()` auf – eine `IllegalStateException` muss geworfen werden
3. Füge ein Angebot hinzu und nimm es an
4. Rufe `setStatusToNotaryAppointment()` erneut auf – diesmal muss es erfolgreich sein

```bash
cd ../../solutions/lab-05-building-blocks
mvn test
```

Alle Tests müssen grün sein.

## Tipps

- Value Objects werden in Java am besten als Records abgebildet – sie sind automatisch unveränderlich und haben `equals()`/`hashCode()`.
- Domain Events werden im Aggregate Root gesammelt und erst beim Speichern veröffentlicht.
- Das Repository-Interface gehört zur Domain-Schicht und darf keine Framework-Abhängigkeiten haben.
