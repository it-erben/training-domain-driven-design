# Lab 03: Strategic Design - Bounded Contexts und Context Map

Leite aus dem Event Storming Bounded Contexts ab und erstelle eine Context Map
für die Immobilien-CRM-Domäne. Nutze dafür die Ergebnisse aus Lab 02 als
Ausgangspunkt.

Dieses Lab ist ein reines Modellierungs-Lab - es wird kein Code geschrieben.

Einordnung im Kurs: Ziel dieses Labs ist es, die fachliche Landschaft
sichtbar zu machen.

## Phase 1: Fachliche Teilbereiche und Bounded Contexts identifizieren (25 Min)

Betrachte die Ergebnisse des Event Stormings und arbeite in zwei Schritten:

1. Markiere zunächst fachliche Teilbereiche / Subdomains im Prozess.
2. Leite daraus Bounded Contexts ab und ziehe Modellgrenzen.

Nutze insbesondere die in Lab 02 dokumentierten Hot Spots, Sprachwechsel und
Verantwortungswechsel. Verwende dazu die folgenden Leitfragen:

Leitfragen:

1. Welche fachlichen Teilbereiche / Subdomains sind überhaupt erkennbar?
Welche fachlichen Probleme oder Verantwortungsräume stecken hinter den Events?
Welche davon wirken stabiler als eine einzelne Prozessphase?

2. Wo ändert sich die Bedeutung eines Begriffs?
Beispiel: "Immobilie" bedeutet in der Objektverwaltung etwas anderes (
Stammdaten, Flächen, Ausstattung) als in der Vermarktung (Expose-Texte,
Fotos, Zielgruppe).

3. Welche sogenannten Pivot Events markieren fachliche Phasenübergänge?
Übergänge wie "Auftrag erteilt", "Expose veröffentlicht" oder "Angebot
angenommen" markieren oft eine neue Verantwortung oder ein anderes Modell.

4. Wo gibt es unterschiedliche Experten oder Akteure?
Wenn verschiedene Personen oder Abteilungen für bestimmte Themen zuständig
sind, deutet das auf getrennte Bounded Contexts hin.

5. Welche Daten und Entscheidungen ändern sich gemeinsam?
Wenn bestimmte Informationen, Regeln und Entscheidungen fachlich eng
zusammengehören, sollten sie meist im selben Bounded Context liegen.

6. Welcher Bereich hätte eine eigene fachliche Verantwortung?
Überlege, welcher Fachbereich oder welches Team diesen Bereich sinnvoll
eigenständig verantworten könnte. Bounded Contexts orientieren sich an
Modellgrenzen, nicht zwingend an separaten Deployments.

Vorgehen:

- Markiert zuerst grob fachliche Teilbereiche / Subdomains auf dem
  Event-Storming-Board.
- Zeichne anschließend Linien um die Gruppen von Events, die zu einem
  Bounded Context zusammengehören.
- Gib jedem Bereich einen sprechenden Namen.
- Nutze Hot Spots und Sprachwechsel aus Lab 02 bewusst als Kandidaten für
  Grenzen zwischen Contexts.
- Notiert pro Grenze kurz, welches Signal aus Lab 02 sie auslöst
  (`Sprachwechsel`, `Pivot Event`, `Verantwortungswechsel`, `Hot Spot`).
- Prüfe, ob die Gruppen in sich schlüssig sind und klare Verantwortlichkeiten
  haben.
- Prüfe kritisch, ob ihr gerade eine Subdomain modelliert oder nur eine
  Prozessphase ausgeschnitten habt.

## Phase 2: Bounded Contexts beschreiben und Subdomains klassifizieren (20 Min)

Beschreibe jeden identifizierten Bounded Context kurz und ordne die zugehörige
Subdomain einer strategischen Kategorie zu.

### Subdomain-Klassifikation

| Kategorie        | Kennzeichen                                                    | Strategische Konsequenz                           |
|------------------|----------------------------------------------------------------|---------------------------------------------------|
| Core         | Differenziert euch am Markt, enthält eure wichtigsten Regeln   | Selbst bauen, beste Leute, höchste Qualität       |
| Supporting   | Nötig fürs Geschäft, aber kein Differenzierungsmerkmal         | Selbst bauen, pragmatisch, darf einfacher sein    |
| Generic      | Standardproblem, das viele Unternehmen gleich lösen            | Kaufen, SaaS nutzen oder Standardlösung einsetzen |

Diskutiert für jeden Teilbereich:

- Würde ein Wettbewerber dasselbe Problem genauso lösen, oder ist das eure
  fachliche Besonderheit?
- Könntet ihr diesen Bereich durch ein Standardprodukt ersetzen, ohne
  Wettbewerbsnachteil?

### Beschreibungstabelle

