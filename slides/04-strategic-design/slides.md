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

# Modul 04 – Strategic Design

## Bounded Contexts & Context Mapping

**Geschätzte Dauer:** ca. 75 Minuten

### Lernziele

- Bounded Contexts definieren und abgrenzen können
- Den Unterschied zwischen Subdomain und Bounded Context verstehen
- Alle Context-Map-Patterns kennen und anwenden
- Conway's Law und seine Auswirkungen auf BC-Schnitte verstehen
- Strategic Design auf das Immobilien-CRM anwenden

---

## Was ist ein Bounded Context?

- Ein **explizit abgegrenzter Bereich**, in dem ein bestimmtes Modell gilt
- Innerhalb eines BC haben Begriffe eine **eindeutige Bedeutung**
- Außerhalb kann derselbe Begriff etwas **völlig anderes** meinen

### Beispiel: Der Begriff "Immobilie"

```
┌─ BC: Objektverwaltung ──────┐   ┌─ BC: Vermarktung ────────────┐
│                              │   │                              │
│  "Immobilie" =               │   │  "Immobilie" =               │
│  Grundbuchdaten, Baujahr,    │   │  Exposé-Fotos, Headline,     │
│  Wohnfläche, Energieausweis, │   │  Zielgruppe, Portale,        │
│  Grundrissplan               │   │  Vermarktungsstatus          │
│                              │   │                              │
│  → technisch / detailliert   │   │  → marketingoptimiert        │
└──────────────────────────────┘   └──────────────────────────────┘
```

> **Eric Evans:** *"A Bounded Context delimits the applicability
> of a particular model."*

---

## Bounded Context vs. Subdomain

| | Subdomain | Bounded Context |
|---|-----------|-----------------|
| **Raum** | Problemraum | Lösungsraum |
| **Was?** | Fachlicher Bereich | Softwaregrenze |
| **Entdeckung** | Wird entdeckt / analysiert | Wird bewusst geschnitten |
| **Existenz** | Existiert unabhängig von Software | Ist ein Architektur-Artefakt |
| **Mapping** | 1 Subdomain → 1 oder N BCs | 1 BC ← 1 Subdomain (ideal) |

- Idealerweise: **1 Subdomain = 1 Bounded Context**
- In der Praxis: Legacy-Systeme erzwingen manchmal Abweichungen
- Ein BC sollte nie **mehrere Subdomains** abdecken (→ Big Ball of Mud)

---

## Bounded Contexts im Immobilien-CRM

![Bounded Contexts](../diagrams/bounded-contexts-immobilien-crm.drawio.png)

---

## Unsere sechs Bounded Contexts

| Bounded Context | Kernverantwortung | Aggregate(s) |
|-----------------|-------------------|--------------|
| **Objektverwaltung** | Immobilien-Stammdaten, Merkmale, Fotos | Immobilie, Bewertung |
| **Kontaktmanagement** | Eigentümer, Interessenten, Kontaktdaten | Kontakt |
| **Akquise / Auftrag** | Maklerverträge, Auftragserteilung | Maklerauftrag |
| **Vermarktung** | Exposés, Portale, Inserate | Exposé, Inserat |
| **Vermittlungsprozess** | Besichtigungen, Angebote, Abschluss | Vermittlungsvorgang |
| **Aktivitäten** | Termine, Telefonate, E-Mails, Aufgaben | Aktivität, Termin |

---

## Conway's Law

### Die Organisationsstruktur bestimmt die Softwarearchitektur

> *"Any organization that designs a system will produce a design whose
> structure is a copy of the organization's communication structure."*
> — Melvin Conway, 1968

### Konsequenz für BC-Schnitte

- Ein BC sollte von **einem Team** verantwortet werden
- Team-Grenzen und BC-Grenzen sollten **übereinstimmen**
- Zwei Teams, ein BC → Abstimmungsoverhead, Konflikte
- Ein Team, viele BCs → möglich bei kleinen BCs

### Inverse Conway Maneuver

Organisiere Teams **entlang der gewünschten Architektur**, nicht umgekehrt.

---

## Context Map – Überblick

- Eine **Context Map** zeigt, wie Bounded Contexts zueinander stehen
- Sie dokumentiert **Integrations-Beziehungen** und **Machtverhältnisse**
- Es geht um **Team- und Systembeziehungen**, nicht nur Technik

### Die 8 Patterns

| Pattern | Kurzbeschreibung |
|---------|-----------------|
| **Customer / Supplier** | Downstream stellt Anforderungen an Upstream |
| **Conformist** | Downstream übernimmt Upstream-Modell 1:1 |
| **Anti-Corruption Layer** | Downstream schützt sich mit Übersetzungsschicht |
| **Shared Kernel** | Geteilter Modellkern, gemeinsame Verantwortung |
| **Published Language** | Dokumentiertes, versioniertes Datenformat |
| **Open Host Service** | Offene API für viele Consumer |
| **Partnership** | Zwei Teams entwickeln gemeinsam, keine U/D-Hierarchie |
| **Separate Ways** | Bewusst keine Integration |

