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

# Modul 06 – Clean Architecture

## Von Hexagonal zu Clean Architecture

**Geschätzte Dauer:** ca. 75 Minuten

### Lernziele

- Die historische Entwicklung von Ports & Adapters bis Clean Architecture nachvollziehen
- Die Dependency Rule verstehen und erklären können
- Dependency Inversion als Schlüsselprinzip anwenden
- Die vier Schichten (Ringe) benennen und zuordnen
- Einschätzen, wann ein pragmatischer Kompromiss sinnvoll ist

---

## Historische Entwicklung

| Jahr | Architektur | Urheber |
|------|------------|---------|
| 2005 | **Ports & Adapters** (Hexagonal) | Alistair Cockburn |
| 2008 | **Onion Architecture** | Jeffrey Palermo |
| 2012 | **Clean Architecture** | Robert C. Martin |

- Alle drei verfolgen dasselbe Ziel: **Domäne im Zentrum**, Infrastruktur am Rand
- Clean Architecture ist eine **Synthese** der vorherigen Ansätze
- Der gemeinsame Kern: die **Dependency Rule**

> Drei Namen, ein Prinzip — die Dependency Rule.

---

## Ports & Adapters (Hexagonal Architecture)

![Hexagonale Architektur](../diagrams/hexagonale-architektur.drawio.png)

---

## Ports & Adapters – Konzept

### Driving / Inbound (links)

- **Ports** definieren, was die Anwendung **kann** (Use-Case-Interfaces)
- **Adapter** rufen diese Ports auf (REST-Controller, CLI, UI)
- Richtung: Außenwelt → Anwendung

### Driven / Outbound (rechts)

- **Ports** definieren, was die Anwendung **braucht** (Repository-Interfaces)
- **Adapter** implementieren diese Ports (JPA, Messaging, externe APIs)
- Richtung: Anwendung → Außenwelt

### Kernidee

- Kein Unterschied zwischen „links" und „rechts" — beides sind **Adapter**
- Die Anwendung kennt nur ihre eigenen **Ports**, nicht die Adapter

---

## Onion Architecture

### Jeffrey Palermo, 2008

- Stellt die Schichten als **konzentrische Ringe** dar
- Abhängigkeiten zeigen **immer nach innen**

```
┌─────────────────────────────────────────────────────┐
│  Infrastructure (UI, DB, externe Systeme)           │
│  ┌─────────────────────────────────────────────┐    │
│  │  Application Services                       │    │
│  │  ┌─────────────────────────────────────┐    │    │
│  │  │  Domain Services                    │    │    │
│  │  │  ┌─────────────────────────────┐    │    │    │
│  │  │  │  Domain Model (Kern)        │    │    │    │
│  │  │  └─────────────────────────────┘    │    │    │
│  │  └─────────────────────────────────────┘    │    │
│  └─────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────┘
```

- Der Kern (Domain Model) hat **keine** Abhängigkeiten nach außen

---

## Clean Architecture – Robert C. Martin

- Publiziert 2012 (Blog), 2017 (Buch "Clean Architecture")
- Vereint Hexagonal, Onion und weitere Ansätze
- Definiert **vier Ringe** mit klaren Verantwortlichkeiten

![Clean Architecture](../diagrams/clean-architecture-ringe.drawio.png)

---

## Die Dependency Rule

> *"Source code dependencies must point only inward,
> toward higher-level policies."*
> — Robert C. Martin

- Nichts in einem inneren Ring darf etwas aus einem äußeren Ring kennen
- Kein Name, kein Typ, keine Klasse, kein Interface aus dem äußeren Ring
- **Daten** fließen in beide Richtungen — **Abhängigkeiten** nur nach innen

![Dependency Rule](../diagrams/dependency-rule.drawio.png)

---

## Die vier Ringe im Detail

| Ring | Verantwortung | Enthält | Spring? |
|------|--------------|---------|---------|
| **1. Entities** (innen) | Geschäftsregeln des Unternehmens | Aggregates, Value Objects, Domain Events | Nein |
| **2. Use Cases** | Anwendungsspezifische Abläufe | Application Services, Commands, Ports | Minimal (`@Service`) |
| **3. Interface Adapters** | Übersetzen zwischen innen und außen | Controller, DTOs, JPA Entities, Mapper | Ja |
| **4. Frameworks & Drivers** (außen) | Technische Infrastruktur | Spring Boot, JPA, H2, Jackson | Ja |

> Je weiter innen, desto **stabiler** und **langlebiger** ist der Code.
> Je weiter außen, desto **austauschbarer**.

---

## Dependency Inversion Principle (DIP)

### Das Problem

- Use Case muss Daten persistieren
- Darf aber die Datenbank-Implementierung **nicht kennen**

### Die Lösung

