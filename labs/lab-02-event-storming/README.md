# Lab 02: Event Storming - Die Immobilien-CRM-Domäne erkunden

## Aufgabe

Führe ein Event Storming für die Immobilien-CRM-Domäne
durch. Ziel ist es, die wichtigsten Geschäftsprozesse eines Immobilienmaklers
als zeitliche Abfolge von Domain Events zu modellieren und offene Fragen
früh sichtbar zu machen.

Dieses Lab ist ein reines Modellierungs-Lab - es wird kein Code geschrieben.

## Zielbild für dieses Lab

Am Ende soll ein Board vorliegen, das folgende Fragen beantwortet:

- Welche fachlich relevanten Dinge passieren im Prozess?
- In welcher zeitlichen Reihenfolge passieren sie?
- Wo gibt es Unklarheiten, Konflikte oder Sprachwechsel?
- Welche 5-8 Events bilden den Kernablauf?

## Farbcodierung

Verwende folgende Farbcodierung für die Sticky Notes:

| Farbe  | Element         | Einsatz in diesem Lab                                 | Beispiel                                      |
|--------|-----------------|-------------------------------------------------------|-----------------------------------------------|
| Orange | Domain Event    | Pflicht - etwas ist passiert                      | "Immobilie erfasst"                           |
| Rot    | Hot Spot        | Pflicht - Unklarheit, Konflikt oder offene Frage  | "Wer darf den Angebotspreis anpassen?"        |
| Blau   | Command         | Optional - nur an wichtigen Stellen ergänzen          | "Immobilie erfassen"                          |
| Gelb   | Aggregate       | Optional - nur an wichtigen Stellen ergänzen          | "Immobilie"                                   |
| Lila   | Policy          | Optional - nur wenn klar erkennbar                    | "Wenn Auftrag erteilt, dann Expose erstellen" |
| Rosa   | External System | Optional - nur wenn beteiligt                         | "ImmoScout24", "Grundbuchamt"                 |
| Grün   | Read Model      | Optional - nur wenn es die Diskussion wirklich hilft  | "Immobilienliste", "Exposee-Ansicht"          |

## Anleitung

### Phase 1: Domain Events sammeln (15-20 Min)

Beginne mit den folgenden Trigger-Szenarien und notiere alle Domain Events, die
dir einfallen. Schreibe jedes Event auf ein orangefarbenes Sticky Note in der
Vergangenheitsform.

Trigger-Szenarien:

1. Ein Eigentümer ruft beim Makler an und möchte seine Immobilie verkaufen.
   - Was passiert von der ersten Kontaktaufnahme bis zur Auftragserteilung?

2. Ein Interessent findet ein Expose auf ImmoScout24 und meldet sich.
   - Was passiert von der Anfrage bis zur Besichtigung?

3. Ein Kaufinteressent gibt ein Angebot ab.
   - Was passiert von der Angebotsabgabe bis zum Abschluss oder Abbruch?

4. Der Makler bewertet eine Immobilie vor Ort.
   - Welche Schritte umfasst die Bewertung?

Tipps:

- Arbeitet in dieser Phase möglichst still und parallel.
- Diskutiert noch nicht über technische Umsetzung oder Datenbanktabellen.
- Denkt nicht zu lange nach - schreibt zuerst Menge statt Perfektion.
- Auch Duplikate oder unscharfe Events sind erlaubt. Sortieren kommt später.

### Phase 2: Zeitlinie ordnen und Kernablauf markieren (15 Min)

Ordne die gesammelten Domain Events auf einer horizontalen Zeitlinie von links
(früh) nach rechts (spät). Gruppiere zusammengehörige Events und führt
Duplikate zusammen.

Markiert anschließend den Kernablauf:

- Welche 5-8 Events beschreiben den wichtigsten Happy Path?
- Wo beginnen oder enden fachliche Phasen?
- Wo scheint ein Verantwortungswechsel stattzufinden?

### Phase 3: Hot Spots markieren (10 Min)

Markiert offene Fragen, Konflikte und Unklarheiten mit roten Hot Spots.
Typische Fragen sind:

- Fehlt zwischen zwei Events ein fachlicher Schritt?
- Verwenden Beteiligte denselben Begriff unterschiedlich?
- Ist unklar, wer entscheidet oder welche Regel gilt?
- Ist unklar, wann ein Prozess in eine neue Phase übergeht?

Wichtig: Hot Spots werden nicht sofort gelöst. Sie sind ein bewusstes
Ergebnis des Labs und dienen als Vorbereitung für Lab 03.

### Phase 4: Commands und Actors ergänzen (10 Min)

Ergänzt nur für die wichtigsten 5-8 Events des Kernablaufs:

- Command (blau): Welche Aktion hat das Event ausgelöst?
- Actor: Wer hat den Command ausgelöst? (z. B. Makler, Eigentümer,
  Interessent, System)

Notiert den Actor direkt auf dem Command oder daneben. Es ist kein eigenes
Board-Element nötig.

### Phase 5: Optionale Vertiefung an 2-3 Stellen (5-10 Min)

Wenn noch Zeit bleibt, vertieft 2-3 fachlich wichtige Stellen des Boards:

- Aggregate (gelb): Welches fachliche Objekt entscheidet über den Command
  und schützt Regeln?
- Policies (lila): Wo reagiert das System automatisch auf ein Event und löst
  den nächsten Schritt aus?
- External Systems (rosa): Welche externen Systeme sind beteiligt?
- Read Models (grün): Welche Ansichten oder Abfragen helfen einem Akteur bei
  einer Entscheidung?

Es ist nicht notwendig, für jedes Event bereits ein Design-Level-Modell zu
erzeugen. Fokus zuerst auf Domänenverständnis, dann auf ausgewählte Knoten im
Prozess.

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

- Eigentümer kontaktiert
- Immobilie erfasst
- Bewertung durchgeführt
- Maklerauftrag erteilt
- Expose erstellt

## Verifikation

Prüfe dein Ergebnis anhand folgender Kriterien:

- [ ] Mindestens 15 Domain Events identifiziert
- [ ] Events sind in der Vergangenheitsform formuliert
- [ ] Events sind auf einer Zeitlinie angeordnet
- [ ] Ein Kernablauf mit 5-8 Events ist markiert
- [ ] Mindestens 3 Hot Spots markiert
- [ ] Für die wichtigsten Kern-Events sind Commands und Actors ergänzt
- [ ] Hinweise auf Sprachwechsel, Verantwortungswechsel oder Phasenübergänge
  sind dokumentiert

## Hinweise

- Es gibt kein "richtig" oder "falsch" - Event Storming ist ein Werkzeug zur
  Erkundung der Domäne.
- Beginnt mit dem Happy Path und ergänzt später Fehlerfälle und Sonderfälle.
- Hot Spots sind wertvoll. Gerade dort verbergen sich Regeln,
  Verantwortungswechsel oder spätere Bounded Contexts.
- Versucht in diesem Lab nicht, schon eine perfekte Zielarchitektur zu
  entwerfen.
- Ziel von Lab 02 ist zuerst gemeinsames Domänenverständnis, nicht ein
  vollständiges Design-Level-Board.
- Die Ergebnisse dieses Labs bilden die Grundlage für Lab 03 (Strategic Design).
