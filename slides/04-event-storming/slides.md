---
marp: true
theme: default
paginate: true
header: "DDD & Clean Architecture mit Spring Boot 4"
footer: "CC BY-NC-SA 4.0, Alexander Erben"
---

# Modul 04 - Event Storming

---

## Lernziele

- Das Format Event Storming nach Alberto Brandolini kennen
- Die Farben und Elemente korrekt einsetzen können
- Den Ablauf einer Event-Storming-Session leiten können
- Events, Commands und Aggregates für die Förderantragsverwaltung identifizieren
- Den Übergang von Event Storming zu Bounded Contexts verstehen

---

## Was ist Event Storming?

### Ein Workshop-Format zur Domänenexploration

- Erfunden von Alberto Brandolini (2013)
- Bringt Fachexperten und Entwickler an einen Tisch
- Visualisiert Geschäftsprozesse als Abfolge von Ereignissen
- Nutzt farbige Sticky Notes auf einer großen Papierbahn

> *"It is not the domain expert's knowledge that goes into production,
> it is the developer's assumption of that knowledge."*
> Alberto Brandolini

---

## Warum ein Board und keine Spezifikation?

Ein Dokument beschreibt, was **eine Person** verstanden hat.
Ein Board zeigt, was **ein Team** gemeinsam modelliert.

Der Unterschied:

```
Dokument:   Eine Person schreibt → andere lesen → jeder versteht es anders
Board:      Alle schreiben → alle diskutieren → gemeinsames Bild entsteht
```

> Wenn zwei Menschen auf ein Event zeigen und verschiedene Dinge meinen,
> ist das kein Fehler des Workshops — es ist das wichtigste Ergebnis.
> Das Board macht Unterschiede im Verständnis **sichtbar**, bevor sie im Code landen.

**Faustregel:** Wenn alle sofort einig sind, habt ihr noch nicht tief genug gegraben.

---

## Die Elemente - Farbübersicht

![Event Storming Elemente](images/event-storming-elemente.drawio.svg)

---

## Farbübersicht

| Farbe | Element | Frage, die es beantwortet               |
|-------|---------|-----------------------------------------|
| 🟧 Orange | Domain Event | Was ist passiert?                       |
| 🟦 Blau | Command | Was hat jemand ausgelöst?               |
| 🟨 Gelb | Aggregate | Wer entscheidet und schützt die Regeln? |
| 🟪 Lila | Policy | Was passiert als Folge?                 |
| 🩷 Rosa | External System | Welches System ist beteiligt?           |
| 🟩 Grün | Read Model | Welche Daten braucht der Akteur?        |
| 🟥 Rot | Hot Spot | Wo gibt es Unklarheiten?                |

---

## Domain Event (Orange)

### Etwas ist passiert - in der Vergangenheitsform

- Formulierung: Substantiv + Partizip oder Passivform
- Beschreibt ein fachliches Ergebnis, kein technisches
- Zeitlich geordnet von links nach rechts

### Beispiele aus der Förderantragsverwaltung

![Domain Event Beispiele](images/domain-event-beispiele.drawio.svg)

> Tipp: Zuerst Events sammeln - Reihenfolge und Details später klären.
> Menge vor Präzision!

---

<style scoped>section { font-size: 1.5em; }</style>

## Command (Blau)

### Eine Absicht, die ein Event auslöst

- Formulierung: Imperativ (Befehlsform)
- Wird von einem Akteur (Person oder System) ausgelöst
- Steht links neben dem zugehörigen Event

### Beispiele

| Command | → | Domain Event |
|---------|---|-------------|
| `FlurstückeDigitalisieren` | → | `FlurstückeDigitalisiert` |
| `AntragsmappeEinreichen` | → | `AntragsmappeEingereicht` |
| `KontrolleDurchführen` | → | `KontrolleDurchgeführt` |

> Command = die Absicht, Event = das Ergebnis.

---

<style scoped>section { font-size: 1.5em; }</style>

## Aggregate (Gelb)