---

## Context Map – Immobilien-CRM

![Context Map](../diagrams/context-map-immobilien-crm.drawio.png)

---

## Pattern: Customer / Supplier (U/D)

### Downstream stellt Anforderungen an Upstream

```
  ┌──────────────────┐        ┌──────────────────┐
  │  Objektverwaltung │  U/D   │  Vermarktung     │
  │  (Upstream)       │───────►│  (Downstream)    │
  │                   │        │                   │
  │  Liefert: Lage,   │        │  Braucht: Adresse,│
  │  Fläche, Typ      │        │  Fotos, Merkmale  │
  └──────────────────┘        └──────────────────┘
```

- **Upstream** liefert Daten oder Services
- **Downstream** konsumiert und kann **Anforderungen stellen**
- Beide Teams stimmen sich aktiv ab
- **Wann?** Klare Lieferbeziehung, Downstream hat Einfluss

---

## Pattern: Conformist

### Downstream übernimmt das Upstream-Modell unverändert

- Wie Customer/Supplier, aber Downstream hat **keinen Einfluss**
- Das Downstream-Team übernimmt das Modell **1:1**
- Kein eigenes Domänenmodell für die integrierten Daten

### Wann einsetzen?

- Integration mit **externen Systemen**, die man nicht ändern kann
- Kosten einer Übersetzung übersteigen den Nutzen
- Beispiel: Übernahme des OpenImmo-XML-Standards

> **Risiko:** Das eigene Modell wird vom Upstream-Modell „infiziert".
> Alternative: ACL, wenn der Aufwand vertretbar ist.

---

## Pattern: Anti-Corruption Layer (ACL)

### Schützt das eigene Modell mit einer Übersetzungsschicht

```
  ┌──────────────┐      ┌───────────┐      ┌──────────────┐
  │ External CRM │ ───► │    ACL    │ ───► │ Kontakt-     │
  │ (Upstream)   │      │ Translator│      │ management   │
  │              │      │           │      │ (Downstream) │
  │ "Customer"   │      │ Customer  │      │ "Kontakt"    │
  │ "Account"    │      │ → Kontakt │      │ "Eigentümer" │
  └──────────────┘      └───────────┘      └──────────────┘
```

- Übersetzt eingehende Daten in die **eigene Ubiquitous Language**
- **Wann?** Integration mit Legacy-Systemen oder externen APIs
  deren Modell nicht zum eigenen passt

```java
@Component
public class ExternalCrmTranslator {
    public Kontakt translate(CrmCustomerDto dto) {
        return new Kontakt(
            KontaktId.generate(),
            dto.getFirstName(), dto.getLastName(),
            Kontaktart.from(dto.getType()));
    }
}
```

---

## Pattern: Shared Kernel

### Geteilter Modellkern zwischen zwei BCs

- Zwei BCs teilen sich einen **gemeinsamen Modellteil**
- Änderungen müssen **abgestimmt** werden – enger Kopplungsgrad
- Nur bei **engem Team-Alignment** sinnvoll

### Wann einsetzen?

- Gemeinsame Kernkonzepte, die identisch bleiben **müssen**
- Beispiel: Gemeinsame Value Objects `Adresse`, `Waehrungsbetrag`

> **Vorsicht:** Shared Kernel ist **die engste Kopplung** zwischen BCs.
> Je größer der Kernel, desto mehr Abstimmungsaufwand.
> Alternative: Published Language oder ACL.

---

## Pattern: Published Language & Open Host Service

### Published Language

- Ein **dokumentiertes, versioniertes Datenformat** für Kommunikation
- Unabhängig von der internen Modellierung beider Seiten
- JSON-Schemas, XML-Schemas, Protobuf, Avro

### Open Host Service (OHS)

- Ein BC stellt eine **offene, wohldefinierte API** bereit
- Mehrere andere BCs können darüber zugreifen
- Oft **kombiniert** mit Published Language

### Beispiel im Immobilien-CRM

- **Kontaktmanagement** bietet eine REST-API (OHS) mit
  JSON-Schema (Published Language), die von Akquise,
  Vermittlung und Aktivitäten genutzt wird

---

## Pattern: Partnership & Separate Ways

### Partnership

- Zwei Teams entwickeln **gemeinsam** ohne Upstream/Downstream-Hierarchie
- Erfolg oder Misserfolg betrifft **beide** gleichermaßen
- Erfordert enge Abstimmung und gegenseitiges Vertrauen
- **Wann?** Zwei BCs, die so eng verbunden sind, dass sie quasi co-entwickelt werden

### Separate Ways

- Bewusste Entscheidung: **keine Integration**
- Jeder BC löst das Problem **eigenständig** (evtl. mit Duplikation)
- **Wann?** Integrations-Kosten übersteigen den Nutzen
- Beispiel: Jeder BC pflegt seine eigene einfache Adress-Verwaltung,
  statt ein gemeinsames Kontaktmanagement zu integrieren