```
┌───────────────────────────────────────────────────────┐
│  Domain (innerer Ring)                                │
│                                                       │
│  interface VermittlungsvorgangRepository {             │
│      Optional<Vermittlungsvorgang> findById(UUID id); │
│      void save(Vermittlungsvorgang vorgang);          │
│  }                                                    │
└────────────────────────┬──────────────────────────────┘
                         │ implements
┌────────────────────────┴──────────────────────────────┐
│  Infrastructure (äußerer Ring)                        │
│                                                       │
│  @Component                                           │
│  class VermittlungsvorgangRepositoryAdapter            │
│      implements VermittlungsvorgangRepository {        │
│      // ... JPA-Implementierung                       │
│  }                                                    │
└───────────────────────────────────────────────────────┘
```

- Der **innere Ring** definiert das Interface (Port)
- Der **äußere Ring** liefert die Implementierung (Adapter)
- Abhängigkeit zeigt nach **innen**, nicht nach außen

---

## Datenfluss vs. Abhängigkeitsrichtung

### Das häufigste Missverständnis

```
Datenfluss (Request):
[Controller] ──► [Use Case] ──► [Repository Port] ──► [DB]
     │                │                │
     ▼                ▼                ▼
  Adapter          Application        Domain          Infrastructure

Abhängigkeiten:
[Controller] ──► [Use Case] ◄── [Repository Adapter]
                      │
                      ▼
               [Domain Model]
```

- Daten fließen **durch alle Schichten** (Request rein, Response raus)
- Abhängigkeiten zeigen **nur nach innen** (Controller → Use Case ← Adapter)
- Der Repository-Adapter **implementiert** das Domain-Interface (Pfeil nach innen!)

---

## Before / After: Klassisch → Clean Architecture

### ❌ Klassisch: Domain hängt von JPA ab

```java
@Entity                             // ← Framework im Kern!
public class Vermittlungsvorgang {
    @Id @GeneratedValue
    private Long id;
    @OneToMany(cascade = ALL)
    private List<Besichtigung> besichtigungen;
}
```

### ✅ Clean Architecture: Domain ist rein

```java
// domain/model — kein Spring, kein JPA
public class Vermittlungsvorgang {
    private final UUID id;
    private final List<Besichtigung> besichtigungen;

    public UUID besichtigungHinzufügen(String name, LocalDateTime termin) {
        // Geschäftslogik hier, nicht im Service
    }
}
```

> Das JPA-Mapping wandert in eine **separate** Klasse in `infrastructure`.

---

## Mapping zwischen Schichten

### Der Daten-Transformations-Flow

```
HTTP-Request            Command              Domain              JPA-Entity
┌──────────┐    Map    ┌──────────┐   Use   ┌──────────┐  Map   ┌──────────┐
│ Request  │ ────────► │ Besich-  │ ──Case─►│ Vermitt- │ ────►  │ JpaVer-  │
│ DTO      │           │ tigung   │         │ lungs-   │        │ mittlung │
│ (JSON)   │           │ Anlegen  │         │ vorgang  │        │ (DB)     │
└──────────┘           │ Command  │         └──────────┘        └──────────┘
                       └──────────┘
                                                 │
                                                 ▼
┌──────────┐    Map    ┌──────────┐         ┌──────────┐
│ Response │ ◄──────── │ Result   │ ◄────── │ Domain   │
│ DTO      │           │ Record   │         │ Methode  │
│ (JSON)   │           └──────────┘         └──────────┘
└──────────┘
```

- Jede Schichtgrenze hat **eigene Datenstrukturen**
- Kein „durchreichen" von JPA-Entities bis zum Controller!
- Mapping ist **explizit** — mehr Code, aber klare Grenzen

---

## Vorteile der Clean Architecture

### Testbarkeit

| Ring | Testart | Framework nötig? |
|------|---------|-------------------|
| Domain (Entities) | Unit Tests | **Nein** – reines Java, JUnit 5 |
| Use Cases | Unit Tests + Mocks | **Nein** – Ports mocken |
| Adapters | Integration Tests | Ja – `@WebMvcTest`, `@DataJpaTest` |
| Full Stack | E2E Tests | Ja – `@SpringBootTest` |

```java
@Test
void sollte_besichtigung_anlegen() {
    // Kein Spring-Kontext nötig!
    var repo = new InMemoryVermittlungsvorgangRepository();
    repo.save(Vermittlungsvorgang.erstellen(...));
    var useCase = new BesichtigungAnlegenUseCase(repo);
    var result = useCase.anlegen(command);
    assertThat(result.besichtigungId()).isNotNull();
}
```

---

## Weitere Vorteile