### Die Konsistenzgrenze - entscheidet, ob ein Command erlaubt ist

- Empfängt einen Command, prüft Regeln und erzeugt ein Event
- Schützt Geschäftsregeln (Invarianten)
- Steht zwischen Command und Event

### Beispiele

| Aggregate | Verantwortlich für | Beispiel-Regel |
|-----------|-------------------|----------------|
| `AntragsMappe` | Flurstücke, Status, Einreichung | Einreichung nur mit mindestens einem Flurstück |
| `Prüfvorgang` | Fachliche Prüfschritte | Prüfung nur für eingereichte Anträge startbar |
| `Kontrolle` | Vor-Ort-Prüfung, Ergebnis | Kontrollergebnis erfordert begonnene Kontrolle |
| `Bescheid` | Förderentscheidung, Versand | Bescheid nur für positiv geprüfte Anträge |

---

## Der Kernflow: Command → Aggregate → Event

### So hängen die Elemente zusammen

![Command Aggregate Event Flow](images/command-aggregate-event-flow.drawio.svg)

> Die Policy löst den nächsten Command aus →
> so entsteht eine Kette von Events durch den Geschäftsprozess.

---
<style scoped>section { font-size: 1.5em; }</style>

## Policy (Lila)

### Automatische Reaktion auf ein Event

- Formulierung: "Wenn ... dann ..." oder "Immer wenn ..."
- Löst einen neuen Command aus - verknüpft Events miteinander
- Steht unterhalb des auslösenden Events

### Beispiele aus der Förderantragsverwaltung

| Auslösendes Event | Policy | Resultierender Command |
|-------------------|--------|----------------------|
| `FlurstückeDigitalisiert` | Wenn digitalisiert → Fläche prüfen | `FlaecheValidieren` |
| `AntragsmappeEingereicht` | Wenn eingereicht → Prüfung starten | `FachlichePrüfungStarten` |
| `KontrolleDurchgeführt` | Wenn OK → Bescheid vorbereiten | `BescheidVorbereiten` |
| `AntragPositivBeschieden` | Wenn beschieden → Zahlungsantrag anlegen | `ZahlungsantragAnlegen` |

> Policies sind der Klebstoff zwischen den Phasen eines Geschäftsprozesses.

---
<style scoped>section { font-size: 1.5em; }</style>

## External System (Rosa) & Read Model (Grün)

### External System - Systeme außerhalb unserer Domäne

- Beispiele: EU-IACS (InVeKoS-Daten), Bundesanstalt für Landwirtschaft, E-Mail-Provider, ZID (Zentrales Identitätsmanagement)
- Können Events empfangen oder Commands auslösen

### Read Model - Daten, die ein Akteur für seine Entscheidung braucht

- Steht links vor dem Command (der Akteur schaut sich Daten an, bevor er handelt)

### Konkretes Beispiel

![Read Model Akteur Command](images/read-model-akteur-command.drawio.svg)

> Die Sachbearbeiterin sieht die Antragsliste (Read Model) → entscheidet → löst Command aus.

---
<style scoped>section { font-size: 1.5em; }</style>

## Hot Spot (Rot)

### Unklarheiten und offene Fragen markieren

- Steht an Stellen mit Diskussionsbedarf
- Nicht sofort lösen - sammeln und später klären!

### Typische Hot Spots

- Widersprüchliche Aussagen von Fachexperten
- Unklare Geschäftsregeln oder Grenzfälle
- Fehlende Informationen
- Stellen, an denen sich die Ubiquitous Language ändert (→ BC-Grenze!)

### Beispiele aus der Förderantragsverwaltung

- "Kann eine AntragsMappe nach der Einreichung noch Flurstücke hinzufügen?"
- "Wann gilt eine Kontrolle als abgeschlossen — nach Besuch oder nach Dokumentation?"
- "Wer darf eine Bewilligungsentscheidung zurücknehmen — Sachbearbeiterin oder Behördenleitung?"
- "Was passiert, wenn ein Zahlungsantrag nach dem Bescheid korrigiert werden muss?"

