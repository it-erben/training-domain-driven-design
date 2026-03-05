---
marp: true
theme: default
paginate: true
header: "DDD & Clean Architecture mit Spring Boot 3"
footer: "CC BY-NC-SA 4.0, Alexander Erben"
---

# Modul 05 – Tactical DDD Building Blocks

## Die Bausteine des Domain-Driven Design

**Geschätzte Dauer:** ca. 90 Minuten

### Lernziele

- Entity, Value Object und Aggregate unterscheiden können
- Aggregate-Regeln verstehen und korrekt anwenden
- Domain Services, Domain Events, Factories und Repositories einordnen
- Die „Primitive Obsession" als Anti-Pattern erkennen
- Alle Building Blocks in Java 17+ idiomatisch umsetzen

---

## Verbindung zum Event Storming

### Die gelben Sticky Notes werden jetzt zu Code

| Event-Storming-Element | → | Building Block |
|------------------------|---|---------------|
| 🟨 Aggregate (gelb) | → | Aggregate Root (Klasse mit Invarianten) |
| 🟧 Domain Event (orange) | → | Domain Event (Java Record) |
| 🟦 Command (blau) | → | Command-Objekt (Java Record) |
| 🟪 Policy (lila) | → | Event Handler / Domain Service |
| Akteur | → | Auslöser eines Use Case |

> Die Ergebnisse aus Lab 02 (Event Storming) werden jetzt
> zu lauffähigem Java-Code.

---

## Überblick – Tactical DDD Building Blocks

| Baustein | Zweck | Java-Umsetzung |
|----------|-------|----------------|
| **Entity** | Objekt mit Identität und Lebenszyklus | Klasse mit ID, equals/hashCode by ID |
| **Value Object** | Unveränderlicher Wert ohne Identität | Java `record` mit Validierung |
| **Aggregate** | Konsistenzgrenze mit Root-Entity | Klasse mit Invarianten + Event-Liste |
| **Domain Service** | Zustandslose domänenübergreifende Logik | Klasse ohne State |
| **Domain Event** | Fachliches Ereignis (Vergangenheitsform) | Java `record` (immutabel) |
| **Factory** | Komplexe Erzeugungslogik | Statische Methode oder eigene Klasse |
| **Repository** | Collection-ähnlicher Zugriff auf Aggregates | Java Interface (Port) |

---

## Entity – Definition

- Hat eine **eindeutige Identität** (ID), die über den Lebenszyklus bestehen bleibt
- Gleichheit wird über die **ID** bestimmt, nicht über Attribute
- Hat einen **Lebenszyklus**: Erstellung → Änderung → ggf. Archivierung
- Enthält **Geschäftslogik** als Methoden

### Beispiele im Immobilien-CRM

- `BrokerageProcess` (identifiziert durch `UUID`)
- `Viewing` (identifiziert durch `UUID`, lebt innerhalb des Aggregats)
- `Contact` (identifiziert durch `ContactId`)

---

## Entity – Codebeispiel

```java
public class Contact {

    private final UUID id;
    private String firstName;
    private String lastName;
    private Emailadresse email;
    private ContactType type; // OWNER, PROSPECT

    public Contact(UUID id, String firstName,
                   String lastName, ContactType type) {
        this.id = Objects.requireNonNull(id);
        this.firstName = Objects.requireNonNull(firstName);
        this.lastName = Objects.requireNonNull(lastName);
        this.type = Objects.requireNonNull(type);
    }
}
```

- Konstruktor **validiert** alle Pflichtfelder
- Objekt ist **nie in einem ungültigen Zustand**

---

## Entity – Gleichheit über ID

```java
public class Contact {
    // ... fields and constructor

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Contact other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
```

- Zwei Kontakte mit **gleicher ID** sind dasselbe Objekt
- Auch wenn Vorname oder E-Mail sich geändert haben
- `instanceof` Pattern Matching (Java 16+)

---

## Value Object – Definition

- Hat **keine eigene Identität**
- Gleichheit durch **Wertevergleich** aller Attribute
- **Unveränderlich** (immutable) – Änderung erzeugt neues Objekt
- Beschreibt eine **Eigenschaft**, ein **Maß** oder ein **Konzept**
- In Java 17+: ideal als **Record** umsetzbar

### Beispiele im Immobilien-CRM

| Value Object | Beschreibt |
|-------------|-----------|
| `Address` | Straße, PLZ, Ort |
| `AskingPrice` | Betrag + Währung |
| `Commission` | Prozentsatz |
| `Emailadresse` | Validierte E-Mail |

> **Auch IDs sind Value Objects!** `ContactId`, `ProcessId` etc.

---

## Value Object als Java Record

