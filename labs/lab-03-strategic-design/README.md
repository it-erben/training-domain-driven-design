# Lab 03: Strategic Design - Bounded Contexts und Context Map

Leite aus dem Event Storming Bounded Contexts ab und erstelle eine Context Map.
Nutze dafür die Ergebnisse aus Lab 02b (Förderantragsverwaltung) als Ausgangspunkt.

Dieses Lab ist ein reines Modellierungs-Lab - es wird kein Code geschrieben.

---

## Unser Ausgangsmaterial: Pivot Events aus Lab 02b

| Pivot Event | Grenze | Signal aus Lab 02b |
|-------------|--------|--------------------|
| `AntragsmappeEingereicht` | Antragstellung → Fachliche Prüfung | Akteurwechsel: Antragsteller → Sachbearbeiterin |
| `AntragPositivBeschieden` | Fachliche Prüfung → Auszahlung | Sprachgrenze: "AntragsMappe" → "Zahlungsantrag" |
| `ZahlungAngewiesen` | Auszahlung → Bescheidversand | Verantwortungswechsel: Zahlstelle → System |

### Hot Spots als Grenz-Kandidaten

- "Was ist 'fertig'?" — `AenderungsArt`-Inferenz = implizites State-Machine-Muster
- "Kontrolle: Teil der AntragsMappe?" — eigene Listener auf demselben Topic → Aggregate-Kandidat
- "Wer nimmt Entscheidung zurück?" — unklare Verantwortung → Bewilligungsstelle als Grenze

### Ausgefülltes Beispiel (Referenz für Phase 2)

| Subdomain | Typ | Bounded Context | Verantwortlichkeit | Gründe aus Lab 02b | Kern-Aggregate | Wichtigste Events |
|-----------|-----|-----------------|--------------------|--------------------|----------------|-------------------|
| Förderantrag stellen | **Core** | Antragstellung | AntragsMappe erfassen, Flurstücke digitalisieren | Pivot Event: `AntragsmappeEingereicht`; Akteurwechsel | AntragsMappe, Flurstück | AntragsmappeErstellt, FlurstückeDigitalisiert, AntragsmappeEingereicht |
| Fachliche Prüfung | **Core** | Fachliche Prüfung | Antrag prüfen, Kontrollen durchführen, Bescheid vorbereiten | Pivot Event: `AntragPositivBeschieden`; Sprachgrenze "Antrag" → "Bescheid" | Prüfvorgang, Kontrolle | FachlichePrüfungGestartet, KontrolleDurchgeführt, AntragPositivBeschieden |
| Zahlungsabwicklung | **Supporting** | Auszahlung | Zahlungsantrag anlegen, Kautionsverwaltung, Zahlung anweisen | Pivot Event: `ZahlungAngewiesen`; Verantwortungswechsel | Zahlungsantrag, Kautionsverwaltung | AuszahlungHinzugefügt, ZahlungAngewiesen |
| Bescheidversand | **Supporting** | Bescheidversand | Amtliche Bescheide erzeugen und versenden | Hot Spot: Polling statt Event-Listener | Bescheid | BescheidVersandt |
| Auswertung / Monitoring | **Supporting** | Auswertung | Dashboards, Reports, Monitoring der Antragsmengen | Konformist: reagiert reaktiv auf Events | MonitoringEintrag | (nur Listener, keine eigenen Events) |
| Referenzflächen | **Generic** | Referenzdaten | Katasterdaten, GIS-Daten bereitstellen | Open Host Service: keine BC-spezifische Logik | Flurstück-Stammdaten | — |

---

## Phase 1: Fachliche Teilbereiche und Bounded Contexts identifizieren (25 Min)

Betrachte die Ergebnisse des Event Stormings und arbeite in zwei Schritten:

1. Markiere zunächst fachliche Teilbereiche / Subdomains im Prozess.
2. Leite daraus **Bounded Contexts** ab und ziehe Modellgrenzen.

Nutze insbesondere die in Lab 02b dokumentierten Hot Spots, Sprachwechsel und
Verantwortungswechsel. Verwende dazu die folgenden Leitfragen:

**Leitfragen:**

1. Welche fachlichen Teilbereiche / Subdomains sind überhaupt erkennbar?
Welche fachlichen Probleme oder Verantwortungsräume stecken hinter den Events?
Welche davon wirken stabiler als eine einzelne Prozessphase?

2. Wo ändert sich die Bedeutung eines Begriffs?
Beispiel: "Antrag" bedeutet in der Antragstellung etwas anderes (AntragsMappe
mit Flurstücken und Förderbedingungen) als in der Auszahlung (Zahlungsantrag
mit Förderbeträgen und Kautionsverwaltung).