---
<style scoped>section { font-size: 1.45em; }</style>

## Ubiquitous Language: Förderantragsverwaltung

> Unser Glossar — Begriffe, die im gesamten System eine eindeutige Bedeutung haben müssen

| Begriff | Bedeutung im Kontext |
|---------|---------------------|
| **AntragsMappe** | Das zentrale Aggregate: Gesamtheit aller Dokumente eines Förderantrags |
| **Antragsteller** | Landwirt / Betriebsinhaberin, die den Förderantrag stellt |
| **Sachbearbeiterin** | Mitarbeiterin der Bewilligungsstelle, die den Antrag prüft |
| **Bewilligungsstelle** | Behörde, die Förderanträge genehmigt oder ablehnt |
| **Flurstück** | Katasterparzelle — kleinste landwirtschaftliche Flächeneinheit |
| **AenderungsArt** | Art der Zustandsänderung: `UPDATED` · `REACTIVATED` · `REMOVED` · `ARCHIVED` |
| **Bescheid** | Rechtsmittelfähiges Verwaltungsdokument mit der Förderentscheidung |
| **Zahlungsantrag (ZA)** | Antrag auf Auszahlung einer bewilligten Förderung |
| **ELER** | Europäischer Landwirtschaftsfonds für die Entwicklung des ländlichen Raums |
| **Direktzahlungen (DZ)** | Flächenbezogene EU-Direktzahlungen an Landwirte |
| **Registerable** | Fachliches Interface: alles, was mit einer Registrierungsnummer versioniert wird |
| **MDB** | Message Driven Bean — der EJB-Empfänger von JMS-Nachrichten |

> Hot Spot: Bedeutet "Antrag" in der Antragstellung dasselbe wie in der Auszahlung?
> Nein — das ist bereits ein Signal für eine Bounded-Context-Grenze.

---
<style scoped>section { font-size: 1.5em; }</style>

## Beispiel: ELER-Direktzahlungen — Event Storming Board

### Kern-Events des ELER-Förderantragsprozesses

| 🟦 Command | 🟧 Domain Event | 🟨 Aggregate | Akteur | 🟪 Policy |
|-----------|---------------|------------|--------|----------|
| AntragsMappeErfassen | **AntragsmappeErstellt** | AntragsMappe | Antragsteller | — |
| FlurstückeDigitalisieren | **FlurstückeDigitalisiert** | AntragsMappe | GIS-Bearbeiterin | Wenn digitalisiert → Fläche prüfen |
| AntragsmappeEinreichen | **AntragsmappeEingereicht** | AntragsMappe | Antragsteller | Wenn eingereicht → Prüfung starten |
| FachlichePrüfungStarten | **FachlichePrüfungGestartet** | Prüfvorgang | System | — |
| KontrolleDurchführen | **KontrolleDurchgeführt** | Kontrolle | Kontrolleur | Wenn OK → Bescheid vorbereiten |
| AntragBescheiden | **AntragPositivBeschieden** | Bescheid | Bewilligungsstelle | Wenn beschieden → ZA anlegen |
| ZahlungAnweisen | **ZahlungAngewiesen** | Zahlungsantrag | Zahlstelle | Wenn angewiesen → Bescheid versenden |
| BescheidVersenden | **BescheidVersandt** | Bescheid | System | — |

### 🔴 Typische Hot Spots in diesem Prozess

- *"Wann ist eine AntragsMappe 'fertig'?"* — kein explizites Abschluss-Event, Zustand wird aus `AenderungsArt` inferiert
- *"Meinen Antragstellung und Auszahlung dasselbe mit 'Antrag'?"* — Sprachgrenze = BC-Grenze
- *"Wer darf eine Bewilligungsentscheidung zurücknehmen?"* — mehrere Konsumenten hören auf dasselbe Event, keine klare Verantwortung

---

## Session-Ablauf - 5 Phasen

![Event Storming 5 Phasen](images/event-storming-phasen.drawio.svg)

---
<style scoped>section { font-size: 1.7em; }</style>