```java
public record Address(String street, String postalCode, String city) {

    // Compact constructor: validation
    public Address {
        Objects.requireNonNull(street, "Street must not be null");
        Objects.requireNonNull(postalCode, "Postal code must not be null");
        Objects.requireNonNull(city, "City must not be null");
        if (!postalCode.matches("\\d{5}")) {
            throw new IllegalArgumentException("Invalid postal code: " + postalCode);
        }
    }
}
```

- Record = automatisch immutable + `equals()`/`hashCode()` by value
- **Compact Constructor** für Validierung – kein new-Keyword im Body
- Keine Getter-Boilerplate: `address.postalCode()` statt `address.getPostalCode()`

---

## Value Objects mit Geschäftslogik

```java
public record AskingPrice(BigDecimal amount, String currency) {

    public AskingPrice {
        Objects.requireNonNull(amount);
        Objects.requireNonNull(currency);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                "Price must be positive: " + amount);
        }
    }

    public boolean isAboveMarketPrice(AskingPrice marketPrice) {
        return amount.compareTo(marketPrice.amount()) > 0;
    }

    public AskingPrice reduce(BigDecimal percent) {
        var factor = BigDecimal.ONE.subtract(
            percent.divide(BigDecimal.valueOf(100)));
        return new AskingPrice(amount.multiply(factor), currency);
    }
}
```

- Geschäftslogik **im Value Object** selbst
- `reduce()` liefert **neues** Objekt (immutabel!)

---

## Primitive Obsession – Das Anti-Pattern

### ❌ Primitives statt Value Objects

```java
public class BrokerageProcess {
    private String ownerId;              // What format?
    private double purchasePrice;        // What currency? Cents?
    private double commission;           // Percent or absolute?
    private String street, postalCode, city; // Always needed together
}
```

### ✅ Value Objects statt Primitives

```java
public class BrokerageProcess {
    private UUID ownerId;
    private AskingPrice askingPrice;
    private Commission commission;
    private Address address;
}
```

- Value Objects **dokumentieren** die Domäne
- **Validierung** findet im Konstruktor statt, nicht überall verstreut
- **Typsicherheit**: Man kann keine `Commission` versehentlich als `AskingPrice` übergeben

---

## Entity vs. Value Object – Entscheidungshilfe

| Kriterium | Entity | Value Object |
|-----------|--------|-------------|
| **Identität** | Ja (ID) | Nein |
| **Gleichheit** | Per ID | Per Wert |
| **Veränderlich** | Ja (kontrolliert) | Nein (immutable) |
| **Lebenszyklus** | Ja | Nein – wird ersetzt |
| **Java-Umsetzung** | Klasse | Record |
| **Beispiel** | `Viewing` | `Address` |

> **Faustregel:** Im Zweifel → **Value Object** bevorzugen!
> Nur wenn ein Objekt über die Zeit **getrackt** werden muss → Entity.

---

## Aggregate – Definition

- Ein **Cluster von Entities und Value Objects** mit einer Root-Entity
- Bildet eine **Konsistenzgrenze** (transactional boundary)
- Zugriff von außen **nur über die Aggregate Root**
- Invarianten werden innerhalb des Aggregates **sofort garantiert**

### Die Aggregate Root

- Die **einzige Entity**, über die von außen zugegriffen wird
- Kontrolliert alle Änderungen an inneren Objekten
- Hat eine **global eindeutige ID**
- Stellt sicher: Das Aggregat ist **immer in einem gültigen Zustand**

---

## Aggregate-Regeln – Die 7 Gebote

1. **Nur die Root** ist von außen referenzierbar
2. Innere Objekte dürfen **nicht direkt** weitergegeben werden (→ Kopie oder Unmodifiable)
3. **Cross-Aggregate-Referenz** nur per ID, nie per Objektreferenz
4. Innerhalb eines Aggregats: **Immediate Consistency**
5. Zwischen Aggregates: **Eventual Consistency** (über Domain Events)
6. Aggregates sollten **klein** gehalten werden
7. **Ein Repository pro Aggregate** – nie für innere Entities

> **Regel 3** ist besonders wichtig: Kein `private Contact owner`,
> sondern `private UUID ownerId`. Das entkoppelt Aggregates!

---

## Aggregate Sizing – Wie groß ist richtig?

### Klein anfangen!

![Aggregate Sizing](images/aggregate-sizing.drawio.png)

### Faustregel

- Entities, die **nur zusammen mit der Root geändert** werden → ins Aggregate
- Entities, die **eigenständig geladen** werden müssen → eigenes Aggregate
- Wenn in Zweifel → **kleineres Aggregate**, verbunden per ID

---

## Aggregate – Codebeispiel

![Aggregate Vermittlungsvorgang](images/aggregate-vermittlungsvorgang.drawio.png)

