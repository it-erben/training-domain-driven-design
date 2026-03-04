---
marp: true
theme: default
paginate: true
header: "DDD & Clean Architecture mit Spring Boot 3"
footer: "© 2026 – Workshop S2090"
style: |
  section {
    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  }
  h1 {
    color: #2d6a4f;
  }
  h2 {
    color: #40916c;
  }
  code {
    background-color: #f0f0f0;
    border-radius: 4px;
    padding: 2px 6px;
  }
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

- `Vermittlungsvorgang` (identifiziert durch `UUID`)
- `Besichtigung` (identifiziert durch `UUID`, lebt innerhalb des Aggregats)
- `Kontakt` (identifiziert durch `KontaktId`)

---

## Entity – Codebeispiel

```java
public class Kontakt {

    private final UUID id;
    private String vorname;
    private String nachname;
    private Emailadresse email;
    private Kontaktart art; // EIGENTUEMER, INTERESSENT

    public Kontakt(UUID id, String vorname,
                   String nachname, Kontaktart art) {
        this.id = Objects.requireNonNull(id);
        this.vorname = Objects.requireNonNull(vorname);
        this.nachname = Objects.requireNonNull(nachname);
        this.art = Objects.requireNonNull(art);
    }
}
```

- Konstruktor **validiert** alle Pflichtfelder
- Objekt ist **nie in einem ungültigen Zustand**

---

## Entity – Gleichheit über ID

```java
public class Kontakt {
    // ... Felder und Konstruktor

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Kontakt other)) return false;
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
| `Adresse` | Straße, PLZ, Ort |
| `Preisvorstellung` | Betrag + Währung |
| `Provision` | Prozentsatz |
| `Emailadresse` | Validierte E-Mail |

> **Auch IDs sind Value Objects!** `KontaktId`, `VorgangId` etc.

---

## Value Object als Java Record

```java
public record Adresse(String strasse, String plz, String ort) {

    // Compact Constructor: Validierung
    public Adresse {
        Objects.requireNonNull(strasse, "Straße darf nicht null sein");
        Objects.requireNonNull(plz, "PLZ darf nicht null sein");
        Objects.requireNonNull(ort, "Ort darf nicht null sein");
        if (!plz.matches("\\d{5}")) {
            throw new IllegalArgumentException("PLZ ungültig: " + plz);
        }
    }
}
```

- Record = automatisch immutable + `equals()`/`hashCode()` by value
- **Compact Constructor** für Validierung – kein new-Keyword im Body
- Keine Getter-Boilerplate: `adresse.plz()` statt `adresse.getPlz()`

---

## Value Objects mit Geschäftslogik

```java
public record Preisvorstellung(BigDecimal betrag, String währung) {

