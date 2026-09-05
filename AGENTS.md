# Arbeitsregeln

## Ton

- Knapp. Sag, was zu sagen ist, dann Schluss. Kein Vorgeplänkel, keine
  Zusammenfassung des gerade Getanen, kein „gute Frage“, kein Wiederholen der
  Aufgabe.
- Keine Füll-Adjektive (robust, nahtlos, mächtig, umfassend, produktionsreif).
  Knapp sagen, was der Code tut, nicht wie gut er ist. Nicht paraphrasieren, was
  die nächsten Zeilen tun. Stattdessen das WARUM und WIE erklären, wenn das dem
  Verständnis wirklich hilft.
- Docs und READMEs: was es ist, wie man es nutzt, was es bereitstellt. Sonst
  nichts.
- Commit-Nachrichten: conventional-commit, Imperativ, möglichst einzeilig. Den
  Scope richtig wählen — Release-Tooling routet unter Umständen darüber. Breaking
  Changes bekommen ein `!` (`feat(api)!: …`) oder einen `BREAKING CHANGE:`-Footer.
  Betreffzeile ≤ 72 Zeichen, Imperativ („add“, „fix“, nicht „added“, „fixes“).
  Body auf 72 Zeichen umbrechen.
- Kleine, fokussierte Commits bevorzugen. Release-Tooling leitet Versionssprünge
  und Changelog oft aus den Commit-Betreffzeilen ab.
- Keine Ticket-Nummern in Code, Commits oder Docs.
- Kommentare erklären das *Warum*, nicht das *Was*. Code-Kommentare benennen die
  Absicht oder eine Einschränkung, die der Code nicht zeigen kann. Kommentare
  löschen, die den Code nur wiederholen.
- Kommentare und Docs immer als Ganzes betrachten. Nie nur anhängen. Im Kontext
  prüfen und auf den faktischen Stand bringen. Im Zweifel im Code recherchieren.
  Veraltete und aus dem Kontext gefallene Verweise entfernen, ebenso frühere
  Beobachtungen, Schilderungen von Situationen, die zu einer früheren Änderung
  führten, Maschinennamen oder -adressen sowie jede Vermutung über die
  nachgelagerte Nutzung dieses Repos und seiner Artefakte — abgesehen von
  gültigen, aktuellen Beispielen.
- Auf ein anderes Repository oder Projekt nur verweisen, wenn dessen Zustand der
  unmittelbare Grund für die Änderung ist (ein Dependency-Bump, ein eingespielter
  Fix, ein an eine veröffentlichte Version gebundener API-Vertrag). Kontext für
  Reviewer, Dank oder Querverweise gehören in den PR-Thread oder ein Issue, nicht
  in den Commit.
- Deklarative Fakten schreiben. Keine Personalpronomen („ich“, „wir“, „du“).
  Keine Leseransprache: kein „beachte, dass…“, „wie man sieht…“, „wir haben uns
  entschieden…“, „das sollte helfen…“. Die Regel gilt für Dokumentation, die
  ein Artefakt beschreibt. Ausgenommen sind Folien und Lab-Anleitungen, siehe
  unten.
- Nicht erzählen. Keine Historie, was zuerst versucht wurde, was scheiterte oder
  welche Alternativen erwogen wurden.
- Keine Füll-Verben ohne Konkretes. „Aufräumen“, „verbessern“, „refactoren“
  allein sagen nichts; entweder die tatsächliche Änderung benennen oder die Zeile
  weglassen.
- Keine Checklisten, keine „Summary“-/„Test plan“-Abschnitte, keine
  Marketing-Sprache, keine Emojis.

## Folien

Marp-Decks unter `slides/<NN-thema>/slides.md`. Sie sind Lehrmaterial und
heben die Pronomen- und Leseransprache-Regel auf: „wir“ für das gemeinsame
Vorgehen, „ihr/euch“ für die Teilnehmer. Nicht siezen.

- Frontmatter unverändert übernehmen: `header: "DDD & Clean Architecture mit
  Spring Boot 4"`, `footer: "CC BY-NC-SA 4.0, Alexander Erben"`,
  `paginate: true`.
- Ein Deck beginnt mit `# Modul NN - Titel`, optional einem `##`-Untertitel,
  danach `## Lernziele` als Infinitivliste („… unterscheiden können“).
- Neun der fünfzehn Decks schließen mit `## Zusammenfassung`. Wer ein Deck
  wesentlich erweitert, aktualisiert diese Folie mit.
- Schriftgröße pro Folie über `<style scoped>section { font-size: 1.5em; }
  </style>` direkt unter dem Folientrenner. Dieses Repo kennt keine
  Dichte-Klassen; nicht welche einführen.
- Alle Beispiele spielen im Immobilien-CRM. Bounded Contexts sind
  `de.realestate.brokerage` (Hauptkontext), `de.realestate.acquisition` und
  `de.realestate.property`. Keine Fremddomäne einstreuen.
- Tabellen bilden Konzept auf Java-Umsetzung ab (Baustein → Zweck →
  Java-Umsetzung), nicht Feature-Listen.
- Farbige Sticky-Note-Emojis (🟨 🟧 🟦 🟪) nur in der Zuordnung zum Event
  Storming, wo die Farbe die Information ist. Sonst keine Emojis.
- Code zeigt die fertige Struktur, darunter zwei bis drei Bullets, die sagen,
  warum sie so aussieht — nicht, was dasteht.
- Listenzeichen ist `-`. Im gesamten `slides/`-Baum kommt kein `*` als
  Aufzählungszeichen vor.
- `## Diskussion`-Folien stellen eine offene Frage an die Gruppe und geben
  keine Antwort.

