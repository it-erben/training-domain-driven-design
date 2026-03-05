# Lab 03: Event Storming - Die Immobilien-CRM-Domäne erkunden

## Lernziel

Event Storming als Methode anwenden, um die Immobilien-CRM-Domäne zu erkunden. Domain Events, Commands, Aggregates, Policies und externe Systeme identifizieren.

## Dauer

60 Minuten

## Aufgabe

Führe ein Event Storming für die Immobilien-CRM-Domäne durch. Ziel ist es, die wichtigsten Geschäftsprozesse eines Immobilienmaklers als zeitliche Abfolge von Domain Events zu modellieren.

**Dieses Lab ist ein reines Modellierungs-Lab - es wird kein Code geschrieben.**

## Farbcodierung

Verwende folgende Farbcodierung für die Sticky Notes:

| Farbe | Element | Beschreibung | Beispiel |
|---|---|---|---|
| Orange | Domain Event | Etwas ist passiert (Vergangenheitsform) | "Immobilie erfasst" |
| Blau | Command | Eine Aktion, die ein Event auslöst | "Immobilie erfassen" |
| Gelb | Aggregate | Die Entität, auf die sich das Event bezieht | "Immobilie" |
| Lila | Policy | Eine Regel, die automatisch auf ein Event reagiert | "Wenn Auftrag erteilt, dann Expose erstellen" |
| Rosa | External System | Ein externes System | "ImmoScout24", "Grundbuchamt" |
| Grün | Read Model | Eine Ansicht/Abfrage | "Immobilienliste", "Exposee-Ansicht" |

## Anleitung

### Phase 1: Domain Events sammeln (20 Min)

Beginne mit den folgenden Trigger-Szenarien und notiere alle Domain Events, die dir einfallen. Schreibe jedes Event auf einen orangefarbenen Sticky Note in der **Vergangenheitsform**.

**Trigger-Szenarien:**

1. **Ein Eigentümer ruft beim Makler an und möchte seine Immobilie verkaufen.**
   - Was passiert von der ersten Kontaktaufnahme bis zur Auftragserteilung?

2. **Ein Interessent findet ein Expose auf ImmoScout24 und meldet sich.**
   - Was passiert von der Anfrage bis zur Besichtigung?

3. **Der Makler bewertet eine Immobilie vor Ort.**
   - Welche Schritte umfasst die Bewertung?

**Tipp:** Denke nicht zu lange nach - schreibe einfach alles auf, was dir einfällt. Sortieren kommt später.

### Phase 2: Zeitlinie ordnen (5-10 Min)

Ordne die gesammelten Domain Events auf einer horizontalen Zeitlinie von links (früh) nach rechts (spät). Gruppiere zusammengehörige Events.

### Phase 3: Commands und Aggregates zuordnen (10 Min)

Ordne jedem Domain Event zu:

- **Command (blau):** Welche Aktion hat das Event ausgelöst?
- **Aggregate (gelb):** Auf welche Entität bezieht sich das Event?
- **Actor:** Wer hat den Command ausgelöst? (Makler, Eigentümer, Interessent, System)

### Phase 4: Policies und externe Systeme (10 Min)

Identifiziere:

- **Policies (lila):** Wo reagiert das System automatisch auf ein Event?
- **External Systems (rosa):** Welche externen Systeme sind beteiligt?
- **Read Models (grün):** Welche Ansichten/Abfragen werden benötigt?

### Phase 5: Dokumentation (15 Min)

Ordne und dkumentiere die Ergebnisse in der folgenden Tabelle.

## Beispiel-Events als Starthilfe

Hier sind einige Domain Events als Inspiration - es gibt noch viele mehr:

- Eigentümer kontaktiert
- Immobilie erfasst
- Besichtigungstermin vereinbart
- Bewertung durchgeführt
- Maklerauftrag erteilt

## Verifikation

Prüfe dein Ergebnis anhand folgender Kriterien:

- [ ] Mindestens **15 Domain Events** identifiziert
- [ ] Events sind in der **Vergangenheitsform** formuliert
- [ ] Events sind auf einer **Zeitlinie** angeordnet
- [ ] Jedem Event ist mindestens ein **Aggregate** zugeordnet
- [ ] Mindestens **3 Policies** identifiziert
- [ ] Mindestens **2 externe Systeme** identifiziert
- [ ] Die wichtigsten **Actors** sind benannt (Makler, Eigentümer, Interessent, System)

## Hinweise

- Es gibt kein "richtig" oder "falsch" - Event Storming ist ein Werkzeug zur Erkundung der Domäne.
- Beginne mit dem "Happy Path" und ergänze später Fehlerfälle und Sonderfälle.
- Diskutiert im Team über unterschiedliche Begriffe - genau da verstecken sich oft die Bounded Contexts (siehe Lab 04).
- Die Ergebnisse dieses Labs bilden die Grundlage für Lab 04 (Strategic Design).
