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

# Modul 03 – Event Storming

**Geschätzte Dauer: 120 Minuten (inkl. Workshop)**

### Lernziele

- Das Format Event Storming nach Alberto Brandolini kennen
- Die Farben und Elemente korrekt einsetzen können
- Den Ablauf einer Event-Storming-Session leiten können
- Events, Commands und Aggregates für das Immobilien-CRM identifizieren
- Den Übergang von Event Storming zu Bounded Contexts verstehen

---

## Was ist Event Storming?

### Ein Workshop-Format zur Domänenexploration

- Erfunden von **Alberto Brandolini** (2013)
- Bringt **Fachexperten und Entwickler** an einen Tisch
- Visualisiert **Geschäftsprozesse** als Abfolge von Ereignissen
- Nutzt **farbige Sticky Notes** auf einer großen Papierbahn

> *"It is not the domain expert's knowledge that goes into production,
> it is the developer's assumption of that knowledge."*
> — Alberto Brandolini

---

## Warum Event Storming?

### Vorteile gegenüber klassischen Analyse-Methoden

- **Gemeinsames Verständnis** entsteht in kurzer Zeit (Stunden statt Wochen)
- **Implizites Wissen** der Fachexperten wird sichtbar gemacht
- **Konflikte und Unklarheiten** werden früh erkannt (Hot Spots)
- Ermöglicht **Bottom-Up-Entdeckung** von Bounded Contexts
- Basis für **Ubiquitous Language** und das Domänenmodell
- Kein technisches Vorwissen nötig – **alle können mitmachen**

### Varianten

| Variante | Ziel | Dauer |
|----------|------|-------|
| **Big Picture** | Gesamtüberblick über die Domäne | 2–4 Stunden |
| **Process Modelling** | Detaillierter Prozessablauf | 2–3 Stunden |
| **Design Level** | Aggregate-Design, Bounded Contexts | 1–2 Stunden |

---

## Die Elemente – Farbübersicht

![Event Storming Elemente](../diagrams/event-storming-elemente.drawio.png)

| Farbe | Element | Frage, die es beantwortet |
|-------|---------|---------------------------|
| 🟧 Orange | **Domain Event** | Was ist passiert? |
| 🟦 Blau | **Command** | Was hat jemand ausgelöst? |
| 🟨 Gelb | **Aggregate** | Wer entscheidet und schützt die Regeln? |
| 🟪 Lila | **Policy** | Was passiert automatisch danach? |
| 🩷 Rosa | **External System** | Welches System ist beteiligt? |
| 🟩 Grün | **Read Model** | Welche Daten braucht der Akteur? |
| 🟥 Rot | **Hot Spot** | Wo gibt es Unklarheiten? |

---

## Domain Event (Orange)

### Etwas ist passiert – in der Vergangenheitsform

- Formulierung: **Substantiv + Partizip** oder **Passivform**
- Beschreibt ein **fachliches Ergebnis**, kein technisches
- Zeitlich geordnet von **links nach rechts**

### Beispiele aus dem Immobilien-CRM

```
  ┌──────────────────────┐  ┌──────────────────────┐  ┌──────────────────────┐
  │  ObjektErfasst       │  │  ObjektBewertet       │  │  MaklervertragUnter- │
  │                      │  │                      │  │  schrieben           │
  └──────────────────────┘  └──────────────────────┘  └──────────────────────┘
        (orange)                  (orange)                  (orange)
```

> **Tipp:** Zuerst Events sammeln – Reihenfolge und Details später klären.
> Menge vor Präzision!

---

## Command (Blau)

### Eine Absicht, die ein Event auslöst

- Formulierung: **Imperativ** (Befehlsform)
- Wird von einem **Akteur** (Person oder System) ausgelöst
- Steht **links neben** dem zugehörigen Event

### Beispiele

| Command | → | Domain Event |
|---------|---|-------------|
| `KontaktiereEigentümer` | → | `EigentümerKontaktiert` |
| `BewerteObjekt` | → | `ObjektBewertet` |
| `ErstelleExposé` | → | `ExposéErstellt` |
| `FühreBesichtigungDurch` | → | `BesichtigungDurchgeführt` |
| `NimmAngebotAn` | → | `AngebotAngenommen` |

