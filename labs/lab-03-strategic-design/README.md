# Lab 03: Strategic Design - Bounded Contexts und Context Map

## Lernziel

Bounded Contexts aus den Ergebnissen des Event Stormings ableiten und eine
Context Map erstellen, die die Beziehungen zwischen den Contexts beschreibt.

## Dauer

60 Minuten

## Voraussetzungen

- Lab 02 (Event Storming) abgeschlossen
- Ergebnisse des Event Stormings liegen vor (Domain Events, Aggregates,
  Policies)

## Aufgabe

Leite aus dem Event Storming Bounded Contexts ab und erstelle eine Context Map
für die Immobilien-CRM-Domäne.

**Dieses Lab ist ein reines Modellierungs-Lab - es wird kein Code geschrieben.**

## Anleitung

### Phase 1: Bounded Contexts identifizieren (25 Min)

Betrachte die Ergebnisse des Event Stormings und gruppiere die Domain Events zu
Bounded Contexts. Verwende dazu die folgenden Leitfragen:

**Leitfragen:**

1. **Wo ändert sich die Bedeutung eines Begriffs?**
Beispiel: "Immobilie" bedeutet in der Objektverwaltung etwas anderes (
Stammdaten, Flächen, Ausstattung) als in der Vermarktung (Expose-Texte,
Fotos, Zielgruppe).

2. **Welche Events gehören zusammen?**
Events, die immer gemeinsam auftreten oder eng zusammenhängen, gehören
wahrscheinlich zum selben Bounded Context.

3. **Wo gibt es unterschiedliche Experten?**
Wenn verschiedene Personen oder Abteilungen für bestimmte Themen zuständig
sind, deutet das auf getrennte Bounded Contexts hin.

4. **Was könnte unabhängig deployed werden?**
Teile des Systems, die sich unabhängig voneinander ändern und deployen
lassen, sind gute Kandidaten für eigene Bounded Contexts.

**Vorgehen:**

- Zeichne auf dem Event-Storming-Board Linien um die Gruppen von Events, die
  zusammengehören.
- Gib jedem Bereich einen sprechenden Namen.
- Prüfe, ob die Gruppen in sich schlüssig sind und klare Verantwortlichkeiten
  haben.

### Phase 2: Bounded Contexts beschreiben (15 Min)

Beschreibe jeden identifizierten Bounded Context kurz:

| Bounded Context | Verantwortlichkeit     | Kern-Aggregates      | Wichtigste Events |
|-----------------|------------------------|----------------------|-------------------|
| *Name*          | *Was ist die Aufgabe?* | *Welche Aggregates?* | *Welche Events?*  |

### Phase 3: Context Map erstellen (20 Min)

Erstelle eine Context Map, die zeigt, wie die Bounded Contexts miteinander in
Beziehung stehen.

**Beziehungstypen (DDD Context Mapping Patterns):**

| Pattern                         | Beschreibung                                                                                |
|---------------------------------|---------------------------------------------------------------------------------------------|
| **Shared Kernel**               | Gemeinsam genutzter Code/Modell, beide Teams müssen sich abstimmen                          |
| **Customer/Supplier**           | Downstream (Customer) hängt von Upstream (Supplier) ab, Supplier berücksichtigt Bedürfnisse |
| **Conformist**                  | Downstream übernimmt das Modell des Upstream ohne Anpassung                                 |
| **Anti-Corruption Layer (ACL)** | Downstream übersetzt das Upstream-Modell in das eigene Modell                               |
| **Open Host Service (OHS)**     | Upstream stellt eine definierte API/Schnittstelle bereit                                    |
| **Published Language (PL)**     | Gemeinsames Austauschformat (z.B. OpenImmo-XML)                                             |

**Vorlage für die Context Map:**

| Bounded Context           | Upstream/Downstream | Beziehung zu         | Pattern                  |
|---------------------------|---------------------|----------------------|--------------------------|
| Objektverwaltung          | Upstream            | Vermarktung          | Open Host Service        |
| Vermarktung               | Downstream          | Objektverwaltung     | Anti-Corruption Layer    |
| Kontaktmanagement         | Upstream            | Akquise/Auftrag      | Open Host Service        |
| Kontaktmanagement         | Upstream            | Vermittlungsprozess  | Open Host Service        |
| Akquise/Auftrag           | Downstream          | Kontaktmanagement    | Customer/Supplier        |
| Akquise/Auftrag           | Upstream            | Vermarktung          | Customer/Supplier        |
| Vermittlungsprozess       | Downstream          | Vermarktung          | Anti-Corruption Layer    |
| Vermittlungsprozess       | Downstream          | Kontaktmanagement    | Customer/Supplier        |
| Aktivitäten/Kommunikation | Downstream          | (alle anderen)       | Conformist               |
| Vermarktung               | -                   | ImmoScout24 (extern) | ACL + Published Language |

**Tipps:**

- Zeichne die Context Map als Diagramm mit Kästen (Bounded Contexts) und
  Pfeilen (Beziehungen).
- Beschrifte die Pfeile mit dem jeweiligen Pattern.
- Markiere externe Systeme gesondert.

## Verifikation

Prüfe dein Ergebnis anhand folgender Kriterien:

- [ ] Mindestens **4 Bounded Contexts** identifiziert
- [ ] Jeder Bounded Context hat eine **klare Verantwortlichkeit**
- [ ] Die **Beziehungen** zwischen den Bounded Contexts sind dokumentiert
- [ ] Mindestens **2 verschiedene Patterns** (z.B. ACL, OHS, Customer/Supplier)
  verwendet
- [ ] Die Context Map ist als **Diagramm oder Tabelle** dargestellt
- [ ] **Externe Systeme** sind in der Context Map berücksichtigt

## Hinweise

- Die Grenzen der Bounded Contexts sind nicht immer eindeutig. Diskutiert im
  Team und findet einen pragmatischen Schnitt.
- Achtet darauf, dass die Ubiquitous Language innerhalb eines Bounded Context
  konsistent ist, aber zwischen Bounded Contexts abweichen darf.
- Die Context Map ist ein lebendes Dokument - sie wird sich im Laufe des
  Projekts weiterentwickeln.
- Die Ergebnisse dieses Labs bilden die Grundlage für die Code-Labs ab Lab 04 (
  Building Blocks).