- **Framework-Unabhängigkeit**: Spring Boot → Quarkus? Domain bleibt gleich
- **Datenbank-Unabhängigkeit**: JPA → MongoDB? Nur Adapter tauschen
- **UI-Unabhängigkeit**: REST → GraphQL? Nur Adapter tauschen
- **Domänenfokus**: Geschäftslogik steht im Zentrum, nicht die Technik
- **Parallele Entwicklung**: Teams können an verschiedenen Schichten arbeiten

---

## Der Pragmatismus-Trade-off: @Entity in der Domain?

### Puristischer Ansatz (unser Workshop)

- Domain-Modell **ohne** JPA-Annotationen
- Separate JPA-Entities in `infrastructure`
- Mapping zwischen Domain-Model und JPA-Entity
- **Vorteil:** Domain ist 100% rein, testbar, portabel
- **Nachteil:** Mehr Mapping-Code, Duplikation

### Pragmatischer Ansatz

- `@Entity` direkt auf dem Domain-Modell
- Spart Mapping-Code
- **Vorteil:** Weniger Boilerplate, schneller
- **Nachteil:** Domain ist an JPA gekoppelt, schwerer zu testen

> **Empfehlung:** Puristisch für Core Subdomains,
> pragmatisch für Supporting/Generic Subdomains.

---

## Clean Architecture vs. klassische Schichtarchitektur

| Aspekt | Klassisch (Layers) | Clean Architecture |
|--------|---------------------|---------------------|
| **Abhängigkeiten** | UI → Service → Repository → DB | Alle zeigen nach innen (Domain) |
| **Domain kennt** | JPA, Spring, DB-Schema | Nichts außer Java |
| **Framework-Rolle** | Durchdringt alles | Nur am Rand (Adapter) |
| **Tests** | Brauchen DB/Spring | Domain: plain JUnit |
| **DB-Wechsel** | Ändert Domain | Ändert nur Adapter |
| **Mapping** | Wenig (Entities durchgereicht) | Explizit (DTO ↔ Domain ↔ JPA) |
| **Komplexität** | Niedrig | Höher (mehr Struktur) |

---

## Was Clean Architecture NICHT ist

- Kein **Silver Bullet** – nicht für jedes Projekt geeignet
- Kein Grund für **Over-Engineering** bei einfachen CRUD-Anwendungen
- Kein Dogma – die Ringe sind **Richtlinien**, keine Gesetze
- Nicht gleichbedeutend mit **vielen Schichten und Indirektionen**

### Wann lohnt es sich?

- Komplexe Domänenlogik (Core Subdomain)
- Langlebige Systeme (> 2 Jahre Lebensdauer)
- Mehrere Teams oder Module
- Wechselnde Infrastruktur-Anforderungen

### Wann eher nicht?

- Reine CRUD-Anwendungen
- Prototypen und Proof of Concepts
- Generic Subdomains mit trivialer Logik

---

## Häufige Fehler bei der Umsetzung

| Fehler | Warum problematisch |
|--------|---------------------|
| JPA-Annotations im Domain-Model | Domain kennt Infrastruktur |
| Spring-Imports in Entities | Domain ist nicht rein |
| Use Case ruft JPA-Repo direkt auf | DIP verletzt, Port übersprungen |
| Controller enthält Geschäftslogik | Logik gehört in Domain |
| Domain-Objekte als API-Response | Innere Struktur nach außen exponiert |
| Kein Mapping zwischen Schichten | Kopplung durch durchgereichte Objekte |

> **Faustregel:** Wenn eure Domain-Klasse ein
> `import org.springframework` enthält, stimmt etwas nicht.

---

## Zusammenfassung

- **Hexagonal, Onion, Clean Architecture** — ein gemeinsamer Kern
- Die **Dependency Rule** ist das zentrale Prinzip: Abhängigkeiten nur nach innen
- Innere Ringe definieren **Interfaces** (Ports), äußere liefern **Implementierungen** (Adapter)
- **Mapping** zwischen Schichten ist explizit — mehr Code, aber klare Grenzen
- **Pragmatismus** ist erlaubt: `@Entity` in der Domain für Supporting Subdomains
- Ergebnis: testbare, wartbare, domänenfokussierte Software

> Im nächsten Modul setzen wir das in eine konkrete
> **Spring Boot Paketstruktur** um (Modul 07).

---

## 💬 Diskussion

> Wie sieht die Abhängigkeitsrichtung in euren Projekten aus?

- Zeigen eure Abhängigkeiten **nach innen** oder nach außen?
- Kennt eure Domäne Spring, JPA oder andere Frameworks?
- Könnt ihr eure Geschäftslogik **ohne Datenbank** testen?
- Wo würdet ihr **puristisch** vorgehen, wo **pragmatisch**?
- Was müsste sich ändern, um die Dependency Rule einzuhalten?
