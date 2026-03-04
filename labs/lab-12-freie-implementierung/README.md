# Lab 12: Freie Implementierung -- Vertiefung und Erweiterung

## Lernziel

Gelerntes eigenstaendig anwenden und vertiefen.

## Dauer

120 Minuten

## Voraussetzungen

- Labs 00-11 abgeschlossen

## Aufgabenstellung

Waehle eine oder mehrere der folgenden Vertiefungsoptionen und implementiere sie eigenstaendig.

### Option 1: Weiterer Use Case im eigenen BC

Implementiere den `AngebotEntgegennehmenUseCase` mit Command, Application Service, REST-Endpoint und Tests.

**Flow:**

1. `POST /api/vermittlungsvorgaenge/{id}/angebote` mit Angebotsbetrag und Interessent
2. Command-Objekt `AngebotEntgegennehmenCommand(UUID vermittlungsvorgangId, String interessentName, BigDecimal betrag)`
3. Application Service laedt den Vermittlungsvorgang, ruft `angebotEntgegennehmen()` auf und speichert
4. REST-Endpoint gibt 201 mit der Angebots-ID zurueck
5. Tests: Unit-Test fuer Use Case, WebMvcTest fuer Controller

**Schwierigkeit:** :star:

### Option 2: Anti-Corruption Layer -- OpenImmo-XML

Erstelle einen Adapter, der ein (simuliertes) OpenImmo-XML-Dokument entgegennimmt und in die Domain-Objekte der Objektverwaltung uebersetzt.

**Schritte:**

1. Definiere ein einfaches OpenImmo-XML-Format (z.B. `<openimmo><anbieter><immobilie><geo><plz>50667</plz><ort>Koeln</ort></geo></immobilie></anbieter></openimmo>`)
2. Erstelle einen `OpenImmoTranslationService` im Package `adapter.acl`
3. Verwende JAXB oder einfaches String-Parsing, um das XML in Domain-Objekte zu uebersetzen
4. Schreibe Tests, die die korrekte Uebersetzung pruefen

**Schwierigkeit:** :star::star:

### Option 3: CQRS mit separatem Read Model

Erstelle ein separates Read Model `VermittlungsvorgangUebersicht`, das fuer Listenansichten optimiert ist.

**Schritte:**

1. Erstelle ein Read Model `VermittlungsvorgangUebersicht` als eigene `@Entity` (oder Spring Data JPA Projection)
2. Felder: `id`, `adresse` (als String), `status`, `anzahlBesichtigungen`, `anzahlAngebote`
3. Implementiere einen dedizierten `VermittlungsvorgangQueryService`, der das Read Model abfragt
4. Erstelle einen separaten REST-Endpoint `GET /api/vermittlungsvorgaenge/uebersicht`

**Schwierigkeit:** :star::star:

### Option 4: Spring Modulith Integration

Fuege Spring Modulith hinzu und nutze es zur Pruefung und Verbesserung der Modul-Grenzen.

**Schritte:**

1. Fuege `spring-modulith-starter-core` als Dependency hinzu
2. Definiere `@ApplicationModule` fuer Akquise und Vermittlung
3. Erstelle einen `ModulithVerificationTest`, der die Modul-Grenzen prueft
4. Ersetze den manuellen `@EventListener` durch Spring Modulith's Event Publication Registry

**Schwierigkeit:** :star::star::star:

### Option 5: Kafka-Anbindung skizzieren

Skizziere eine Kafka-basierte Event-Kommunikation zwischen Bounded Contexts.

**Schritte:**

1. Fuege `spring-kafka` als Dependency hinzu
2. Erstelle einen Outbound-Adapter `KafkaVermittlungsvorgangEventPublisher` (Interface + Klasse), der Domain Events auf ein Kafka-Topic schreibt
3. Erstelle einen Inbound-Adapter `KafkaAkquiseEventConsumer`, der Events von Kafka empfaengt
4. Konfiguriere Kafka in `application.yml`
5. **Hinweis:** Es wird kein laufender Kafka-Broker benoetigt -- es reicht eine Skizze mit den richtigen Annotationen und Konfigurationen

**Schwierigkeit:** :star::star::star:

## Hinweise

- Es gibt keine Musterloesung -- die Aufgabe dient der eigenstaendigen Vertiefung
- Nutze die Patterns und Strukturen aus den vorherigen Labs als Vorlage
- Bei Fragen: Trainer ansprechen

## Verifikation

- Code kompiliert: `mvn compile`
- Tests sind gruen: `mvn test`
- Clean Architecture Regeln (ArchUnit) werden eingehalten