---

## Aggregate Root – Vermittlungsvorgang

```java
public class BrokerageProcess {

    private final UUID id;
    private final UUID propertyId; // Reference by ID!
    private Address address;
    private AskingPrice askingPrice;
    private ProcessStatus status;
    private final List<Viewing> viewings = new ArrayList<>();
    private final List<Offer> offers = new ArrayList<>();
    private final List<Object> domainEvents = new ArrayList<>();

    public UUID addViewing(
            String prospect, LocalDateTime timestamp) {
        var viewing = new Viewing(
            UUID.randomUUID(), prospect, timestamp);
        this.viewings.add(viewing);
        this.status = ProcessStatus.VIEWING;
        return viewing.getId();
    }
}
```

---

## Invarianten schützen

```java
public class BrokerageProcess {

    // ... fields

    public void setStatusToNotaryAppointment() {
        boolean hasAcceptedOffer = offers.stream()
            .anyMatch(Offer::isAccepted);

        if (!hasAcceptedOffer) {
            throw new IllegalStateException(
                "Notary appointment requires an accepted offer");
        }
        this.status = ProcessStatus.NOTARY_APPOINTMENT;
        domainEvents.add(new NotaryAppointmentScheduled(this.id));
    }

    public List<Viewing> getViewings() {
        return Collections.unmodifiableList(viewings);
    }
}
```

- **Invariante**: Kein Notartermin ohne angenommenes Angebot
- **Unmodifiable List**: Außenstehende können die Liste nicht manipulieren
- **Domain Event** wird gesammelt, nicht sofort verschickt

---

## Domain Event Collection Pattern

### Wie Events gesammelt und dispatcht werden

```java
public class BrokerageProcess {

    private final transient List<Object> domainEvents = new ArrayList<>();

    // Called by business methods
    protected void registerEvent(Object event) {
        this.domainEvents.add(event);
    }

    public List<Object> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
}
```

### Dispatch im Application Service (→ Modul 08)

```
Application Service:
  1. process = repo.findById(id)
  2. process.acceptOffer(offerId)  ← Event registered
  3. repo.save(process)
  4. process.getDomainEvents().forEach(publisher::publish)
  5. process.clearDomainEvents()
```

---

## Domain Service – Definition

- **Zustandslos** (stateless) – kein eigener Zustand
- Kapselt Logik, die **keiner einzelnen Entity** zugeordnet werden kann
- Operiert oft **über mehrere Aggregates** hinweg
- Liegt in der **Domain-Schicht** (kein Application Service!)

### Abgrenzung

| | Domain Service | Application Service |
|---|---------------|-------------------|
| **Schicht** | Domain | Application |
| **Enthält** | Geschäftslogik | Orchestrierung |
| **Zustand** | Stateless | Stateless |
| **Spring** | Kein Spring nötig | `@Service`, `@Transactional` |
| **Beispiel** | Provisionsberechnung | ScheduleViewingUseCase |

---

## Domain Service – Codebeispiel

```java
public class CommissionCalculator {

    public Commission calculate(AskingPrice price,
                                BigDecimal commissionRate,
                                SplitModel model) {
        var amount = price.amount()
            .multiply(commissionRate)
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        return switch (model) {
            case BUYER_PAYS_ALL ->
                new Commission(amount, BigDecimal.ZERO);
            case FIFTY_FIFTY ->
                new Commission(amount.divide(BigDecimal.TWO), amount.divide(BigDecimal.TWO));
            case SELLER_PAYS_ALL ->
                new Commission(BigDecimal.ZERO, amount);
        };
    }
}
```

- **Kein Spring**, kein State – reines Java
- Testbar mit **plain JUnit** ohne Kontext

---

## Domain Event – Definition

- Beschreibt **etwas, das passiert ist** – immer in der Vergangenheitsform
- **Unveränderlich** (immutable) – einmal eingetreten, nicht mehr änderbar
- Enthält alle relevanten Daten des Ereignisses
- Ermöglicht **lose Kopplung** zwischen Aggregates und BCs

### Namenskonvention

```
[Aggregate][WhatHappened]
```

Beispiele: `ViewingCompleted`, `OfferAccepted`,
`BrokerageCompleted`

---

## Domain Event – Codebeispiel

```java
public record ViewingCompleted(
    UUID brokerageProcessId,
    UUID viewingId,
    LocalDateTime timestamp
) {
    public ViewingCompleted {
        Objects.requireNonNull(brokerageProcessId);
        Objects.requireNonNull(viewingId);
        Objects.requireNonNull(timestamp);
    }
}
```