| Subdomain | Typ | Bounded Context | Verantwortlichkeit | Gründe aus Lab 02 | Wichtige fachliche Objekte / mögliche Kern-Aggregates | Wichtigste Events |
|-----------|-----|-----------------|--------------------|--------------------|-------------------------------------------------------|-------------------|
| *Welcher fachliche Bereich?* | Core / Supporting / Generic | *Name* | *Was ist die Aufgabe?* | *Warum ist hier eine Grenze?* | *Welche Objekte tragen das Modell?* | *Welche Events?* |

Die Spalte `Gründe aus Lab 02` sollte auf konkrete Beobachtungen
verweisen, z. B. `Sprachwechsel: "Immobilie"`, `Pivot Event: "Auftrag erteilt"`
oder `Hot Spot: "Wer darf den Angebotspreis ändern?"`.

## Phase 3: Context Map erstellen (25 Min)

Erstelle eine Context Map, die zeigt, wie die Bounded Contexts miteinander in
Beziehung stehen.

Beziehungstypen (DDD Context Mapping Patterns):

| Pattern                         | Beschreibung                                                                                |
|---------------------------------|---------------------------------------------------------------------------------------------|
| Shared Kernel               | Gemeinsam genutzter Code/Modell, beide Teams müssen sich abstimmen                          |
| Customer/Supplier           | Downstream (Customer) hängt von Upstream (Supplier) ab, Supplier berücksichtigt Bedürfnisse |
| Conformist                  | Downstream übernimmt das Modell des Upstream ohne Anpassung                                 |
| Anti-Corruption Layer (ACL) | Downstream übersetzt das Upstream-Modell in das eigene Modell                               |
| Open Host Service (OHS)     | Ergänzende Kennzeichnung: Upstream stellt eine definierte API/Schnittstelle bereit          |
| Published Language (PL)     | Ergänzende Kennzeichnung: Gemeinsames Austauschformat (z.B. OpenImmo-XML)                   |
| Partnership                 | Zwei Teams entwickeln und entscheiden eng gemeinsam, ohne klare U/D-Hierarchie              |
| Separate Ways               | Bewusste Entscheidung gegen Integration; beide Seiten lösen das Problem getrennt            |

Tipps:

- Zeichne die Context Map als Diagramm mit Kästen (Bounded Contexts) und
  Pfeilen (Beziehungen).
- Beschrifte die Pfeile mit dem jeweiligen Pattern und der Richtung
  (Upstream/Downstream), falls relevant.
- Konzentriere dich auf die 3-5 wichtigsten Beziehungen und begründe diese
  kurz: Warum passt genau dieses Pattern?
- Markiere externe Systeme gesondert.

## Phase 4: Vergleich und kurze Auswertung (10 Min)

Vergleicht eure Ergebnisse im Team oder mit einem anderen Tisch:

- Wo wart ihr euch bei den Grenzen sicher, wo nicht?
- Welche Grenze ist durch Lab-02-Signale gut begründet?
- Wo habt ihr alternative Schnitte verworfen und warum?
- Welche Beziehung in eurer Context Map war am schwierigsten zu klassifizieren?

## Ergebnis

Prüfe dein Ergebnis anhand folgender Kriterien:

- [ ] Jede Subdomain ist als Core, Supporting oder Generic
  klassifiziert
- [ ] Für jeden Bounded Context sind Verantwortlichkeit, wichtige fachliche
  Objekte, wichtige Events und eine kurze Abgrenzung zu anderen BCs dokumentiert
- [ ] Die Herleitung nutzt sichtbar Ergebnisse aus Lab 02, insbesondere
  Hot Spots, Sprachwechsel oder Verantwortungswechsel
- [ ] Die Context Map enthält die 3-5 wichtigsten Beziehungen zwischen den
  Contexts
- [ ] Für jede Beziehung ist genau ein primäres Beziehungsmuster benannt
- [ ] Bei gerichteten Beziehungen ist kenntlich, was Upstream und
  Downstream ist
- [ ] Externe Systeme sind, falls vorhanden, gesondert markiert

## Tipps

- Die Grenzen der Bounded Contexts sind nicht immer eindeutig. Diskutiert im
  Team und findet einen pragmatischen Schnitt.
- Achtet darauf, dass die Ubiquitous Language innerhalb eines Bounded Context
  konsistent ist, aber zwischen Bounded Contexts abweichen darf.
- In Phase 2 reicht es, mögliche Kern-Aggregates bzw. wichtige fachliche
  Objekte zu benennen. Die taktische Ausarbeitung folgt erst in den
  nachfolgenden Labs.
- Nutzt die Begriffe Subdomain und Bounded Context bewusst
  unterschiedlich: Subdomains beschreiben fachliche Problemräume, Bounded
  Contexts beschreiben bewusst geschnittene Modellgrenzen.
- Ein Bounded Context ist in erster Linie eine Modell- und
  Verantwortungsgrenze, nicht automatisch ein eigener Microservice.
