# DDD & Clean Architecture mit Spring Boot 4

Workshop S2090 - GFU Cyrus AG | 5 Tage

Praxisorientierter Workshop für erfahrene Java-Entwickler und
Software-Architekten. Anhand einer durchgängigen Übungsdomäne -
der Förderantragsverwaltung (Landwirtschaft/ELER) - werden Domain-Driven Design und
Clean Architecture Schritt für Schritt mit Spring Boot 4 umgesetzt.

---

## Tagesübersicht

| Tag | Thema | Slides | Labs                                                                                         | Zeitplan |
|-----|-------|--------|----------------------------------------------------------------------------------------------|----------|
| 1 | Grundlagen & Domänenverständnis | 01-intro, 02-spring-boot-basics, 03-ddd-einfuehrung, 04-event-storming | Lab 01 (Setup und Warmup), Lab 02 & Lab 02b (Event Storming)                                 | Intro 45 Min → Spring Boot Recap 45 Min → DDD Einführung 90 Min → Event Storming 120 Min |
| 2 | Strategisches & Taktisches Design | 05-strategic-design, 06-building-blocks, 07-clean-architecture | Lab 03 (Strategic Design), Lab 04 (Building Blocks)| Strategic Design 90 Min → Building Blocks 120 Min → Clean Architecture 90 Min |
| 3 | Implementierung & Adapter | 08-paketstruktur, 09-use-cases-application-services, 10-rest-adapter | Lab 05 (Clean Architecture Refactoring), Lab 06 (Use Case), Lab 07 (REST Adapter)                               | Paketstruktur 60 Min → Use Cases 60 Min → REST Adapter 90 Min |
| 4 | Architektur-Governance & Integration | 11-archunit, 12-context-integration, 13-business-components-modulith, 14-querschnittsthemen | Lab 08 (ArchUnit), Lab 09 (Context Integration), Lab 10 (Modulith), Lab 11 (Querschnittsthemen) | ArchUnit 60 Min → Integration 60 Min → Modulith 90 Min → Querschnitt 60 Min |
| 5 | Testing, Vertiefung & Reflexion | 15-teststrategie | Lab 12 (Testing), Lab 13 (Freie Implementierung)                                             | Teststrategie 90 Min → Freie Implementierung 120 Min → Reflexion 60 Min |

---

## Voraussetzungen

| Werkzeug | Version | Hinweis |
|----------|---------|---------|
| JDK | 21+ | Empfohlen für den gesamten Workshop; erforderlich, um Virtual Threads in Lab 01 praktisch auszuprobieren |
| Maven | 3.9+ | `mvn -version` zur Prüfung |
| IDE | - | IntelliJ IDEA empfohlen (Community oder Ultimate) |
| Git | 2.x | Zum Klonen des Repositories |
| Docker | optional | Für optionale Kafka-Übung in Lab 13 |
| Browser | - | Für H2-Console und draw.io |

---

## Setup

```bash
# Repository klonen
git clone <repository-url>
cd workshop-ddd-clean-architecture

# Starter-Projekt bauen und starten
cd labs/lab-01-setup-und-warmup/initial-project
mvn clean verify
mvn spring-boot:run

# Health-Check
curl http://localhost:8080/actuator/health
# Erwartete Antwort enthält mindestens "status":"UP",
# z. B. {"groups":["liveness","readiness"],"status":"UP"}
```

---

## Projektstruktur

```
workshop-ddd-clean-architecture/
├── slides/                  # MARP Slide-Decks (Module 01-15)
│   ├── 01-intro/
│   ├── 02-spring-boot-basics/
│   ├── ...
│   └── template.html        # Marp HTML-Template
├── labs/                    # Hands-on Labs (01-13)
│   ├── lab-01-setup-und-warmup/  # Starter-Projekt + CRUD-Warmup
│   ├── lab-02-event-storming/
│   ├── ...
│   └── lab-13-freie-implementierung/
└── README.md                # Diese Datei
```

---

## Slides rendern (MARP)

Die Slide-Decks sind im [MARP](https://marp.app/)-Format geschrieben und
können als HTML oder PDF gerendert werden.

```bash
# MARP CLI installieren (einmalig)
npm install -g @marp-team/marp-cli

# Einzelnes Modul als HTML rendern
marp --html slides/01-intro/slides.md

# Einzelnes Modul als PDF rendern
marp --html --pdf slides/01-intro/slides.md

# Alle Module rendern
for dir in slides/*/; do
  if [ -f "$dir/slides.md" ]; then
    marp --html --pdf "$dir/slides.md"
  fi
done
```

---

## Diagramme bearbeiten

Die `.drawio`-Dateien in den `slides/*/images/`-Verzeichnissen können mit folgenden
Tools geöffnet und bearbeitet werden:

- [draw.io Desktop](https://github.com/jgraph/drawio-desktop/releases) -
  Offline-Anwendung für alle Plattformen
- [diagrams.net](https://app.diagrams.net/) - Online-Editor im Browser
- VS Code Extension - "Draw.io Integration" von Henning Dieterichs

Zum Einbetten in die Slides werden die Diagramme als PNG exportiert und unter
demselben Namen mit `.png`-Endung abgelegt (z. B.
`clean-architecture-ringe.drawio.png`).

---

## Übungsdomäne: Förderantragsverwaltung (Landwirtschaft/ELER)

Die durchgängige Übungsdomäne (Bearbeitung eines ELER-Flächenantrags) umfasst folgende Bounded Contexts:

| Bounded Context | Beschreibung |
|-----------------|-------------|
| Antragstellung | Anträge und Mappen erfassen, Flurstücke digitalisieren |
| Fachliche Prüfung | Kontrollen durchführen, Bonität und Fristen prüfen |
| Auszahlung / ZA | Auszahlungsanträge bearbeiten, Zahlungsbeträge berechnen |
| Auswertung / Monitoring | Analytics und Dashboarding von Antragsdaten |
| Bescheidversand | Erstellung und Versand von PDF-Bescheiden |
| Datenaustausch / Externe | Synchronisation von Referenzflächen und externen Systemen |

---

## Technologie-Stack

- Java 21+ (Records, Sealed Classes, Virtual Threads)
- Spring Boot 4.0.x (Jakarta EE 11, Spring Framework 7, Virtual Threads)
- Spring Data JPA + H2 (In-Memory-Datenbank)
- Bean Validation (jakarta.validation)
- ArchUnit (Architektur-Tests)
- JUnit 5 + Mockito (Testframework)
- Maven (Build-Tool)
- Kein Lombok - explizite Konstruktoren und Java Records

---

## Lizenz

Dieses Material ist für den internen Schulungsgebrauch bestimmt.
© 2026 - GFU Cyrus AG - Workshop S2090
