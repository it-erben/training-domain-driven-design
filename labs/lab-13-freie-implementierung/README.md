# Lab 13: Freie Implementierung - Vertiefung und Erweiterung

Wähle eine oder mehrere der folgenden Vertiefungsoptionen und implementiere sie eigenständig.

## AcceptOffer - ein weiterer Use Case (einfach)

Implementiere den `AcceptOfferUseCase` mit Command, Application Service, REST-Endpoint und Tests. Der Flow im Überblick:

1. `POST /api/brokerage/processes/{id}/offers` mit Angebotsbetrag und Interessent
2. Command-Objekt `AcceptOfferCommand(UUID brokerageProcessId, String prospectName, BigDecimal amount)`
3. Application Service lädt den `BrokerageProcess`, ruft `acceptOffer()` auf und speichert
4. REST-Endpoint gibt 201 mit der Angebots-ID zurück
5. Tests: Unit-Test für Use Case, WebMvcTest für Controller

## Anti-Corruption Layer mit OpenImmo-XML (mittel)

Externe Systeme liefern Daten in fremden Formaten. Erstelle einen Adapter, der ein (simuliertes) OpenImmo-XML-Dokument entgegennimmt und in die Domain-Objekte der Objektverwaltung übersetzt.

Definiere dazu ein einfaches OpenImmo-XML-Format (z.B. `<openimmo><anbieter><immobilie><geo><plz>50667</plz><ort>Köln</ort></geo></immobilie></anbieter></openimmo>`), erstelle einen `OpenImmoTranslationService` im Package `adapter.acl` und verwende JAXB oder einfaches String-Parsing für die Übersetzung. Vergiss die Tests nicht.

## CQRS: Separates Read Model (mittel)

Listenansichten brauchen andere Daten als Schreiboperationen. Erstelle ein separates Read Model `BrokerageProcessOverview` als eigene `@Entity` (oder Spring Data JPA Projection) mit den Feldern `id`, `address` (als String), `status`, `viewingCount`, `offerCount`.

Dazu gehören ein dedizierter `BrokerageProcessQueryService` und ein separater REST-Endpoint `GET /api/brokerage/processes/overview`.

## Kafka-Anbindung skizzieren (schwer)

Wie sähe eine Kafka-basierte Event-Kommunikation zwischen Bounded Contexts aus? Füge `spring-kafka` als Dependency hinzu und erstelle sowohl einen Outbound-Adapter `KafkaBrokerageProcessEventPublisher`, der Domain Events auf ein Kafka-Topic schreibt, als auch einen Inbound-Adapter `KafkaAcquisitionEventConsumer`, der Events empfängt. Konfiguriere Kafka in `application.yml`.

Ein laufender Kafka-Broker wird nicht benötigt - es reicht eine Skizze mit den richtigen Annotationen und Konfigurationen.