- **Record** = automatisch immutable, equals by value, toString
- Compact Constructor für **Null-Checks**
- Enthält die **IDs**, nicht die Objekte (lose Kopplung)
- Kein Timestamp-Feld mit `Instant.now()` im Record nötig –
  das Dispatching übernimmt die Infrastruktur

---

## Factory – Erzeugung komplexer Objekte

### Als statische Factory-Methode auf dem Aggregate Root

```java
public class BrokerageProcess {

    // Private constructor
    private BrokerageProcess(UUID id, UUID propertyId,
            Address address, AskingPrice price, Commission commission) {
        this.id = id;
        this.propertyId = propertyId;
        this.address = address;
        this.askingPrice = price;
        this.commission = commission;
        this.status = ProcessStatus.NEW;
    }

    public static BrokerageProcess create(
            UUID propertyId, Address address,
            AskingPrice price, Commission commission) {
        var process = new BrokerageProcess(
            UUID.randomUUID(), propertyId, address, price, commission);
        process.registerEvent(new BrokerageStarted(process.id));
        return process;
    }
}
```

- Privater Konstruktor → Erstellung **nur** über Factory-Methode
- Event wird **direkt bei Erstellung** registriert
- Objekt ist **sofort in einem gültigen Zustand**

---

## Repository – Definition

- Bietet eine **collection-ähnliche Schnittstelle** für Aggregates
- Abstrahiert die Persistenz – die Domäne kennt **keine Datenbank**
- Definiert als **Interface** in der Domain-Schicht (Port)
- Implementierung in der Infrastructure-Schicht (Adapter)

### Repository-Interface

```java
// Domain layer: pure Java, no Spring!
public interface BrokerageProcessRepository {

    Optional<BrokerageProcess> findById(UUID id);

    void save(BrokerageProcess process);

    void deleteById(UUID id);
}
```

- Kein `JpaRepository`, keine Spring-Abhängigkeit!
- Spricht die **Ubiquitous Language**: `save`, `findById`
- Rückgabetyp: **Domain-Objekt**, nicht JPA-Entity

---

## ID-Strategien

### Wie generiert man Aggregate-IDs?

| Strategie | Vorteile | Nachteile |
|-----------|----------|-----------|
| **UUID.randomUUID()** | Einfach, keine DB nötig, verteilt | Nicht sortierbar, 36 Chars |
| **UUIDv7** (zeitbasiert) | Sortierbar + einzigartig | Java-Library nötig |
| **DB-Sequence** | Kompakt, sortierbar | Kopplung an DB |
| **Fachliche ID** | Lesbar (`IMM-2024-0042`) | Eindeutigkeit schwerer sicherbar |

### Empfehlung für diesen Workshop

```java
UUID id = UUID.randomUUID(); // Simple, independent, good enough
```

> ID wird im **Domain Layer** erzeugt (Factory-Methode),
> nicht von der Datenbank vergeben.

---

## Zusammenspiel der Building Blocks

![Zusammenspiel der Building Blocks](images/building-blocks-zusammenspiel.drawio.png)

---

## 🎯 Hands-on: Lab 04

### Building Blocks im Immobilien-CRM implementieren

- **Value Objects** als Java Records: `Address`, `AskingPrice`, `Commission`
- **Domain Events** als Records: `ViewingCompleted`, `OfferAccepted`
- **Entities**: `Viewing`, `Offer` (innerhalb des Aggregats)
- **Aggregate Root**: `BrokerageProcess` mit Geschäftslogik + Invarianten
- **Repository Interface**: `BrokerageProcessRepository` (reines Java)

> **Dauer:** ca. 90 Minuten
> Details und Aufgabenstellung im **Lab 04**

---

## 💬 Diskussion

> Was wäre ein Value Object in eurer Domäne?

- Welche Konzepte haben **keine eigene Identität**?
- Wo verwendet ihr **primitive Typen** (String, int, double),
  die eigentlich Value Objects sein sollten?
- Welche **Validierungsregeln** stecken heute in Services,
  die in Value Objects wandern könnten?
- Wo liegen eure **Aggregate-Grenzen** – was gehört zusammen?

---

## Zusammenfassung

- **Entity** = Identität + Lebenszyklus, Gleichheit per ID
- **Value Object** = immutabel, Gleichheit per Wert → Java Record
- **Aggregate** = Konsistenzgrenze, Zugriff nur über Root, klein halten!
- **Domain Service** = zustandslose Geschäftslogik (kein Spring nötig)
- **Domain Event** = was passiert ist, immutabel, Record
- **Factory** = garantiert gültigen Initialzustand
- **Repository** = Java Interface in der Domain-Schicht (Port)
- **Primitive Obsession** vermeiden → Value Objects nutzen!

> Im nächsten Modul überführen wir diese Building Blocks
> in eine **Clean Architecture** (Modul 06).