## Phase 1: Chaotische Exploration (15-20 Min.)

### Alle schreiben Domain Events auf orange Sticky Notes

- Keine Diskussion - einfach alles aufschreiben, was im Prozess passiert
- Auf die Papierbahn kleben, grobe zeitliche Ordnung (links = früh, rechts = spät)
- Menge vor Qualität - auch "falsche" Events sind wertvoll
- Duplikate sind okay, werden später zusammengeführt

### Facilitator-Tipps

- "Schreibt auf, was passiert, nicht was jemand tut"
- "Vergangenheitsform verwenden!"
- "Jede Note = ein Event"
- Ruhige Teilnehmer aktiv ansprechen

---
<style scoped>section { font-size: 1.6em; }</style>

## Phase 2-3: Timeline ordnen & Hot Spots (20-25 Min.)

### Phase 2: Timeline sortieren

- Events in chronologische Reihenfolge bringen
- Duplikate zusammenführen
- Lücken identifizieren: "Was passiert zwischen X und Y?"
- Parallele Stränge untereinander anordnen

### Phase 3: Hot Spots markieren

- Rote Sticky Notes auf Unklarheiten kleben
- Kurze Diskussion zu den wichtigsten Punkten
- Hot Spots sind wertvoll - nicht sofort lösen wollen!

> Hot Spots an Stellen, wo sich die Sprache ändert, deuten
> auf Bounded-Context-Grenzen hin.

---
<style scoped>section { font-size: 1.7em; }</style>

## Phase 4-5: Commands, Akteure, Aggregates & Policies (30 Min.)

### Phase 4: Commands und Akteure

- Blaue Sticky Notes für Commands links neben die Events
- Akteure identifizieren: Wer löst den Command aus?
  - Antragsteller, Sachbearbeiterin, Kontrolleur, Zahlstelle, System, Zeitablauf

### Phase 5: Aggregates und Policies

- Gelbe Notes für Aggregates zwischen Command und Event
- Lila Notes für Policies unterhalb der Events
- Rosa Notes für External Systems am Rand

---

### Das Board sieht dann so aus:

![Event Storming Board Layout](images/event-storming-board-layout.drawio.svg)

---

## Domain Storytelling — eine ergänzende Technik

Event Storming erforscht **Was** passiert im Prozess. Domain Storytelling geht tiefer: es erklärt **Wer** mit **was** interagiert, **wie** und **warum**.

> *"Domain Storytelling bridges the gap between domain experts and developers by visually modeling processes in narrative form."*
> — Santana, „Domain-Driven Design with Java" (2026), Kap. 13

| Aspekt | Event Storming | Domain Storytelling |
|--------|---------------|---------------------|
| Fokus | Was passiert? | Wer macht was, womit, wie? |
| Technik | Sticky Notes (5 Farben) | Akteure + Arbeitsschritte + Fachgegenstände |
| Output | Prozess-Timeline mit Events | Narrative Prozessdiagramme |
| Versionierung | Foto/Miro | JSON-Dateien `.egn` in Git (Egon.io) |
| DDD-Beitrag | Bounded Contexts, Events | Ubiquitous Language, Interaktionen |

### Egon.io in der Praxis

- Browser-Tool, kein Account nötig: [egon.io](https://egon.io)
- Export als `.egn`-Datei → direkt in Git committen
- Bilder über einen Export als SVG/PNG dokumentierbar
- Ergänzt den Code als lebendige Domänendokumentation

> **Tipp:** Event Storming am Tag 1 — Domain Storytelling zur Vertiefung einzelner Prozesse.
> Santana, „Domain-Driven Design with Java" (2026), Kap. 13: Domain Storytelling als letzter Schritt vor der Implementierung.

---

## Zum Nachlesen

- Khononov, „Einführung in Domain-Driven Design" (2022), Kapitel 12: EventStorming — Prozesse visualisieren und gemeinsames Verständnis aufbauen
- Santana, „Domain-Driven Design with Java" (2026), Kap. 13: Domain Storytelling — Brücke zwischen Fachexpertise und Code
