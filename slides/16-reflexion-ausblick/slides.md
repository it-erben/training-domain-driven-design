---
marp: true
theme: default
paginate: true
header: "DDD & Clean Architecture mit Spring Boot 4"
footer: "CC BY-NC-SA 4.0, Alexander Erben"
---

# Modul 16 – Reflexion & Ausblick

## Wann DDD? Pragmatismus, Migration und nächste Schritte

**Geschätzte Dauer:** ca. 45 Minuten

### Lernziele

- Einschätzen, wann DDD den Aufwand rechtfertigt
- Core, Supporting und Generic Subdomains unterscheiden
- Pragmatische Trade-offs bewusst treffen
- Migrationsstrategie mit dem Strangler Fig Pattern kennen
- Weiterführende Themen und Literatur kennen
- Einen konkreten Aktionsplan für die eigene Praxis erstellen

---

## Wann lohnt sich DDD?

### DDD ist kein Allheilmittel

```
Entscheidungsbaum:
│
├─ Gibt es nicht-triviale Geschäftsregeln?
│  └─ Nein → CRUD reicht, kein taktisches DDD nötig
│
├─ Verschiedene Fachbegriffe in verschiedenen Abteilungen?
│  └─ Ja → Strategisches DDD (Bounded Contexts, Ubiquitous Language)
│
├─ Entwickelt sich die Domäne über Jahre weiter?
│  └─ Ja → Taktisches DDD (Aggregates, Domain Events, Clean Architecture)
│
└─ Arbeiten mehrere Teams an der Anwendung?
   └─ Ja → Context Mapping, Spring Modulith, ggf. Microservices
```

---

## Investition nach Subdomain-Kategorie

![Investition nach Subdomain](images/investition-nach-subdomain.drawio.png)

---

## Pragmatismus vs. Purismus — Entscheidungshilfe

| Aspekt | Puristisch | Pragmatisch |
|--------|-----------|-------------|
| **Domain-Modell** | Ohne JPA-Annotationen | `@Entity` direkt auf Domain |
| **Mapping** | Domain ↔ JPA-Entity | Kein Mapping nötig |
| **Wartung** | Zwei Modelle pflegen | Ein Modell pflegen |
| **Framework-Wechsel** | JPA → MongoDB: nur Adapter | Überall anfassen |
| **Testbarkeit** | Maximal (reines Java) | Sehr gut (JPA-Annotationen stören kaum) |
| **Einstiegshürde** | Höher (mehr Konzepte) | Niedriger |

### Empfehlung

- **Core Domain:** Eher puristisch — die Investition lohnt sich
- **Supporting Subdomain:** Pragmatisch — `@Entity` in der Domain ist OK
- **Generic Subdomain:** CRUD reicht — kein DDD nötig

> Wir haben im Workshop den **puristischen Weg** geübt,
> damit ihr die Trade-offs bewusst einschätzen könnt.

---

## Migration: Das Strangler Fig Pattern

### Schrittweise Migration statt Big-Bang-Rewrite

> *"The only thing a Big Rewrite guarantees is a Big Risk."* — Martin Fowler

![Strangler Fig Pattern](images/strangler-fig-pattern.drawio.png)

---

## Migration: Praktische Schritte

### 1. Bounded Context identifizieren

- Event Storming (Modul 04) auf das Legacy-System anwenden
- **Einen** BC auswählen — bevorzugt einen mit klarer Grenze

### 2. Anti-Corruption Layer aufbauen

- ACL zwischen Legacy und neuem BC (Modul 13)
- Legacy-Aufrufe durch Events oder API-Calls ersetzen

### 3. Neuen BC implementieren

- Clean Architecture Paketstruktur (Modul 08)
- Domain Model, Ports, Application Services
- Tests auf allen Ebenen (Modul 15)

### 4. Traffic umleiten

- Feature Flags oder API-Gateway-Routing
- Parallelbetrieb, bis der neue BC stabil ist

### 5. Legacy-Code entfernen

- Erst wenn der neue BC **produktiv bewährt** ist

---

## Vom Modulith zu Microservices

### Wann Microservices?

| Kriterium | Modulith reicht | Microservices sinnvoll |
|-----------|----------------|----------------------|
| **Teams** | 1–4 Teams | 5+ Teams |
| **Deployment** | Gemeinsam OK | Unabhängig nötig |
| **Skalierung** | Uniform | Unterschiedliche Last |
| **Technologie** | Einheitlich | Unterschiedliche Stacks |

### Extraktionsstrategie (Modul 12)

```
1. Modul sauber geschnitten    → Spring Modulith verify() ✅
2. Events für Kommunikation    → @Externalized vorbereitet
3. API definiert               → REST / gRPC Schnittstelle
4. Eigene Datenbank            → Shared DB auflösen
5. Eigenes Deployment          → Container, CI/CD Pipeline
```

---

## Weiterführende Themen — Ausblick

| Thema | Was? | Wann relevant? |
|-------|------|---------------|
| **Event Sourcing** | State als Folge von Events statt aktuellem Snapshot | Audit-Trail, Undo, komplexe Domäne |
| **CQRS (voll)** | Getrennte Read/Write-Modelle mit separater DB | Hohe Leselast, komplexe Abfragen |
| **Saga Pattern** | Verteilte Transaktionen über Microservices | Microservices mit Eventual Consistency |
| **Domain Storytelling** | Alternative zu Event Storming | Kleinere Gruppen, visuelle Prozessmodellierung |
| **Architecture Decision Records** | Architekturentscheidungen dokumentieren | Ab sofort — leichtgewichtig und wertvoll |

