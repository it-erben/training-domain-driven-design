# Lab 09: Context-Integration – Bounded Contexts verbinden

In der Förderantragsverwaltung gibt es bisher den Antragstellung-Bounded Context,
der sich um die Erfassung und Einreichung von Förderanträgen kümmert. Aber was
passiert nach der Einreichung?

In der Fachdomäne beginnt die **fachliche Prüfung**: Eine Sachbearbeiterin der
Bewilligungsstelle prüft den eingereichten Antrag. Erst wenn die AntragsMappe
eingereicht ist, soll automatisch ein `Pruefvorgang` im Prüfungs-BC angelegt
werden. Diesen nachgelagerten Schritt bilden wir in einem zweiten Bounded
Context ab — der **Fachlichen Prüfung**.

Der Antragstellung-BC publiziert bei Einreichung ein Integrations-Event, auf
das der Prüfungs-BC über einen **Anti-Corruption Layer** reagiert.

## Schritt 1: Integrations-Event im Antragstellung-BC

Erstelle das Integrations-Event `AntragsmappeEingereicht` als Record im
Root-Package `de.foerderung.antragstellung` (öffentliche API des Moduls):

```java
package de.foerderung.antragstellung;

public record AntragsmappeEingereicht(
    UUID antragsmappeId,
    String registrierungsNummer,
    Instant eingereichtAm
) {
    public AntragsmappeEingereicht(UUID antragsmappeId, String registrierungsNummer) {
        this(antragsmappeId, registrierungsNummer, Instant.now());
    }
}
```

Das Event verwendet primitive Typen (UUID, String, Instant) — keine Value Objects
des Antragstellung-BCs. Andere Module dürfen dieses Record importieren.

## Schritt 2: Application Service im Antragstellung-BC

Erstelle den Service `AntragEinreichenService` im Package
`de.foerderung.antragstellung.internal.application`:

- Injiziert `AntragsMappeRepository` und `ApplicationEventPublisher`
- Methode `execute(AntragEinreichenCommand cmd)`:
    1. Lade die `AntragsMappe`
    2. Rufe `einreichen()` auf (registriert das Event im Aggregate)
    3. Speichere
    4. Lese die Domain Events aus dem Aggregate und publiziere sie über
       `ApplicationEventPublisher`
    5. Lösche die Domain Events im Aggregate

```java
@Service
@Transactional
@RequiredArgsConstructor
public class AntragEinreichenService {

    private final AntragsMappeRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    public void execute(AntragEinreichenCommand cmd) {
        var mappe = repository.findById(cmd.antragsmappeId()).orElseThrow();
        mappe.einreichen();
        repository.save(mappe);
        mappe.domainEvents().forEach(eventPublisher::publishEvent);
        mappe.clearDomainEvents();
    }
}
```

## Schritt 3: ACL-Translator im Prüfungs-BC

Erstelle den Translator `AntragstellungEventTranslator` im Package
`de.foerderung.pruefung.internal.adapter.acl`. Der Translator übersetzt das
fremde Event in einen eigenen Command — mit eigenen Value Objects:

```java
@Component
public class AntragstellungEventTranslator {

    public PruefungStartenCommand translate(AntragsmappeEingereicht event) {
        return new PruefungStartenCommand(
            new AntragsReferenz(event.antragsmappeId()),      // eigenes Value Object!
            new RegistrierungsNummer(event.registrierungsNummer()), // eigenes VO!
            event.eingereichtAm()
        );
    }
}
```

**Wichtig:** Der Translator ist ein reiner Mapper — keine Geschäftslogik.
Fremde IDs werden in eigene Value Objects gewrappt, fremde Begriffe in die
eigene Domänensprache übersetzt:
`AntragsmappeEingereicht` (Antragstellung) → `PruefungStartenCommand` (Prüfung).

## Schritt 4: Event-Listener im Prüfungs-BC

Erstelle den Listener `AntragstellungEventListener` im selben ACL-Package.
Der Listener delegiert sofort an den Translator — der Application Service
kennt nur eigene Commands:

```java
@Component
@RequiredArgsConstructor
public class AntragstellungEventListener {

    private final AntragstellungEventTranslator translator;
    private final PruefungStartenService service;

    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void on(AntragsmappeEingereicht event) {
        var command = translator.translate(event);
        service.start(command);
    }
}
```

## Schritt 5: Application Service mit Idempotenz im Prüfungs-BC

Erstelle den `PruefungStartenService` im Package
`de.foerderung.pruefung.internal.application`:

```java
@Service
@Transactional
@RequiredArgsConstructor
public class PruefungStartenService {

    private final PruefvorgangRepository repository;

    public void start(PruefungStartenCommand cmd) {
        // Idempotenz: bereits vorhanden?
        if (repository.existsByAntragsReferenz(cmd.antragsReferenz())) {
            log.info("Pruefvorgang fuer Antrag {} bereits vorhanden",
                cmd.antragsReferenz());
            return;  // stille Deduplizierung
        }
        var pruefvorgang = Pruefvorgang.starten(
            PruefvorgangId.generate(), cmd.antragsReferenz(), cmd.eingereichtAm());
        repository.save(pruefvorgang);
    }
}
```

**Warum Idempotenz?** Bei At-Least-Once Delivery (z. B. mit Spring Modulith
EventPublicationRegistry oder Kafka) können Events mehrfach zugestellt werden.
Ohne Idempotenz-Check würde ein `Pruefvorgang` doppelt angelegt.