3. Welche sogenannten Pivot Events markieren fachliche Phasenübergänge?
Übergänge wie `AntragsmappeEingereicht` oder `AntragPositivBeschieden`
markieren oft eine neue Verantwortung oder ein anderes Modell.

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
- Nutze Hot Spots und Sprachwechsel aus Lab 02b bewusst als Kandidaten für
  Grenzen zwischen Contexts.
- Notiert pro Grenze kurz, welches Signal aus Lab 02b sie auslöst
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
| Core         | Differenziert euch am Markt, enthält eure wichtigsten Regeln       | Selbst bauen, beste Leute, höchste Qualität       |
| Supporting   | Nötig fürs Geschäft, aber kein Differenzierungsmerkmal             | Selbst bauen, pragmatisch, darf einfacher sein    |
| Generic      | Standardproblem, das viele Unternehmen gleich lösen                | Kaufen, SaaS nutzen oder Standardlösung einsetzen |

Diskutiert für jeden Teilbereich:

- Würde ein Wettbewerber dasselbe Problem genauso lösen, oder ist das eure
  fachliche Besonderheit?
- Könntet ihr diesen Bereich durch ein Standardprodukt ersetzen, ohne
  Wettbewerbsnachteil?

### Beschreibungstabelle (ausfüllen)

| Subdomain | Typ | Bounded Context | Verantwortlichkeit | Gründe aus Lab 02b | Wichtige fachliche Objekte / mögliche Kern-Aggregates | Wichtigste Events |
|-----------|-----|-----------------|--------------------|--------------------|-------------------------------------------------------|-------------------|
| *Welcher fachliche Bereich?* | Core / Supporting / Generic | *Name* | *Was ist die Aufgabe?* | *Warum ist hier eine Grenze?* | *Welche Objekte tragen das Modell?* | *Welche Events?* |

Die Spalte `Gründe aus Lab 02b` sollte auf konkrete Beobachtungen
verweisen, z. B. `Sprachwechsel: "Antrag"`, `Pivot Event: "AntragsmappeEingereicht"`
oder `Hot Spot: "Wer darf die Bewilligungsentscheidung zurücknehmen?"`.

> Das ausgefüllte Referenzbeispiel steht am Anfang dieser Datei.

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
| Published Language (PL)     | Ergänzende Kennzeichnung: Gemeinsames Austauschformat (z.B. JSON Schema)                    |
| Partnership                 | Zwei Teams entwickeln und entscheiden eng gemeinsam, ohne klare U/D-Hierarchie              |
| Separate Ways               | Bewusste Entscheidung gegen Integration; beide Seiten lösen das Problem getrennt            |

Tipps:

- Zeichne die Context Map als Diagramm mit Kästen (Bounded Contexts) und
  Pfeilen (Beziehungen).
- Beschrifte die Pfeile mit dem jeweiligen Pattern und der Richtung
  (Upstream/Downstream), falls relevant.
- Konzentriere dich auf die 3-5 wichtigsten Beziehungen und begründe diese
  kurz: Warum passt genau dieses Pattern?
- Markiere externe Systeme gesondert (z. B. EU-IACS, GIS-System, ZID).

### Referenz-Context-Map (Ist-Zustand)

```
  [Antragstellung]  ──Customer/Supplier──►  [Fachliche Prüfung]
                    [AntragsmappeGeaendert]
                                                    │
                                          Customer/Supplier
                                     [AntragPositivBeschieden]
                                                    ▼
                                            [Auszahlung]
                                           /            \
                                    sollte ACL sein    sollte ACL sein
                                   (heute: Conformist) (heute: Conformist)
                                          │                  │
                                    [Auswertung]       [Bescheidversand]

  [Referenzdaten] ──Open Host Service──► alle Contexts
  (EU-IACS/GIS)    Published Language
```

**Aufgabe:** Identifiziert in eurer eigenen Context Map, wo heute
"Conformist" steht, obwohl ein ACL nötig wäre.

## Phase 4: Vergleich und kurze Auswertung (10 Min)

Vergleicht eure Ergebnisse im Team oder mit einem anderen Tisch:

- Wo wart ihr euch bei den Grenzen sicher, wo nicht?
- Welche Grenze ist durch Lab-02b-Signale gut begründet?
- Wo habt ihr alternative Schnitte verworfen und warum?
- Welche Beziehung in eurer Context Map war am schwierigsten zu klassifizieren?

### Reflexion

- Wo haben wir heute Conformist, obwohl ein ACL fachlich notwendig wäre?
  (Überall, wo ein fremdes Domänenobjekt direkt importiert wird.)
- Was wäre das teuerste Refactoring? (Einen Klassennamen ändern, der von
  vielen Listenern per `messageSelector` referenziert wird.)
- Warum ist Polling für den Bescheidversand ein Zeichen für eine fehlende BC-Grenze?

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
