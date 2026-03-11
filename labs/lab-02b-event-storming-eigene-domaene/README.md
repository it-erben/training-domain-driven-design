# Lab 02b: Event Storming - Förderantragsverwaltung

## Empfohlenes Szenario: ELER-Direktzahlungs-Antragsprozess

> Dieser Prozess ist ideal, weil er mehrere Bounded Contexts berührt,
> eine lange ereignisreiche Prozesskette hat und die Messaging-Grenzen
> im Produktivcode sichtbar sind.

### Unser Glossar für dieses Lab

| Begriff | Bedeutung |
|---------|-----------|
| **AntragsMappe** | Zentrales Aggregate: alle Dokumente eines Förderantrags |
| **Antragsteller** | Landwirt / Betriebsinhaberin |
| **Sachbearbeiterin** | Mitarbeiterin der Bewilligungsstelle |
| **Flurstück** | Katasterparzelle (kleinste Flächeneinheit) |
| **AenderungsArt** | `UPDATED` · `REACTIVATED` · `REMOVED` · `ARCHIVED` |
| **Bescheid** | Rechtsmittelfähiges Verwaltungsdokument |
| **Zahlungsantrag (ZA)** | Antrag auf Auszahlung einer Förderung |
| **Bewilligungsstelle** | Behörde, die Förderanträge genehmigt oder ablehnt |

### Einstiegspunkt: Welcher Prozess?

Modelliert **einen** dieser Prozesse — je nach Gruppe und Erfahrung:

| Prozess | Einstieg | Schwerpunkt |
|---------|----------|-------------|
| ELER Direktzahlungs-Antrag (DZ) | AntragsmappeErstellt | Flächen, Kontrollen, Bescheid |
| Auszahlungsantrag (ZA) | AntragPositivBeschieden | Zahlungslogik, Kautionsverwaltung |
| Bescheidversand | ZahlungAngewiesen | Messaging, Event-basierte Steuerung |
| Webhook-Benachrichtigung | ZahlungAngewiesen | Outbound Events, externe Systeme |

---

## Aufgabe

Wendet Event Storming auf einen konkreten Geschäftsprozess aus der
Förderantragsverwaltung an.

Dieses Lab ist ein reines Modellierungs-Lab - es wird kein Code geschrieben.

Ihr arbeitet jetzt nicht mehr mit einer vorgegebenen Beispieldomäne,
sondern mit einem Prozess, der in der Praxis real existiert.
Gerade dadurch werden implizites Wissen, uneinheitliche Begriffe und
versteckte Komplexität sichtbar.

## Vorbereitung (vor dem Lab)

Jede Gruppe wählt einen einzelnen Geschäftsprozess. Geeignet sind Prozesse, die:

- für das Geschäft sichtbar relevant sind
- von mehreren Rollen geprägt werden
- fachliche Entscheidungen oder Regeln enthalten
- im Team regelmäßig zu Rückfragen, Missverständnissen oder Diskussionen führen

Falls ihr noch keinen Prozess gewählt habt, helfen diese Fragen:

- "Wo müssen wir Fachlogik erklären, bevor jemand Änderungen machen kann?"
- "Wo hängen mehrere Rollen oder Teams inhaltlich zusammen?"
- "Wo ist unklar, wann ein Fall in die nächste Phase übergeht?"
- "Wo verwenden verschiedene Beteiligte ähnliche Begriffe unterschiedlich?"

### Typische Startpunkte für diesen Prozess

- *"Wann ist eine AntragsMappe 'fertig'?"* — kein explizites Event, der Zustand wird aus `AenderungsArt` inferiert
- *"Wer ist für die Entscheidung zuständig, wenn eine Bewilligungsentscheidung zurückgenommen wird?"* — mehrere Listener hören zu, keine klare Verantwortung
- *"Ist eine Kontrolle Teil der AntragsMappe oder ein eigenständiges Aggregate?"* — eigene Listener, aber dasselbe Topic
- *"Warum gibt es Polling für den Bescheidversand statt eines Event-Listeners?"* — fehlende BC-Grenze

## Zielbild für dieses Lab

Am Ende soll ein Board vorliegen, das diese Fragen beantwortet:

- Welche fachlich relevanten Dinge passieren in diesem Prozess?
- In welcher zeitlichen Reihenfolge passieren sie?
- Welche 5-8 Events bilden den Kernablauf?
- Wo gibt es Unklarheiten, Konflikte oder Sprachwechsel?
- An welchen Stellen scheint Verantwortung oder Bedeutung zu wechseln?

## Format

|                   |                                                                         |
|-------------------|-------------------------------------------------------------------------|
| Gruppenarbeit | 2-4 Personen pro Gruppe (Breakout-Räume)                                |
| Board         | Eigenes Miro-/Mural-Board pro Gruppe (Template siehe unten)             |
| Trainer       | Springt zwischen den Breakout-Räumen, ist aber nicht dauerhaft anwesend |

## Farbcodierung

| Farbe  | Element         | Beispiel                              |
|--------|-----------------|---------------------------------------|
| Orange | Domain Event    | "Antrag geprüft"                      |
| Rot    | Hot Spot        | "Wer darf die Priorität ändern?"      |
| Blau   | Command         | "Antrag prüfen"                       |
| Gelb   | Aggregate       | "Antrag"                              |
| Lila   | Policy          | "Wenn genehmigt, dann Auszahlung"     |
| Rosa   | External System | "EU-IACS", "ZID"                      |
| Grün   | Read Model      | "Offene Anträge", "Antragsliste"      |

