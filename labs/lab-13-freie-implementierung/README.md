# Lab 13: Freie Implementierung - Vertiefung und Erweiterung

Wähle eine oder mehrere der folgenden Vertiefungsoptionen und implementiere sie eigenständig.

## Kontrolle durchführen - ein weiterer Use Case (einfach)

Implementiere den `KontrolleDurchfuehrenUseCase` mit Command, Application Service,
REST-Endpoint und Tests. Der Flow im Überblick:

1. `POST /api/pruefung/pruefvorgaenge/{id}/kontrollen` mit Kontrolleur-Name und Ergebnis
2. Command-Objekt `KontrolleDurchfuehrenCommand(UUID pruefvorgangId, String kontrolleur, KontrollErgebnis ergebnis)`
3. Application Service lädt den `Pruefvorgang`, ruft `kontrolleDurchfuehren()` auf und speichert
4. REST-Endpoint gibt 201 mit der Kontroll-ID zurück
5. Tests: Unit-Test für Use Case, WebMvcTest für Controller

## Anti-Corruption Layer für externe Referenzdaten (mittel)

Externe Systeme liefern Daten in fremden Formaten. Erstelle einen Adapter, der
ein (simuliertes) IACS/InVeKoS-Datenformat entgegennimmt und in die
Domain-Objekte der Antragstellung übersetzt.

Definiere dazu ein einfaches XML-Format (z.B. Flurstücksdaten mit
Gemarkung, Flur, Zähler/Nenner), erstelle einen `InVeKoSTranslationService`
im Package `adapter.acl` und verwende JAXB oder einfaches String-Parsing für
die Übersetzung. Vergiss die Tests nicht.

```java
// ACL: externe Referenzdaten → eigene Value Objects
@Component
class InVeKoSTranslator {
    Flurstück translate(InVeKoSFlurstueckDto dto) {
        return new Flurstück(
            new FlurstueckNummer(dto.getGemarkung(), dto.getFlur(),
                dto.getZaehler(), dto.getNenner()),
            new Flaeche(dto.getGroesse(), "ha")
        );
    }
}
```

## CQRS: Separates Monitoring-Read-Model (mittel)

Auswertungs-Dashboards brauchen andere Daten als Antragstellung-Schreiboperationen.
Erstelle ein separates Read Model `MonitoringUebersicht` als eigene `@Entity`
(oder Spring Data JPA Projection) mit den Feldern `registrierungsNummer`,
`status`, `letzteAenderung`, `anzahlKontrollen`, `bescheidVersandt`.

Dazu gehören ein dedizierter `MonitoringQueryService` und ein separater
REST-Endpoint `GET /api/auswertung/monitoring/uebersicht`.

Aktualisiere das Read Model über einen `@EventListener`, der auf
`AntragsmappeGeaendert` reagiert:

```java
@Component @RequiredArgsConstructor
class MonitoringUebersichtProjection {
    private final MonitoringUebersichtRepository repository;

    @EventListener
    void on(AntragsmappeGeaendert event) {
        var uebersicht = repository
            .findByRegistrierungsNummer(event.registrierungsNummer())
            .orElseGet(() -> MonitoringUebersicht.erstellen(
                event.registrierungsNummer()));
        uebersicht.aktualisiere(
            MonitoringsStatus.from(event.aenderungsArt()),
            event.geaendertAm());
        repository.save(uebersicht);
    }
}
```

## Kafka-Anbindung skizzieren (schwer)

Wie sähe eine Kafka-basierte Event-Kommunikation zwischen Bounded Contexts aus?
Füge `spring-kafka` als Dependency hinzu und erstelle sowohl einen
Outbound-Adapter als auch einen Inbound-Adapter:

### Outbound — Antragstellung publiziert auf Kafka

```java
@Component @RequiredArgsConstructor
class KafkaAntragMappeEventPublisher {
    private final KafkaTemplate<String, AntragsmappeEingereicht> kafkaTemplate;
    private static final String TOPIC = "antragstellung.antragsmappe.eingereicht.v1";

    @TransactionalEventListener(phase = AFTER_COMMIT)
    void publish(AntragsmappeEingereicht event) {
        kafkaTemplate.send(TOPIC, event.registrierungsNummer(), event);
    }
}
```

### Inbound — Prüfung konsumiert

```java
@Component @RequiredArgsConstructor
class KafkaPruefungEventConsumer {
    private final AntragstellungEventTranslator translator;
    private final PruefungStartenService service;

    @KafkaListener(topics = "antragstellung.antragsmappe.eingereicht.v1",
                   groupId = "pruefung")
    void consume(AntragsmappeEingereicht event) {
        service.start(translator.translate(event));
    }
}
```

### Evolutionsstufen — DDD-getrieben, nicht infrastruktur-getrieben

```
Stufe 1 — Ist-Zustand: JMS/EJB (Conformist, kein ACL)
  @MessageDriven + ObjectMessage + JMS Topic
  Problem: kein Translator, kein eigenes Modell, kein Idempotenz-Check

Stufe 2 — DDD nachrüsten (Transport bleibt gleich!)
  Published Language + ACL-Translator + Idempotenz + eigenes Domänenmodell
  Erkenntnis: Das Problem ist nicht der Broker, es ist das fehlende DDD

Stufe 3 — Spring Modulith + Postgres Outbox (Ziel)
  ApplicationEventPublisher + EventPublicationRegistry (JDBC)
  At-Least-Once ohne Broker, Kubernetes-ready, Zero Infrastruktur-Overhead

Stufe 4 — Optional: Service-Extraktion mit @Externalized
  @Externalized → Kafka/RabbitMQ — nur bei echten Microservices nötig
  ACL-Code bleibt identisch, nur Transport ändert sich
```

Die ACL-Logik (Translator + Service) bleibt bei jeder Stufe identisch.
Kein Broker nötig — Spring Modulith + Postgres reicht für den Monolithen.

**Diskussionsfragen:**
- *"Welcher Event-Listener hat den höchsten Geschäftswert und wäre der beste
  Kandidat für die erste ACL-Transformation?"*
- *"Brauchen wir in den nächsten 2 Jahren unabhängig deployte Services?"*
- *"Wenn nein: Was spricht dagegen, direkt auf Spring Modulith + Postgres zu gehen?"*
  → `registrierungsNummer` als Key → Ordering pro Antrag garantiert

## JMS-zu-DDD-Mapping erstellen (einfach, konzeptionell)

Erstelle für ein Legacy-System mit JMS-Messaging eine Übersicht, die für
5-10 ausgewählte Message Listener dokumentiert:

| Listener-Name | JMS Topic/Queue | DDD-Entsprechung | Bounded Context | Fehlende DDD-Elemente |
|---------------|----------------|-------------------|-----------------|----------------------|
| *MonitoringSynchronizer* | `topic/AntragGeaendert` | Event Listener | Auswertung | ACL Translator, eigenes VO |
| *AuszahlungListener* | `topic/ZahlungFreigegeben` | Event Listener | Auszahlung | Idempotenz, eigenes Aggregate |
| ... | ... | ... | ... | ... |

**Lernziel:** Message Listener in Legacy-Systemen sind nicht "Chaos", sondern
ein implizites event-getriebenes System — mit DDD-Brille lassen sich ACLs,
Bounded Contexts und fehlende Translators systematisch identifizieren.