> Command = die **Absicht**, Event = das **Ergebnis**.

---

## Aggregate (Gelb)

### Die Konsistenzgrenze – entscheidet, ob ein Command erlaubt ist

- Empfängt einen **Command**, prüft Regeln und erzeugt ein **Event**
- Schützt **Geschäftsregeln** (Invarianten)
- Steht **zwischen** Command und Event

### Beispiele

| Aggregate | Verantwortlich für | Beispiel-Regel |
|-----------|-------------------|----------------|
| `Immobilie` | Objektdaten, Bewertung | Kann nur bewertet werden, wenn erfasst |
| `Maklerauftrag` | Vertragskonditionen | Nur ein aktiver Vertrag pro Objekt |
| `Vermittlungsvorgang` | Besichtigungen, Angebote | Notartermin nur mit angenommenem Angebot |
| `Exposé` | Inhalte, Freigabe | Nur bewertete Objekte bekommen ein Exposé |

---

## Der Kernflow: Command → Aggregate → Event

### So hängen die Elemente zusammen

```
┌──────────┐     ┌────────────────┐     ┌───────────────────┐
│ Akteur   │     │   Aggregate    │     │   Domain Event     │
│ (Makler) │────►│   (Immobilie)  │────►│  (ObjektBewertet)  │
└──────────┘     └────────────────┘     └───────────────────┘
      │                 ▲                        │
      │                 │                        │
      ▼                 │                        ▼
┌──────────┐     ┌────────────────┐     ┌───────────────────┐
│ Command  │     │  Geschäfts-    │     │   Policy           │
│(Bewerte  │     │  regel prüfen  │     │  (Wenn bewertet →  │
│ Objekt)  │     │                │     │   Exposé erstellen)│
└──────────┘     └────────────────┘     └───────────────────┘
   (blau)            (gelb)                   (lila)
```

> Die **Policy** löst automatisch den nächsten Command aus →
> so entsteht eine **Kette von Events** durch den Geschäftsprozess.

---

## Policy (Lila)

### Automatische Reaktion auf ein Event

- Formulierung: **"Wenn … dann …"** oder **"Immer wenn …"**
- Löst einen **neuen Command** aus – verknüpft Events miteinander
- Steht **unterhalb** des auslösenden Events

### Beispiele aus dem Immobilien-CRM

| Auslösendes Event | Policy | Resultierender Command |
|-------------------|--------|----------------------|
| `ObjektBewertet` | Wenn bewertet → Exposé vorbereiten | `ErstelleExposé` |
| `MaklervertragUnterschrieben` | Wenn Vertrag → Vermarktung starten | `StarteVermarktung` |
| `ExposéErstellt` | Wenn Exposé fertig → veröffentlichen | `VeröffentlicheInserat` |
| `AngebotAngenommen` | Wenn angenommen → Notar planen | `VereinbareNotartermin` |

> Policies sind der **Klebstoff** zwischen den Phasen eines Geschäftsprozesses.

---

## External System (Rosa) & Read Model (Grün)

### External System – Systeme außerhalb unserer Domäne

- Beispiele: ImmoScout24-API, Grundbuchamt, E-Mail-Provider, Notar-Portal, Bank
- Können Events empfangen oder Commands auslösen

### Read Model – Daten, die ein Akteur für seine Entscheidung braucht

- Steht **links vor** dem Command (der Akteur schaut sich Daten an, bevor er handelt)

### Konkretes Beispiel

```
┌───────────────────┐     ┌──────────┐     ┌──────────────┐
│ Immobilien-       │     │  Makler  │     │ Bewerte      │
│ übersicht         │────►│ (Akteur) │────►│ Objekt       │
│ (Read Model 🟩)  │     │          │     │ (Command 🟦) │
└───────────────────┘     └──────────┘     └──────────────┘
```