Actors werden nicht als eigenes Board-Element modelliert. Notiert sie
direkt am Command oder daneben, z. B. "Sachbearbeiterin", "Antragsteller", "System".

## Ablauf

### Phase 1: Domain Events sammeln (15 Min)

> Timebox: 15 Minuten - danach wird sortiert, nicht weiter gesammelt.

Jedes Gruppenmitglied schreibt gleichzeitig und still Domain Events auf
orangefarbene Stickies. Keine Diskussion in dieser Phase.

Leitfragen:

1. Was ist der auslösende Moment eures Prozesses?
2. Was ist das gewünschte fachliche Ergebnis?
3. Welche fachlich relevanten Dinge passieren dazwischen?

Wichtig:

- Formuliert Events in der Vergangenheitsform
- Beschreibt fachliche Ergebnisse, keine technischen Implementierungsdetails
- Duplikate und unscharfe Formulierungen sind in dieser Phase erlaubt

Typische Fallen:

- Nicht: "Datensatz gespeichert"
- Besser: "Antrag erfasst"
- Nicht: "Service validiert Eingabe"
- Besser: "Bonität geprüft"

### Phase 2: Zeitlinie ordnen und Hot Spots markieren (15 Min)

Ordnet die gesammelten Domain Events auf einer horizontalen Zeitlinie von links
(früh) nach rechts (spät).

Arbeitet dann gemeinsam heraus:

- Welche Events sind Duplikate?
- Wo fehlen offenbar fachliche Schritte?
- Welche 5-8 Events bilden den Kernablauf?
- Wo gibt es Widersprüche, offene Fragen oder unterschiedliche Begriffe?

Markiert Unklarheiten sofort mit roten Hot Spots.

Typische Hot Spots:

- "Wer entscheidet hier eigentlich?"
- "Ist das noch derselbe Fall oder schon eine neue Phase?"
- "Meinen wir mit diesem Begriff alle dasselbe?"

> Hot Spots werden nicht gelöst. Sie sind ein zentrales Ergebnis des Labs.

### Phase 3: Commands, Actors und gezielte Vertiefung (10 Min)

Ergänzt nur für die wichtigsten Kern-Events:

- Command (blau): Welche Aktion hat das Event ausgelöst?
- Actor: Wer hat den Command ausgelöst?

Vertieft anschließend 2-3 fachlich interessante Stellen, wenn es hilft:

- Aggregate (gelb): Welches fachliche Objekt schützt hier Regeln?
- Policy (lila): Wo reagiert das System automatisch auf ein Event?
- External System (rosa): Wo ist ein fremdes System beteiligt?
- Read Model (grün): Welche Sicht braucht ein Akteur zur Entscheidung?

Fokus bleibt auf dem Verständnis des Prozesses, nicht auf einem vollständigen
Design-Level-Board.

### Phase 4: Ergebnis für Lab 03 festhalten (5 Min)

Haltet das Ergebnis in kompakter Form fest:

- Name des modellierten Prozesses
- 5-8 Kern-Events in Reihenfolge
- mindestens 3 Hot Spots
- auffällige Sprachwechsel, Verantwortungswechsel oder Phasenwechsel

Wenn ihr digital arbeitet, exportiert oder speichert das Board. Dieses Artefakt
wird in Lab 03 weiterverwendet.

## Plenum: Kurzvorstellung (15 Min)

Jede Gruppe präsentiert in 2-3 Minuten:

1. Welchen Prozess habt ihr modelliert?
2. Welche 5-8 Events bilden euren Kernablauf?
3. Wo liegen eure wichtigsten Hot Spots?
4. Wo habt ihr Sprach-, Verantwortungs- oder Phasenwechsel beobachtet?

---

## Referenz: Kern-Events des ELER-Prozesses

| Nr | Domain Event (Orange) | Aggregate | Akteur |
|----|----------------------|-----------|--------|
| 1 | **AntragsmappeErstellt** | AntragsMappe | Antragsteller |
| 2 | **FlurstückeDigitalisiert** | AntragsMappe | GIS-Bearbeiterin |
| 3 | **AntragsmappeEingereicht** | AntragsMappe | Antragsteller |
| 4 | **FachlichePrüfungGestartet** | Prüfvorgang | System (Policy) |
| 5 | **KontrolleDurchgeführt** | Kontrolle | Kontrolleur |
| 6 | **AntragPositivBeschieden** | Bescheid | Bewilligungsstelle |
| 7 | **AuszahlungHinzugefügt** | Zahlungsantrag | System (Policy) |
| 8 | **ZahlungAngewiesen** | Zahlungsantrag | Zahlstelle |
| 9 | **BescheidVersandt** | Bescheid | System |

### Heiße Hot Spots

- "Wann ist eine AntragsMappe eigentlich 'fertig'?" — kein explizites Abschluss-Event
- "Wer darf eine Bewilligungsentscheidung zurücknehmen?" — unklare Verantwortung
- "Meinen 'Antrag' und 'AntragsMappe' dasselbe?" — implizite Sprachgrenze
- "Ist eine Kontrolle Teil der AntragsMappe oder ein eigenes Aggregate?"

### Context-Grenzen aus den Pivot Events

| Pivot Event | Grenze zwischen |
|-------------|-----------------|
| `AntragsmappeEingereicht` | Antragstellung → Fachliche Prüfung |
| `AntragPositivBeschieden` | Fachliche Prüfung → Auszahlung |
| `ZahlungAngewiesen` | Auszahlung → Bescheidversand |