> Diese Themen bauen auf dem Workshop-Fundament auf
> und werden in Folgekursen vertieft.

---

## ADRs: Architecture Decision Records

### Eine Praxis, die ihr sofort einführen könnt

```markdown
# ADR-001: Purist Domain Model for Brokerage BC

## Status: Accepted

## Context
The Brokerage BC is our Core Domain with complex
business rules (state machine, commission calculation).

## Decision
We use a pure domain model without JPA annotations
with separate JPA entities and mappers.

## Consequences
+ Domain is framework-free and maximally testable
+ Framework change only affects Infrastructure
- More mapping code and duplication
- Higher onboarding barrier for new team members
```

> Kurz, strukturiert, versioniert im Repository.
> Template: [adr.github.io](https://adr.github.io/)

---

## Buchempfehlungen & Ressourcen

### Bücher

| Buch | Autor | Fokus |
|------|-------|-------|
| *Domain-Driven Design* (2003) | Eric Evans | Das Originalwerk, "The Blue Book" |
| *Implementing DDD* (2013) | Vaughn Vernon | Praxisnah, "The Red Book" |
| *DDD Distilled* (2016) | Vaughn Vernon | Kompakter Einstieg |
| *Get Your Hands Dirty on Clean Architecture* | Tom Hombergs | Spring Boot + Clean Architecture |
| *Building Microservices* (2. Aufl.) | Sam Newman | Für den nächsten Schritt |

### Online-Ressourcen

- Spring Modulith: [docs.spring.io/spring-modulith](https://docs.spring.io/spring-modulith/reference/)
- ArchUnit: [archunit.org/userguide](https://www.archunit.org/userguide/html/000_Index.html)
- Martin Fowler DDD: [martinfowler.com/tags/domain driven design](https://martinfowler.com/tags/domain%20driven%20design.html)

---

## Workshop-Rückblick

### Was wir gemeinsam erarbeitet haben

| Tag | Module | Labs |
|-----|--------|------|
| **Tag 1** | 01–04: Intro, Spring Boot, DDD-Einführung, Event Storming | Lab 01–03 |
| **Tag 2** | 05–07: Strategic Design, Building Blocks, Clean Architecture | Lab 04–05 |
| **Tag 3** | 08–10: Paketstruktur, Use Cases, REST Adapter | Lab 06–08 |
| **Tag 4** | 11–13: ArchUnit, Spring Modulith, Context Integration | Lab 09–11 |
| **Tag 5** | 14–16: Querschnittsthemen, Teststrategie, Reflexion | Lab 12–13 |

- **16 Module** mit Slides und Diagrammen
- **13 Labs** mit Lösungen im Immobilien-CRM
- Vom Event Storming bis zur getesteten Clean Architecture

---

## Montag-Morgen-Checkliste

### Was ihr nächste Woche konkret tun könnt

- [ ] **Ubiquitous Language** — Glossar für euren Kernbereich starten
- [ ] **Bounded Contexts** — implizite Grenzen in eurer Codebasis identifizieren
- [ ] **Ein ArchUnit-Test** — `domain` darf nicht auf `infrastructure` zugreifen
- [ ] **Ein ADR** — eine wichtige Architekturentscheidung dokumentieren
- [ ] **Einen Domain Unit Test** — ohne Spring Context, < 10 ms
- [ ] **Ein Value Object** — einen primitiven Typ durch ein VO ersetzen (z.B. `EmailAddress`)
- [ ] **Event Storming** — für euer nächstes Feature eine Session vorschlagen

> Nicht alles auf einmal — **eine Sache pro Woche** reicht.

---

## 🎯 Finale Reflexion

### Einzelarbeit (5 Minuten)

- Was war eure **wichtigste Erkenntnis** aus dem Workshop?
- Was setzt ihr **als Erstes** in eurem Projekt um?
- Welche **eine Sache** wollt ihr nächste Woche anders machen?

### Austausch in der Gruppe

- Jeder teilt **einen konkreten Vorsatz**
- Wir sammeln die Ergebnisse am Whiteboard

---

## Key Takeaways

1. **Ubiquitous Language** ist das Fundament — ohne gemeinsame Sprache kein DDD
2. **Bounded Contexts** schützen vor Modell-Vermischung
3. **Clean Architecture** macht die Domäne unabhängig von Frameworks
4. **Aggregates** sind Konsistenzgrenzen — klein halten!
5. **Domain Events** ermöglichen lose Kopplung zwischen Kontexten
6. **Testpyramide** beginnt mit schnellen Domain Unit Tests
7. **Starte pragmatisch** — Purismus kann später kommen
8. **Modulith first** — Microservices nur wenn nötig

> DDD ist kein Framework, sondern eine **Denkweise**.
> Der Mehrwert entsteht durch die **Zusammenarbeit mit den Fachexperten**.

---

## Vielen Dank!

### Kontakt & Weiterführung

- Workshop-Repository mit allen Labs, Slides und Lösungen
- Bei Fragen: jederzeit melden

### Weiterführende Workshops bei gfu

- Microservices mit Spring Cloud
- Event-Driven Architecture mit Kafka
- Kubernetes für Java-Entwickler
- Fortgeschrittenes DDD: Event Sourcing & CQRS

> *"The goal of software architecture is to minimize the human resources
> required to build and maintain the required system."*
> — Robert C. Martin