> Der Makler sieht die Übersicht (Read Model) → entscheidet → löst Command aus.

---

## Hot Spot (Rot)

### Unklarheiten und offene Fragen markieren

- Steht an Stellen mit **Diskussionsbedarf**
- **Nicht sofort lösen** – sammeln und später klären!

### Typische Hot Spots

- Widersprüchliche Aussagen von Fachexperten
- Unklare Geschäftsregeln oder Grenzfälle
- Fehlende Informationen
- Stellen, an denen sich die **Ubiquitous Language** ändert (→ BC-Grenze!)

### Beispiele aus dem Immobilien-CRM

- "Kann ein Objekt **mehrere** Maklerverträge gleichzeitig haben?"
- "Wann genau gilt ein Exposé als **fertig**?"
- "Wer darf den **Angebotspreis ändern** – der Makler oder der Eigentümer?"
- "Was passiert, wenn ein Kaufinteressent sein Angebot **zurückzieht**?"

---

## Vorbereitung: Setup für die Session

### Physisch (empfohlen)

- **5–8 Meter** Papierbahn an der Wand
- Sticky Notes in allen 7 Farben + dicke Marker
- Kein Beamer, keine Laptops – alle stehen und kleben
- Raum mit genug **Platz zum Stehen**

### Digital (Alternative)

- **Miro**, **FigJam** oder **Excalidraw**
- Vorbereitetes Board mit farbigen Vorlagen
- Gut für Remote-Teams, aber weniger Dynamik

### Wer sollte dabei sein?

- Fachexperten (Makler, Vermarktungsleiter, Backoffice)
- Entwickler und Architekten
- Facilitator (lenkt, stellt Fragen, sorgt für Fokus)

---

## Session-Ablauf – 5 Phasen

```
Phase 1          Phase 2          Phase 3          Phase 4          Phase 5
┌───────────┐   ┌───────────┐   ┌───────────┐   ┌───────────┐   ┌───────────┐
│  Events   │──►│ Timeline  │──►│Hot Spots  │──►│ Commands  │──►│Aggregates │
│  sammeln  │   │ ordnen    │   │ markieren │   │ + Akteure │   │+ Policies │
│           │   │           │   │           │   │           │   │           │
│  15-20'   │   │  10-15'   │   │   10'     │   │   15'     │   │   15'     │
└───────────┘   └───────────┘   └───────────┘   └───────────┘   └───────────┘
   chaotisch       sortieren      hinterfragen    wer löst aus?   Grenzen +
   alles raus!     Duplikate      Lücken finden   welche Absicht? Automatis.
```

---

## Phase 1: Chaotische Exploration (15–20 Min.)

### Alle schreiben Domain Events auf orange Sticky Notes

- **Keine Diskussion** – einfach alles aufschreiben, was im Prozess passiert
- Auf die Papierbahn kleben, grobe zeitliche Ordnung (links = früh, rechts = spät)
- **Menge vor Qualität** – auch „falsche" Events sind wertvoll
- Duplikate sind okay, werden später zusammengeführt

### Facilitator-Tipps

- "Schreibt auf, was **passiert**, nicht was jemand **tut**"
- "Vergangenheitsform verwenden!"
- "Jede Note = **ein** Event"
- Ruhige Teilnehmer aktiv ansprechen

---

## Phase 2–3: Timeline ordnen & Hot Spots (20–25 Min.)

### Phase 2: Timeline sortieren

- Events in **chronologische Reihenfolge** bringen
- **Duplikate** zusammenführen
- **Lücken** identifizieren: "Was passiert zwischen X und Y?"
- **Parallele Stränge** untereinander anordnen

### Phase 3: Hot Spots markieren

- Rote Sticky Notes auf **Unklarheiten** kleben
- Kurze Diskussion zu den **wichtigsten** Punkten
- Hot Spots sind **wertvoll** – nicht sofort lösen wollen!

> Hot Spots an Stellen, wo sich die **Sprache ändert**, deuten
> auf Bounded-Context-Grenzen hin.

---

## Phase 4–5: Commands, Akteure, Aggregates & Policies (30 Min.)