## Schritt 6: REST-Adapter für den Antragstellung-BC

Erstelle einen `AntragsMappeController` im Package
`de.foerderung.antragstellung.internal.adapter.web`:

- `POST /api/antragstellung/antraege` – Erstellt eine neue AntragsMappe
- `POST /api/antragstellung/antraege/{id}/einreichen` – Reicht den Antrag ein
  (triggert das Event)

So kann der gesamte Fluss über die API getestet werden:

1. AntragsMappe anlegen
2. AntragsMappe einreichen
3. Pruefvorgang erscheint automatisch im Prüfungs-BC

## Schritt 7: Test

Schreibe einen Integrationstest, der den gesamten Ablauf verifiziert:

1. Erstelle eine `AntragsMappe` mit konkreten Werten
2. Reiche sie ein (über den Service)
3. Suche den erstellten `Pruefvorgang` gezielt per `antragsReferenz`
4. Prüfe, dass die fachlichen Werte korrekt übernommen wurden

```java
@Test
void shouldCreatePruefvorgangWhenAntragsmappeEingereicht() {
    UUID mappeId = UUID.randomUUID();
    String regNr = "DE-ELER-2026-001";

    AntragsMappe mappe = antragEinreichenService.create(mappeId, regNr);
    antragEinreichenService.execute(new AntragEinreichenCommand(mappe.getId()));

    Optional<Pruefvorgang> pruefvorgang =
        pruefvorgangRepository.findByAntragsReferenz(new AntragsReferenz(mappeId));

    assertTrue(pruefvorgang.isPresent());
    assertEquals(regNr, pruefvorgang.get().getRegistrierungsNummer().value());
}
```

## Gut zu wissen

### Paketstruktur: Wo lebt der ACL?

```
de.foerderung/
├── antragstellung/
│   ├── AntragsmappeEingereicht.java     ← öffentliche API (Event Record)
│   └── internal/
│       ├── domain/
│       │   └── AntragsMappe.java        ← Aggregate Root
│       ├── application/
│       │   └── AntragEinreichenService.java
│       └── adapter/
│           └── web/AntragsMappeController.java
└── pruefung/
    └── internal/
        ├── domain/
        │   ├── Pruefvorgang.java         ← eigenes Aggregate, eigene Sprache
        │   ├── AntragsReferenz.java      ← eigenes Value Object (nur ID!)
        │   └── RegistrierungsNummer.java ← eigenes Value Object
        ├── application/
        │   └── PruefungStartenService.java
        └── adapter/
            └── acl/                      ← hier lebt der Anti-Corruption Layer
                ├── AntragstellungEventTranslator.java
                └── AntragstellungEventListener.java
```

### Event-Publishing: Aggregate vs. Application Service

In dieser Lösung registriert das Aggregate (`AntragsMappe`) das Event
intern in `einreichen()`. Der Application Service liest die Events anschließend
aus und publiziert sie. Das stellt sicher, dass nur fachlich gültige Events
entstehen.

### Shared Kernel und Abhängigkeiten

Der Prüfungs-BC importiert das `AntragsmappeEingereicht`-Event direkt aus dem
Package `de.foerderung.antragstellung`. Das erzeugt einen **impliziten Shared
Kernel** zwischen den beiden Bounded Contexts.

Alternativen wären:

- Ein **separates Shared-Events-Modul**, aus dem beide BCs importieren
- Ein **eigenes Event-Interface im Prüfungs-BC** (Anti-Corruption Layer), das
  vom Listener auf die eigene Sprache gemappt wird
- In einem verteilten System: **Serialisierung** (z.B. JSON), sodass keine
  Compile-Time-Abhängigkeit entsteht

Für einen Monolithen ist der direkte Import ein pragmatischer Kompromiss. In
einem verteilten System wäre eine stärkere Entkopplung nötig.

### @TransactionalEventListener — At-Most-Once

Die aktuelle Lösung nutzt `@TransactionalEventListener(phase = AFTER_COMMIT)`.
Das Event wird nach dem Commit des Publishers verarbeitet — aber ohne Retry.
Crasht der Listener, geht das Event verloren (**At-Most-Once**).

**Bonus:** Aktiviere die Spring Modulith EventPublicationRegistry (JDBC) für
At-Least-Once Delivery. Was muss am Idempotenz-Check geändert werden?
(Antwort: Nichts — der Check ist bereits vorhanden.)

### JMS-Konzepte → DDD-Konzepte

In gewachsenen Systemen findet man oft JMS-basierte Messaging-Patterns. Diese
Tabelle zeigt die konzeptionelle Zuordnung:

| JMS / EJB (Legacy-System) | Spring / DDD (modernes System) |
|---------------------------|--------------------------------|
| `@MessageDriven` | `@TransactionalEventListener` |
| JMS Topic (Pub/Sub) | `ApplicationEventPublisher` + mehrere `@EventListener` |
| `messageSelector` auf `messageObjectClass` | Java-Typ-basiertes Event-Routing (automatisch) |
| `ObjectMessage` + Cast | Typsicheres Java Record |
| JMS Queue (Point-to-Point) | Command per direktem Service-Aufruf |

> Der `messageSelector` ist **Event-Routing**, kein ACL.
> Der ACL ist der Translator, der aus dem fremden Typ einen eigenen Command macht.
