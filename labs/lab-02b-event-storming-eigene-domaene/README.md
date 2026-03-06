# Lab 02b: Event Storming – Eure eigene Fachdomäne

## Lernziel

Die in Lab 02 erlernte Event-Storming-Methode auf die eigene Fachdomäne
anwenden. Einen realen Geschäftsprozess aus eurem System analysieren und dabei
Modulgrenzen, unklare Zuständigkeiten und versteckte Komplexität sichtbar
machen.

## Dauer

75 Minuten

## Vorbereitung (vor dem Lab)

Jede Gruppe wählt **einen konkreten Geschäftsprozess** aus dem eigenen System.
Gute Kandidaten sind:

- Ein Prozess, der **mehrere Module oder Services durchläuft**
- Ein Prozess, bei dem **unklar ist, wo neue Anforderungen hingehören**
- Ein Prozess, der **zu viele Zuständigkeiten in einem Modul bündelt**

> **Nicht geeignet:** Rein technische Abläufe (Deployment, Monitoring) oder
> triviale CRUD-Prozesse ohne Geschäftsregeln.

Falls noch keine Auswahl getroffen wurde, helfen diese Fragen:

- "Welcher Prozess verursacht die meisten Merge-Konflikte zwischen Teams?"
- "Wo dauern Änderungen am längsten, weil zu viel zusammenhängt?"
- "Welcher Service hat die meisten Zeilen Code?"

## Format

|                   |                                                                         |
|-------------------|-------------------------------------------------------------------------|
| **Gruppenarbeit** | 2–4 Personen pro Gruppe (Breakout-Räume)                                |
| **Board**         | Eigenes Miro-/Mural-Board pro Gruppe (Template siehe unten)             |
| **Trainer**       | Springt zwischen den Breakout-Räumen, ist aber nicht dauerhaft anwesend |

## Farbcodierung (Wiederholung aus Lab 02)

| Farbe  | Element         | Schreibweise        | Beispiel                              |
|--------|-----------------|---------------------|---------------------------------------|
| Orange | Domain Event    | Vergangenheitsform  | „Bestellung aufgegeben"               |
| Blau   | Command         | Imperativ           | „Bestellung aufgeben"                 |
| Gelb   | Aggregate       | Substantiv          | „Bestellung"                          |
| Lila   | Policy          | Wenn-Dann-Regel     | „Wenn bezahlt, dann Versand auslösen" |
| Rosa   | External System | Name des Systems    | „Payment Provider", „SAP"             |
| Rot    | Hot Spot        | Frage oder Konflikt | „Wer entscheidet über Rabatte?"       |

## Ablauf

### Phase 1: Events sammeln (15 Min)

> **Timebox: 15 Minuten – danach aufhören, auch wenn nicht alles erfasst ist.**

Jedes Gruppenmitglied schreibt **gleichzeitig und still** Domain Events auf
orangefarbene Stickies. Keine Diskussion in dieser Phase – Quantität vor
Qualität.

**Leitfragen für den Einstieg:**

