# Lab 02: Event Storming - Die Förderantrags-Domäne erkunden

## Aufgabe

Führe ein Event Storming für die Domäne der Förderantragsverwaltung (ELER-Flächenantrag) durch.
Ziel ist es, die wichtigsten Geschäftsprozesse als zeitliche Abfolge von Domain Events zu modellieren und offene Fragen früh sichtbar zu machen.

Dieses Lab ist ein reines Modellierungs-Lab - es wird kein Code geschrieben.

## Zielbild für dieses Lab

Am Ende soll ein Board vorliegen, das folgende Fragen beantwortet:

- Welche fachlich relevanten Dinge passieren im Prozess?
- In welcher zeitlichen Reihenfolge passieren sie?
- Wo gibt es Unklarheiten, Konflikte oder Sprachwechsel?
- Welche 5-8 Events bilden den Kernablauf?

## Farbcodierung

Verwende folgende Farbcodierung für die Sticky Notes:

| Farbe  | Element         | Beispiel                                      |
|--------|-----------------|-----------------------------------------------|
| Orange | Domain Event    | "Antrag erfasst"                              |
| Rot    | Hot Spot        | "Wann ist ein Antrag eigentlich fertig?"      |
| Blau   | Command         | "Antrag einreichen"                           |
| Gelb   | Aggregate       | "Antragsmappe"                                |
| Lila   | Policy          | "Wenn Antrag eingereicht, dann Kontrolle starten" |
| Rosa   | External System | "Referenzflächen-Sync", "Poststelle"          |
| Grün   | Read Model      | "Antragsübersicht", "Monitoring-Dashboard"    |

## Anleitung

### Phase 1: Domain Events sammeln (15-20 Min)

Beginne mit den folgenden Einstiegs-Szenarien und notiere alle Domain Events, die
dir einfallen. Schreibe jedes Event auf ein orangefarbenes Sticky Note in der
Vergangenheitsform.

Trigger-Szenarien:

1. Ein Landwirt digitalisiert seine Flurstücke und reicht den ELER-Antrag ein.
   - Was passiert von der Erfassung bis zur Einreichung?

2. Die fachliche Prüfung des Antrags startet.
   - Was passiert bei den Kontrollen, Bonitätsprüfungen und Fristen?

3. Eine Auszahlung wird für einen bewilligten Antrag vorbereitet.
   - Was passiert von der Zahlungsanweisung bis zum Geldeingang?

4. Das Monitoring-Dashboard muss aktualisiert werden.
   - Wie fließen die Daten aus den verschiedenen Schritten zusammen?

Tipps:

- Arbeitet in dieser Phase möglichst still und parallel.
- Diskutiert **noch nicht** über technische Umsetzung oder Datenbanktabellen.
- **Denkt nicht zu lange nach** - schreibt zuerst Menge statt Perfektion.
- Auch Duplikate oder unscharfe Events sind erlaubt. Sortieren kommt später.

### Phase 2: Zeitlinie ordnen und Kernablauf markieren (15 Min)

Ordne die gesammelten Domain Events auf einer horizontalen Zeitlinie von links
(früh) nach rechts (spät). Stellt euch dafür den Ablauf in der echten Welt vor, nicht in der
Software. Gruppiere zusammengehörige Events und führt
Duplikate zusammen.

Markiert anschließend den Kernablauf:

- Welche 5-8 Events beschreiben den wichtigsten *Happy Path*?
- Wo beginnen oder enden fachliche Phasen?
- Wo scheint ein Verantwortungswechsel stattzufinden?

### Phase 3: Hot Spots markieren (10 Min)

Markiert offene Fragen, Konflikte und Unklarheiten mit roten Hot Spots.
Typische Fragen sind:

- Fehlt zwischen zwei Events ein fachlicher Schritt?
- Verwenden Beteiligte denselben Begriff unterschiedlich (z.B. "Antrag" vs. "Mappe")?
- Ist unklar, wer entscheidet oder welche Regel gilt?
- Ist unklar, wann ein Prozess in eine neue Phase übergeht?

Wichtig: Hot Spots werden nicht sofort gelöst. Sie sind ein bewusstes
Ergebnis des Labs und dienen als Vorbereitung für Lab 03.

Immer, wenn ihr anfangt, zu diskutieren – inne halten und einen Hot Spot setzen.

### Phase 4: Commands und Actors ergänzen (10 Min)

Ergänzt nun Commands und Actors:

- Command (blau): Welche Aktion hat das Event ausgelöst?
- Actor: Wer hat den Command ausgelöst? (z. B. Landwirt, Sachbearbeiter, System)

Notiert den Actor direkt auf dem Command oder daneben.

### Phase 5: Vertiefung

Steigt nun immer tiefer in die einzelnen Bereiche ein und definiert:

- Aggregate (gelb): Welches fachliche Objekt entscheidet über den Command
  und schützt Regeln?
- Policies (lila): Wo reagiert das System automatisch auf ein Event und löst
  den nächsten Schritt aus?
- External Systems (rosa): Welche externen Systeme sind beteiligt?
- Read Models (grün): Welche Ansichten oder Abfragen helfen einem Akteur bei
  einer Entscheidung?

### Phase 6: Dokumentation für Lab 03 (5 Min)

Ordne und dokumentiere die Ergebnisse in der folgenden Tabelle.

| Bereich | Ergebnis |
|---------|----------|
| Kernablauf | 5-8 wichtigste Domain Events in zeitlicher Reihenfolge |
| Hot Spots | Mindestens 3 offene Fragen oder Konflikte |
| Wichtige Commands + Actors | Welche Aktionen treiben den Kernablauf? |
| Sprachwechsel / Verantwortungswechsel | Wo ändert sich ein Begriff, ein Verantwortlicher oder eine Prozessphase? |
| Optionale Vertiefung | Relevante Aggregates, Policies oder externe Systeme an 2-3 Stellen |

Wenn ihr digital arbeitet, exportiert oder fotografiert das Board. Dieses
Artefakt wird in Lab 03 weiterverwendet.

## Beispiel-Events als Starthilfe

Hier sind ein Paar Events als Inspiration:

- Antrag erfasst
- Flurstücke digitalisiert
- Antrag eingereicht
- Fachliche Prüfung gestartet
- Kontrolle durchgeführt
- Antrag positiv beschieden
- Zahlung angewiesen