    public Preisvorstellung {
        Objects.requireNonNull(betrag);
        Objects.requireNonNull(währung);
        if (betrag.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                "Preis muss positiv sein: " + betrag);
        }
    }

    public boolean istÜberMarktpreis(Preisvorstellung marktpreis) {
        return betrag.compareTo(marktpreis.betrag()) > 0;
    }

    public Preisvorstellung reduzieren(BigDecimal prozent) {
        var faktor = BigDecimal.ONE.subtract(
            prozent.divide(BigDecimal.valueOf(100)));
        return new Preisvorstellung(betrag.multiply(faktor), währung);
    }
}
```

- Geschäftslogik **im Value Object** selbst
- `reduzieren()` liefert **neues** Objekt (immutabel!)

---

## Primitive Obsession – Das Anti-Pattern

### ❌ Primitives statt Value Objects

```java
public class Vermittlungsvorgang {
    private String eigentümerId;     // Welches Format?
    private double kaufpreis;          // Welche Währung? Cent?
    private double provision;          // Prozent oder absolut?
    private String strasse, plz, ort;  // Immer zusammen nötig
}
```

### ✅ Value Objects statt Primitives

```java
public class Vermittlungsvorgang {
    private UUID eigentümerId;
    private Preisvorstellung preisvorstellung;
    private Provision provision;
    private Adresse adresse;
}
```

- Value Objects **dokumentieren** die Domäne
- **Validierung** findet im Konstruktor statt, nicht überall verstreut
- **Typsicherheit**: Man kann keine `Provision` versehentlich als `Preisvorstellung` übergeben

---

## Entity vs. Value Object – Entscheidungshilfe

| Kriterium | Entity | Value Object |
|-----------|--------|-------------|
| **Identität** | Ja (ID) | Nein |
| **Gleichheit** | Per ID | Per Wert |
| **Veränderlich** | Ja (kontrolliert) | Nein (immutable) |
| **Lebenszyklus** | Ja | Nein – wird ersetzt |
| **Java-Umsetzung** | Klasse | Record |
| **Beispiel** | `Besichtigung` | `Adresse` |

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

> **Regel 3** ist besonders wichtig: Kein `private Kontakt eigentümer`,
> sondern `private UUID eigentümerId`. Das entkoppelt Aggregates!

---

## Aggregate Sizing – Wie groß ist richtig?

### Klein anfangen!

```
❌ Zu groß:                    ✅ Richtig geschnitten:
┌─────────────────────┐       ┌─────────────────┐
│ Vermittlungsvorgang │       │ Vermittlungs-   │
│ ├── Immobilie       │       │ vorgang (Root)  │
│ ├── Eigentümer      │       │ ├── Besichtigung│
│ ├── Interessenten[] │       │ ├── Angebot     │
│ ├── Besichtigungen[]│       │ └── (Value Obj.)│
│ ├── Angebote[]      │       └─────────────────┘
│ ├── Exposé          │             │ ID-Ref.
│ └── Maklervertrag   │             ▼
└─────────────────────┘       ┌──────────────┐
   Lock-Contention!           │ Immobilie    │  ← eigenes Aggregate
                              └──────────────┘
```

### Faustregel

- Entities, die **nur zusammen mit der Root geändert** werden → ins Aggregate
- Entities, die **eigenständig geladen** werden müssen → eigenes Aggregate
- Wenn in Zweifel → **kleineres Aggregate**, verbunden per ID

---

## Aggregate – Codebeispiel

![Aggregate Vermittlungsvorgang](../diagrams/aggregate-vermittlungsvorgang.drawio.png)

---

## Aggregate Root – Vermittlungsvorgang

```java
public class Vermittlungsvorgang {

    private final UUID id;
    private final UUID immobilieId; // Referenz per ID!
    private Adresse adresse;
    private Preisvorstellung preisvorstellung;
    private VermittlungsvorgangStatus status;
    private final List<Besichtigung> besichtigungen = new ArrayList<>();
    private final List<Angebot> angebote = new ArrayList<>();
    private final List<Object> domainEvents = new ArrayList<>();

    public UUID besichtigungHinzufügen(
            String interessent, LocalDateTime zeitpunkt) {
        var besichtigung = new Besichtigung(
            UUID.randomUUID(), interessent, zeitpunkt);
        this.besichtigungen.add(besichtigung);
        this.status = VermittlungsvorgangStatus.BESICHTIGUNG;
        return besichtigung.getId();
    }
}
```

---

## Invarianten schützen

```java
public class Vermittlungsvorgang {

    // ... Felder

    public void statusAufNotarterminSetzen() {
        boolean hatAngenommenesAngebot = angebote.stream()
            .anyMatch(Angebot::isAngenommen);

        if (!hatAngenommenesAngebot) {
            throw new IllegalStateException(
                "Notartermin nur mit angenommenem Angebot möglich");
        }
        this.status = VermittlungsvorgangStatus.NOTARTERMIN;
        domainEvents.add(new NotarterminVereinbart(this.id));
    }