1. Was ist der **auslösende Moment** eures Prozesses? (z.B. "Kundenanfrage
   eingegangen")
2. Was ist das **gewünschte Endergebnis**? (z.B. "Lieferung zugestellt")
3. **Was passiert dazwischen?** – Schreibt jeden Schritt als Event auf.

**Typische Fallen vermeiden:**

- ❌ "System prüft Eingabe" → Das ist kein fachliches Event
- ✅ "Kreditwürdigkeit bestätigt" → Das ist ein fachliches Event
- ❌ "Daten werden in DB geschrieben" → Technische Implementierung
- ✅ "Auftrag angelegt" → Fachliches Ergebnis

### Phase 2: Zeitlinie und Diskussion (15 Min)

1. Klebt alle Events auf eine **horizontale Zeitlinie** (links = früh, rechts =
   spät)
2. Entfernt **Duplikate** (kommt häufig vor – verschiedene Leute, gleicher
   Gedanke)
3. **Jetzt diskutieren:** Wo sind Lücken? Wo widersprechen sich Events? Wo
   fehlen Schritte?
4. Setzt **rote Hot-Spot-Stickies** auf Stellen, wo ihr euch uneinig seid oder
   wo die Zuständigkeit unklar ist

> **Hot Spots sind wichtig.** Sie zeigen genau die Stellen, an denen euer
> aktuelles System Probleme hat. Nicht auflösen – nur markieren.

### Phase 3: Commands, Aggregates, Actors (15 Min)

Ergänzt für die **wichtigsten Events** (nicht für alle – fokussiert euch auf den
Kernablauf):

- **Blau (Command):** Welche Aktion löst das Event aus?
- **Gelb (Aggregate):** Welches fachliche Objekt ist betroffen?
- **Actor:** Wer löst den Command aus? (Benutzerrolle, anderes System,
  Zeitsteuerung)

**Achtet besonders auf:**

- Stellen, wo **ein Aggregate zu viel kann** → Hinweis auf fehlendes Aufteilen
- Stellen, wo **zwei verschiedene Actors** dasselbe Aggregate verändern →
  mögliche BC-Grenze
- Stellen, wo ein Event **in einem anderen Teil des Systems eine Reaktion
  auslöst** → Kommunikation zwischen Bounded Contexts

### Phase 4: Grenzen erkennen (10 Min)

Das ist die Phase, die direkt auf euer Problem einzahlt – **zu große Module
aufbrechen**.

Schaut auf euer Board und sucht nach diesen Mustern:

1. **Sprachgrenzen:** Gibt es Stellen, wo sich die Begriffe ändern? Wo "Auftrag"
   plötzlich etwas anderes bedeutet als vorher?
2. **Pivot Events:** Gibt es Events, die einen **deutlichen Phasenwechsel**
   markieren? (z.B. "Auftrag bestätigt" – danach reden alle anders über die
   Daten)
3. **Autonome Abschnitte:** Gibt es Teile der Zeitlinie, die **unabhängig
   voneinander funktionieren** könnten?

**Zeichnet vertikale Linien** auf euer Board, wo ihr potenzielle
Bounded-Context-Grenzen seht. Gebt jedem Abschnitt einen **vorläufigen Namen**.

> Es ist OK, wenn die Grenzen nicht perfekt sind. In Lab 03 (Strategic Design)
> werden sie verfeinert.

### Phase 5: Kurzpräsentation vorbereiten (5 Min)

Bereitet eine **2-Minuten-Präsentation** für das Plenum vor:

1. Welchen Prozess habt ihr modelliert?
2. Wie viele Events habt ihr gefunden?
3. Wo sind die **Hot Spots** (die interessantesten Konflikte/Unklarheiten)?
4. Welche **Bounded-Context-Grenzen** habt ihr identifiziert?
5. Welche Erkenntnis hat euch am meisten überrascht?

## Plenum: Vorstellung und Diskussion (15 Min, nach den Breakouts)

Jede Gruppe präsentiert in 2–3 Minuten. Der Trainer moderiert und achtet auf:

- Wo haben verschiedene Gruppen **ähnliche Probleme** entdeckt?
- Wo tauchen **Hot Spots** auf, die auf zu große Module hindeuten?
- Wo gibt es **Pivot Events**, die natürliche Schnittgrenzen markieren?

## Verifikation

Prüft euer Ergebnis anhand folgender Kriterien:

- [ ] Mindestens **12 Domain Events** auf der Zeitlinie
- [ ] Events sind in der **Vergangenheitsform** formuliert
- [ ] Mindestens **3 Hot Spots** markiert (rote Stickies mit konkreten Fragen)
- [ ] Commands und Aggregates für die **Kern-Events** zugeordnet
- [ ] Mindestens **eine potenzielle Bounded-Context-Grenze** eingezeichnet
- [ ] **Kurzpräsentation** vorbereitet (5 Sätze reichen)

## Miro-Board-Template

Bereite pro Gruppe ein Miro-Board mit folgender Struktur vor:

```
┌──────────────────────────────────────────────────────────────────┐
│  LEGENDE (oben links, fixiert)                                   │
│  🟧 Domain Event  🟦 Command  🟨 Aggregate                       │
│  🟪 Policy  🟥 Hot Spot  🟩 Read Model  🩷 External System       │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  EUER PROZESS: _______________________________________________   │
│                                                                  │
│  ←── früh ──────────── ZEITLINIE ──────────── spät ──→           │
│                                                                  │
│  ┊                    ┊                    ┊                     │
│  ┊    Phase/BC 1?     ┊    Phase/BC 2?     ┊    Phase/BC 3?      │
│  ┊                    ┊                    ┊                     │
│                                                                  │
├──────────────────────────────────────────────────────────────────┤
│  PARKPLATZ (unten – für Events, die ihr nicht einordnen könnt)   │
└──────────────────────────────────────────────────────────────────┘
```

## Hinweise

- **Ihr seid die Fachexperten.** Anders als beim Immobilien-CRM gibt es hier
  niemanden, der die "richtige Antwort" kennt. Das ist Absicht – Event Storming
  funktioniert genau so.
- **Uneinigkeit ist ein Feature.** Wenn zwei Leute unterschiedliche Begriffe für
  dasselbe verwenden, habt ihr gerade eine Sprachgrenze entdeckt – und damit
  einen potenziellen Bounded Context.
- **Nicht zu granular werden.** Bleibt auf der Ebene der Geschäftsprozesse,
  nicht der Implementierung. "Bestellung aufgegeben" statt
  "OrderService.createOrder() aufgerufen".
- **Vergleicht mit Lab 02.** Was war beim Immobilien-CRM einfacher? Warum? Oft
  liegt es daran, dass die eigene Domäne organisch gewachsen ist und Begriffe
  mehrdeutig geworden sind – genau das macht DDD sichtbar.
- **Die Ergebnisse sind Arbeitsgrundlage.** Fotografiert oder exportiert euer
  Board. Es wird in Lab 03 (Strategic Design) weiterverwendet, und am Ende des
  Workshops (Lab 12) könnt ihr Teile davon in Code übersetzen.