## Lab-Anleitungen

Ein Lab ist genau eine `labs/lab-NN-thema/README.md`.

- Anrede im Imperativ ohne Pronomen: „Implementiere“, „Erstelle“, „Refactore“,
  „Schreibe“. Wo ein Pronomen nötig ist, „ihr/euch“.
- Erster Absatz nennt in ein bis zwei Sätzen, was entsteht und woran es
  anschließt („Refactore den Code aus Lab 04 in die folgende Paketstruktur“).
- Danach entweder `## Schritt N: …` für Implementierungslabs oder `## Aufgabe`
  für Modellierungslabs ohne Code.
- Jeder Schritt nennt vollqualifizierte Packages und die exakten Typnamen, die
  entstehen sollen. Kein fertiger Code — das Lab beschreibt das Ziel, nicht die
  Lösung.
- Abläufe als nummerierte Liste: laden, Domain-Methode rufen, speichern.
- Testerwartungen als nummerierte Fälle mit `→` für den erwarteten Ausgang:
  `*Happy Path:* Prozess existiert → Viewing wird erstellt → save() wird
  aufgerufen`.
- Ein abschließender `## Hinweise`-Block trägt das, was die Teilnehmer beim
  Umsetzen falsch machen würden, mit Begründung („Die Use Cases sind bewusst
  schlank — die Geschäftslogik liegt im Domain-Modell“).
- Knapp auf Satzebene gilt weiterhin: keine Füll-Adjektive, kein Marketing,
  keine Zusammenfassung des Abschnitts darüber.

## Vor dem Abschluss

- Lint, Tests und Build des Projekts für alles Berührte ausführen.
- `pre-commit run --all-files` und `mvn -q verify` laufen lassen.
- Nicht „fertig“ behaupten, ohne die Prüfung ausgeführt zu haben. Belege vor
  Behauptungen.
- Alle TODO-Marker entfernen, die du in deiner Sitzung hinzugefügt hast, und
  nacharbeiten — oder dem Nutzer sagen, dass ein Follow-up nötig ist. Alle Marker
  und Verweise auf deine eigene Aufgabenliste oder historische Arbeitsschritte
  (P2, P3a, Item 1, Task A usw.) samt ihrer Erzählung entfernen. Wenn wirklich
  etwas offen bleibt, dem Nutzer außerhalb von Code, Docs, Markdown, Kommentaren,
  PR-Beschreibungen, Commit-Nachrichten oder allem anderen in diesem Repo und
  seiner angeschlossenen Pipeline Bescheid geben.

## Aufbau dieses Repos

Fünftägiger Workshop, eine durchgängige Domäne: ein Immobilien-CRM für Makler.

- `slides/01-intro` bis `slides/15-teststrategie` — fünfzehn Marp-Decks, die
  Nummer entspricht der Reihenfolge im Ablauf, nicht dem Tag.
- `labs/lab-01-…` bis `labs/lab-13-…` (mit `lab-02b`) — vierzehn
  Aufgabenstellungen.
- `solutions/lab-NN-…` — Referenzlösungen als eigenständige Maven-Module.
- Die Tagesaufteilung, welche Decks und Labs zusammengehören, steht in der
  Wurzel-`README.md` und ist die einzige Stelle, an der sie steht. Ein neues
  Deck oder Lab dort eintragen.

Das Wurzel-POM `immobilien-crm-aggregator` aggregiert
`labs/lab-01-setup-und-warmup/initial-project` und die acht Solutions. Neue
Solutions dort als Modul ergänzen, sonst baut die CI sie nicht.

## Fallstricke dieses Repos

- **Nur acht der vierzehn Labs haben eine Solution.** Ohne Lösung sind
  `lab-02`, `lab-02b` und `lab-03` (Modellierung auf Papier, kein Code) sowie
  `lab-11`, `lab-12` und `lab-13`. Bei 11 und 12 fehlt sie, obwohl es
  Code-Labs sind; 13 ist offen gestellt und bekommt bewusst keine.
- **Die Labs sind in der Gliederung uneinheitlich.** Sechs nutzen
  `## Schritt N`, andere `## Teil N` oder ein einzelnes `## Aufgabe`. Beim
  Bearbeiten eines Labs dessen eigenes Schema fortführen, statt es einseitig
  umzustellen.
- **`## Lernziele` steht mal als `##`, mal als `###`** (zehn zu fünf). Die
  Ebene des jeweiligen Decks beibehalten.
- **markdownlint läuft hier als `markdownlint`, nicht als `markdownlint-cli2`**,
  mit `-c .markdownlint.yaml`. Die Konfiguration ist auf Marp zugeschnitten
  (MD013, MD024, MD033, MD036 aus). Sie gilt für den ganzen Baum, also auch
  für Lab-READMEs — dort trotzdem bei rund 80 Zeichen umbrechen.
- **Die CI baut mehr als Lint.** `.gitlab-ci.yml` bindet `pdf-publisher`,
  `maven/verify`, `release` und `training-deploy` ein. Eine Änderung an
  `slides/` erzeugt ein PDF und ein Deployment, ein Commit mit `feat:` oder
  `fix:` einen Release-Tag.
- **`public/` ist leer und `target/` gitignoriert.** Keins von beidem als
  Ablage benutzen.
- **Die CI läuft auf zwei Plattformen.** `.gitlab-ci.yml` bindet die
  GitLab-Komponenten ein, `.github/workflows/ci.yml` ruft `lint.yml`,
  `slides.yml`, `maven.yml`, `release.yml` und `pages.yml` aus
  `it-erben/ci`. Die PDFs gehen
  dort auf GitHub Pages, ein Deployment gibt es auf GitHub nicht.