    public List<Besichtigung> getBesichtigungen() {
        return Collections.unmodifiableList(besichtigungen);
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
public class Vermittlungsvorgang {

    private final transient List<Object> domainEvents = new ArrayList<>();

    // Wird von Geschäftsmethoden aufgerufen
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
  1. vorgang = repo.findById(id)
  2. vorgang.angebotAnnehmen(angebotId)  ← Event registriert
  3. repo.save(vorgang)
  4. vorgang.getDomainEvents().forEach(publisher::publish)
  5. vorgang.clearDomainEvents()
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
| **Beispiel** | Provisionsberechnung | BesichtigungAnlegenUseCase |

---

## Domain Service – Codebeispiel

```java
public class Provisionsrechner {

    public Provision berechne(Preisvorstellung preis,
                              BigDecimal provisionssatz,
                              Aufteilungsmodell modell) {
        var betrag = preis.betrag()
            .multiply(provisionssatz)
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        return switch (modell) {
            case KAEUFER_ZAHLT_ALLES ->
                new Provision(betrag, BigDecimal.ZERO);
            case HALBE_HALBE ->
                new Provision(betrag.divide(BigDecimal.TWO), betrag.divide(BigDecimal.TWO));
            case VERKAEUFER_ZAHLT_ALLES ->
                new Provision(BigDecimal.ZERO, betrag);
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
[Aggregate][WasPassiertIst]
```

Beispiele: `BesichtigungDurchgeführt`, `AngebotAngenommen`,
`VermittlungAbgeschlossen`

---

## Domain Event – Codebeispiel

```java
public record BesichtigungDurchgeführt(
    UUID vermittlungsvorgangId,
    UUID besichtigungId,
    LocalDateTime zeitpunkt
) {
    public BesichtigungDurchgeführt {
        Objects.requireNonNull(vermittlungsvorgangId);
        Objects.requireNonNull(besichtigungId);
        Objects.requireNonNull(zeitpunkt);
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
public class Vermittlungsvorgang {

    // Privater Konstruktor
    private Vermittlungsvorgang(UUID id, UUID immobilieId,
            Adresse adresse, Preisvorstellung preis, Provision provision) {
        this.id = id;
        this.immobilieId = immobilieId;
        this.adresse = adresse;
        this.preisvorstellung = preis;
        this.provision = provision;
        this.status = VermittlungsvorgangStatus.NEU;
    }

    public static Vermittlungsvorgang erstellen(
            UUID immobilieId, Adresse adresse,
            Preisvorstellung preis, Provision provision) {
        var vorgang = new Vermittlungsvorgang(
            UUID.randomUUID(), immobilieId, adresse, preis, provision);
        vorgang.registerEvent(new VermittlungGestartet(vorgang.id));
        return vorgang;
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
// Domain-Schicht: reines Java, kein Spring!
public interface VermittlungsvorgangRepository {

    Optional<Vermittlungsvorgang> findById(UUID id);

    void save(Vermittlungsvorgang vorgang);

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
UUID id = UUID.randomUUID(); // Einfach, unabhängig, gut genug
```

> ID wird im **Domain Layer** erzeugt (Factory-Methode),
> nicht von der Datenbank vergeben.

---

## Zusammenspiel der Building Blocks

```
                    ┌────────────────────────────┐
                    │   Aggregate Root            │
                    │   (Vermittlungsvorgang)     │
                    │                             │
Factory ──────────► │   ┌───────────┐  ┌───────┐ │ ──────► Domain Events
(erstellen)         │   │ Entity    │  │ Value │ │        (Records)
                    │   │(Besichtig)│  │Object │ │
                    │   └───────────┘  │(Adress)│ │
                    │                  └───────┘ │
                    └──────────────┬──────────────┘
                                   │
                    ┌──────────────┴──────────────┐
                    │   Repository Interface       │
                    │   (findById, save, delete)   │
                    └──────────────────────────────┘
                              Domain Service
                          (Provisionsrechner)
```

---

## 🎯 Hands-on: Lab 04

### Building Blocks im Immobilien-CRM implementieren

- **Value Objects** als Java Records: `Adresse`, `Preisvorstellung`, `Provision`
- **Domain Events** als Records: `BesichtigungDurchgeführt`, `AngebotAngenommen`
- **Entities**: `Besichtigung`, `Angebot` (innerhalb des Aggregats)
- **Aggregate Root**: `Vermittlungsvorgang` mit Geschäftslogik + Invarianten
- **Repository Interface**: `VermittlungsvorgangRepository` (reines Java)

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