### Phase 4: Commands und Akteure

- **Blaue** Sticky Notes für Commands links neben die Events
- **Akteure** identifizieren: Wer löst den Command aus?
  - Makler, Eigentümer, Interessent, System, Zeitablauf

### Phase 5: Aggregates und Policies

- **Gelbe** Notes für Aggregates zwischen Command und Event
- **Lila** Notes für Policies unterhalb der Events
- **Rosa** Notes für External Systems am Rand

### Das Board sieht dann so aus:

```
[Read Model] → Akteur → [Command] → [Aggregate] → [Domain Event]
                                                         │
                                                    [Policy] → [Command] → ...
                                                         │
                                                  [Ext. System]
```

---

## Unser Immobilien-CRM – Der Gesamtprozess

![Event Storming Immobilien-CRM](../diagrams/event-storming-immobilien-crm.drawio.png)

---

## Event-Übersicht: Immobilien-CRM

| Phase | Domain Events | Aggregate |
|-------|--------------|-----------|
| **Akquise** | `EigentümerKontaktiert`, `ObjektErfasst`, `ObjektBesichtigt` | Kontakt, Immobilie |
| **Bewertung** | `ObjektBewertet`, `MaklervertragUnterschrieben` | Immobilie, Maklerauftrag |
| **Vermarktung** | `ExposéErstellt`, `InseratVeröffentlicht` | Exposé, Inserat |
| **Besichtigung** | `BesichtigungDurchgeführt`, `InteressentRegistriert` | Vermittlungsvorgang |
| **Verhandlung** | `AngebotEingegangen`, `AngebotAngenommen`, `AngebotAbgelehnt` | Vermittlungsvorgang |
| **Abschluss** | `NotarterminVereinbart`, `KaufvertragUnterschrieben` | Vermittlungsvorgang |

> Diese Events bilden die Basis für unsere **Ubiquitous Language**
> und tauchen als Java Records im Code wieder auf.

---

## Konkreter Durchlauf: Von der Akquise zum Abschluss

```
Makler                              Makler                         System
  │                                   │                              │
  ▼                                   ▼                              ▼
[Kontaktiere    [Bewerte    [Unterschreibe         [Erstelle        [Veröffentliche
 Eigentümer]    Objekt]      Maklervertrag]         Exposé]          Inserat]
     │             │              │                    │                │
     ▼             ▼              ▼                    ▼                ▼
 ┌────────┐   ┌────────┐   ┌──────────────┐      ┌────────┐      ┌────────┐
 │Kontakt │   │Immob.  │   │Maklerauftrag │      │Exposé  │      │Inserat │
 └────────┘   └────────┘   └──────────────┘      └────────┘      └────────┘
     │             │              │                    │                │
     ▼             ▼              ▼                    ▼                ▼
 Eigentümer   Objekt         Maklervertrag        Exposé           Inserat
 Kontaktiert  Bewertet       Unterschrieben       Erstellt         Veröffentlicht
                                  │                                    │
                                  └──[Policy]──────────┘               │
                                  "Wenn Vertrag → Exposé"             ...
```

---

## Vom Event Storming zum Bounded Context

### Drei Signale für eine BC-Grenze

**1. Sprachliche Grenze** – gleiche Begriffe, andere Bedeutung
- "Immobilie" in der Objektverwaltung ≠ "Immobilie" in der Vermarktung

**2. Pivot Events** – Events, die eine neue Phase einleiten
- `MaklervertragUnterschrieben` → Grenze zwischen Akquise und Vermarktung
- `AngebotAngenommen` → Grenze zwischen Vermittlung und Abschluss

**3. Akteurwechsel** – andere Person übernimmt
- Makler (Akquise) → Marketing-Team (Vermarktung)

---

## Vom Event Storming zu Bounded Contexts (Forts.)

### Schnittstellen erkennen