---

## Beziehungen im Immobilien-CRM

| Upstream | Downstream | Pattern | Warum? |
|----------|-----------|---------|--------|
| Objektverwaltung | Vermarktung | **Customer/Supplier** | Vermarktung braucht Immobiliendaten |
| Kontaktmanagement | Akquise | **OHS** | Viele Consumer, stabile API |
| Kontaktmanagement | Vermittlung | **OHS** | Viele Consumer, stabile API |
| Akquise | Vermittlung | **Customer/Supplier** | Vertrag löst Vermittlung aus |
| Vermittlung | Aktivitäten | **Domain Events** | Lose Kopplung, fire & forget |
| Ext. Immobilienportal | Objektverwaltung | **ACL** | Fremdes Modell übersetzen |

---

## Pattern-Auswahl – Entscheidungsbaum

```
Integrations-Entscheidung:
│
├─ Brauchen wir überhaupt Integration?
│  └─ Nein → Separate Ways
│
├─ Sind beide Teams gleichberechtigt?
│  └─ Ja → Partnership
│
├─ Habe ich Einfluss auf die Schnittstelle?
│  ├─ Ja → Customer / Supplier
│  └─ Nein ──┐
│             ├─ Ist das Upstream-Modell kompatibel?
│             │  ├─ Ja → Conformist
│             │  └─ Nein → Anti-Corruption Layer
│
├─ Hat der Upstream viele Consumer?
│  └─ Ja → Open Host Service + Published Language
│
└─ Teilen wir Kernkonzepte?
   └─ Ja, bewusst → Shared Kernel
```

---

## Wie manifestiert sich ein BC im Code?

### Vorgeschmack auf Modul 07 (Paketstruktur)

```
de.immobiliencrm/
├── vermittlung/          ← BC: Vermittlungsprozess
│   ├── domain/
│   ├── application/
│   ├── infrastructure/
│   └── adapter/
├── akquise/              ← BC: Akquise / Auftrag
│   ├── domain/
│   ├── application/
│   ├── infrastructure/
│   └── adapter/
└── kontakt/              ← BC: Kontaktmanagement
    ├── domain/
    └── ...
```

- Jeder BC ist ein **Top-Level-Package** (oder Maven-Modul)
- BCs kommunizieren **nur über definierte Schnittstellen** (Events, APIs)
- Kein direkter Import von `vermittlung.domain` in `akquise.domain`!

---

## Wie schneidet man Bounded Contexts?

### Fünf Heuristiken

1. **Ubiquitous Language** – Wo ändert sich die Bedeutung eines Begriffs?
2. **Pivot Events** – Welche Events markieren Phasenübergänge?
3. **Akteurwechsel** – Wo übernimmt eine andere Person/Rolle?
4. **Daten-Kohäsion** – Welche Daten ändern sich gemeinsam?
5. **Conway's Law** – Welches Team verantwortet welchen Bereich?

### Warnsignale für falsche Schnitte

- Ein BC hat **mehr als 10 Aggregates** → wahrscheinlich zu groß
- Zwei BCs ändern sich **immer gleichzeitig** → evtl. zusammenlegen
- Ein BC hat **keine eigene Ubiquitous Language** → kein echter BC

---

## 🎯 Hands-on: Lab 03

### Context Map für das Immobilien-CRM erstellen

- Bounded Contexts aus dem Event Storming (Lab 02) ableiten
- Beziehungen zwischen BCs bestimmen
- Passende Integration Patterns zuordnen
- Context Map visualisieren (Miro, draw.io oder Whiteboard)

> **Dauer:** ca. 60 Minuten
> Details und Aufgabenstellung im **Lab 03**

---

## 💬 Diskussion

> Wie würdet ihr die Boundaries in eurem aktuellen Projekt ziehen?

- Welche Begriffe haben in verschiedenen Bereichen **unterschiedliche Bedeutungen**?
- Wo gibt es heute schon **implizite** Bounded Contexts?
- Welche Integration Patterns setzt ihr (unbewusst) bereits ein?
- Wo führt **fehlende Abgrenzung** zu Problemen?
- Passt eure **Team-Struktur** zu euren Softwaregrenzen (Conway's Law)?

---

## Zusammenfassung

- **Bounded Context** = explizite Grenze für ein Domänenmodell
- **Subdomain** (Problemraum) ↔ **Bounded Context** (Lösungsraum)
- **Conway's Law**: Team-Grenzen ≈ BC-Grenzen
- **8 Context-Map-Patterns** für unterschiedliche Integrationsszenarien
- **Entscheidungsbaum**: Separate Ways → Partnership → Customer/Supplier → ACL → OHS
- Heuristiken: Sprache, Pivot Events, Akteure, Datenkohäsion, Teams
- Im Code: **ein Top-Level-Package pro BC**, keine Cross-BC-Imports

> Im nächsten Modul implementieren wir die **Building Blocks**
> innerhalb eines Bounded Context (Modul 05).