```
  ┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
  │   Akquise       │     │  Vermarktung    │     │  Vermittlung    │
  │                 │     │                 │     │                 │
  │  Maklervertrag  │────►│  Exposé         │────►│  Besichtigung   │
  │  Unterschrieben │     │  Erstellt       │     │  Durchgeführt   │
  │   (Pivot)       │     │   (Pivot)       │     │                 │
  └─────────────────┘     └─────────────────┘     └─────────────────┘
         Event verbindet die Kontexte → wird zur Schnittstelle
```

> Die **Pivot Events** werden später zu **Integration Events**
> zwischen den Bounded Contexts (→ Modul 12, Lab 09).

---

## Häufige Fehler vermeiden

| Fehler | Richtig |
|--------|---------|
| Events in der **Gegenwartsform** | Immer Vergangenheit: "Bewertet", nicht "Bewerten" |
| Zu früh über **Technik** sprechen | Fokus auf Fachlichkeit, keine DB-Tabellen! |
| **CRUD-Events** | Fachliche Events: nicht "Updated", sondern "Bewertet" |
| **Nur Entwickler** einladen | Fachexperten sind essenziell |
| Events zu **feingranular** | Ein Event pro fachlich relevantem Ergebnis |
| Sortierung **erzwingen** | Zuerst sammeln, dann sortieren |
| Hot Spots **sofort lösen** | Markieren, sammeln, später klären |

---

## Tipps für die Facilitation

- **Große Fläche** – mindestens 5–8 Meter Papierbahn
- **Timeboxing** strikt einhalten – jede Phase hat ein Zeitlimit
- **"Schreibt Events, keine Features!"** – immer wieder erinnern
- **Ruhige Teilnehmer** direkt ansprechen: "Was passiert bei dir im Bereich X?"
- **Fotos machen** – das Board nach jeder Phase fotografieren
- **Ergebnisse digitalisieren** – Miro, draw.io oder Markdown-Tabelle

### Nach der Session

1. Events in Markdown-Tabelle übertragen
2. Aggregates und Policies zuordnen
3. Potenzielle Bounded Contexts markieren
4. Hot Spots priorisieren und in Tickets überführen

---

## 🎯 Hands-on: Lab 02

### Event Storming für das Immobilien-CRM

**Aufgabe in Kleingruppen:**

1. Sammelt alle Domain Events (orange) – Ziel: mindestens 15
2. Bringt sie in eine zeitliche Reihenfolge
3. Fügt Commands (blau) und Akteure hinzu
4. Identifiziert Aggregates (gelb) – Wer entscheidet?
5. Markiert Hot Spots (rot) bei Unklarheiten
6. Notiert Policies (lila) – Was passiert automatisch?

> **Dauer:** ca. 90 Minuten
> **Material:** Sticky Notes (7 Farben), Marker, Papierbahn oder Miro-Board

---

## 💬 Diskussion: Welche Events fehlen noch?

> Welche Events fallen euch noch ein, die wir nicht abgedeckt haben?

Denkt an:

- **Sonderfälle** – Vertragsrücktritt, Stornierung, Fristablauf
- **Zeitliche Events** – Maklervertrag läuft ab, Exposé veraltet
- **Externe Trigger** – Behörden, Banken, Gutachter
- **Fehlschläge** – Besichtigung abgesagt, Angebot abgelehnt, Finanzierung geplatzt
- **Parallele Prozesse** – mehrere Interessenten gleichzeitig

**Sammelt mindestens 5 weitere Events und diskutiert sie in der Runde.**

---

## Zusammenfassung

- **Event Storming** bringt Fachexperten und Entwickler zusammen
- **Domain Events** (orange) sind das zentrale Element – alles startet hier
- Der **Kernflow**: Command → Aggregate → Event → Policy → Command …
- **Hot Spots** machen Unklarheiten sichtbar – und zeigen BC-Grenzen
- **Pivot Events** markieren den Übergang zwischen Bounded Contexts
- Das Ergebnis bildet die Grundlage für **Strategic Design** (Modul 04)
  und die **Building Blocks** (Modul 05)

> Im nächsten Modul leiten wir aus dem Event Storming
> die **Bounded Contexts** und die **Context Map** ab.
