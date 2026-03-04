---
marp: true
theme: default
paginate: true
header: "DDD & Clean Architecture mit Spring Boot 3"
footer: "© 2026 – Workshop S2090"
style: |
  section {
    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  }
  h1 {
    color: #2d6a4f;
  }
  h2 {
    color: #40916c;
  }
  code {
    background-color: #f0f0f0;
    border-radius: 4px;
    padding: 2px 6px;
  }
---

# Modul 12 – Context Integration

## Bounded Contexts verbinden – ACL, Events, Kafka

**Geschätzte Dauer:** ca. 60 Minuten

### Lernziele

- Context-Mapping-Patterns (aus Modul 04) technisch umsetzen
- Anti-Corruption Layer zwischen Bounded Contexts implementieren
- Idempotente Event-Verarbeitung sicherstellen
- Den Weg von In-Process Events zu Kafka nachvollziehen
- Wissen, wo ACL-Code in der Paketstruktur lebt

---

## Warum Context Integration?

- Bounded Contexts sind bewusst **voneinander getrennt**
- Trotzdem müssen Geschäftsprozesse **kontextübergreifend** ablaufen

### Beispiel im Immobilien-CRM

```
┌── Akquise BC ──────────┐     Event      ┌── Vermittlung BC ──────┐
│                         │               │                         │
│  Maklervertrag wird     │──────────────►│  Vermittlungsvorgang    │
│  abgeschlossen          │               │  wird automatisch       │
│                         │ MaklervertragAbge-│  angelegt            │
└─────────────────────────┘ schlossen     └─────────────────────────┘
```

- Lose Kopplung durch **Events** statt direkte Methodenaufrufe
- Jeder BC behält seine **eigene Ubiquitous Language**
- Die Übersetzung findet im **Anti-Corruption Layer** statt

---

## Context-Mapping-Patterns: Technische Umsetzung

| Pattern (Modul 04) | Technische Umsetzung |
|--------------------|---------------------|
| **Customer/Supplier** | Event Publishing + ACL-Listener |
| **Open Host Service** | REST API mit Published Language (JSON Schema) |
| **Anti-Corruption Layer** | Translator-Klasse an der Modul-Grenze |
| **Conformist** | Event direkt verwenden, kein eigenes Modell |
| **Shared Kernel** | Shared-Modul mit Value Objects |
| **Separate Ways** | Keine Integration |

> In diesem Modul fokussieren wir uns auf **Event-basierte Integration
> mit ACL** — das häufigste Pattern in modularen Monolithen.

---

## Cross-BC-Event publizieren

### Schritt 1: Event im publizierenden BC definieren

```java
// Öffentliche API des Akquise-Moduls (NICHT in internal/)
package de.immobiliencrm.akquise;

public record MaklervertragAbgeschlossenEvent(
    UUID maklervertragId,
    UUID objektId,
    UUID eigentuemerId,
    Instant occurredAt
) {
    public MaklervertragAbgeschlossenEvent(
            UUID maklervertragId, UUID objektId, UUID eigentuemerId) {
        this(maklervertragId, objektId, eigentuemerId, Instant.now());
    }
}
```

- Event verwendet **primitive Typen** (UUID) — keine Value Objects des BCs
- Liegt im **Root-Package** des Moduls = öffentliche API
- Andere Module dürfen dieses Record importieren

---

## Cross-BC-Event publizieren

### Schritt 2: Application Service dispatched nach dem Speichern

```java
package de.immobiliencrm.akquise.internal;

@Service
public class VertragAbschliessenService {

    private final MaklervertragRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    public VertragAbschliessenService(
            MaklervertragRepository repository,
            ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void abschliessen(MaklervertragId id) {
        var vertrag = repository.findById(id).orElseThrow();
        vertrag.abschliessen();
        repository.save(vertrag);
        vertrag.domainEvents().forEach(eventPublisher::publishEvent);
        vertrag.clearDomainEvents();
    }
}
```

---

## Anti-Corruption Layer – Konzept

### Das Problem

- Das Event `MaklervertragAbgeschlossenEvent` spricht die **Akquise-Sprache**
- Der Vermittlung BC kennt keine "Maklerverträge" — er hat **eigene Begriffe**
- Ohne ACL: Akquise-Konzepte "infizieren" das Vermittlung-Domänenmodell

### Die Lösung

```
┌── Akquise BC ────┐                      ┌── Vermittlung BC ─────────────┐
│                   │   Event              │                               │
│  Maklervertrag    │──────────────────►  │  ┌─── ACL ──────────────┐    │
│  abschliessen()   │ MaklervertragAbge-   │  │ AkquiseEventTranslator│    │
│                   │ schlossen            │  │   → VermittlungStarten │    │
└───────────────────┘                      │  │     Command            │    │
                                           │  └───────────┬────────────┘    │
                                           │              ▼                │
                                           │  VermittlungStartenService    │
                                           │  → Vermittlungsvorgang.       │
                                           │    erstellen()                │
                                           └───────────────────────────────┘
```

---

## ACL: Wo lebt der Code?

### Paketstruktur des konsumierenden BC

```
de.immobiliencrm.vermittlung
├── VermittlungApi.java              ← öffentliche API
├── internal
│   ├── domain
│   │   └── model
│   │       └── Vermittlungsvorgang.java
│   ├── application
│   │   └── VermittlungStartenService.java
│   └── adapter
│       └── acl                      ← Anti-Corruption Layer
│           ├── AkquiseEventTranslator.java
│           └── AkquiseEventListener.java
```

- Der ACL ist ein **Adapter** des konsumierenden BC
- Er gehört in die **Adapter-Schicht** (äußerer Ring)
- Der Application Service kennt nur seinen eigenen Command

---

## ACL-Implementierung: Translator

```java
package de.immobiliencrm.vermittlung.internal.adapter.acl;

@Component
public class AkquiseEventTranslator {

    public VermittlungStartenCommand translate(
            MaklervertragAbgeschlossenEvent event) {
        return new VermittlungStartenCommand(
            new ObjektReferenz(event.objektId()),
            new VertragReferenz(event.maklervertragId()),
            LocalDate.now()
        );
    }
}
```

- **Fremde IDs** werden in eigene Value Objects gewrappt
- **Fremde Begriffe** werden in eigene Domänensprache übersetzt
- `MaklervertragAbgeschlossen` (Akquise) → `VermittlungStartenCommand` (Vermittlung)
- Der Translator ist ein reiner Mapper — keine Geschäftslogik

---

## ACL-Implementierung: Event Listener

```java
package de.immobiliencrm.vermittlung.internal.adapter.acl;

@Component
public class AkquiseEventListener {

    private final AkquiseEventTranslator translator;
    private final VermittlungStartenService service;

    public AkquiseEventListener(AkquiseEventTranslator translator,
                                VermittlungStartenService service) {
        this.translator = translator;
        this.service = service;
    }

    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void on(MaklervertragAbgeschlossenEvent event) {
        var command = translator.translate(event);
        service.starten(command);
    }
}
```

- Listener delegiert **sofort** an den Translator
- Application Service kennt **nur eigene Commands**
- Keine Abhängigkeit des Vermittlung-Domänenmodells auf Akquise-Klassen

---

## Idempotente Event-Verarbeitung

### Das Problem bei At-Least-Once Delivery

- Events können **mehrfach** zugestellt werden (Crash, Retry, Redelivery)
- Ohne Idempotenz: Vermittlungsvorgang wird **doppelt** angelegt

### Lösung: Idempotenz-Check im Service

```java
@Service
public class VermittlungStartenService {

    private final VermittlungsvorgangRepository repository;

    @Transactional
    public void starten(VermittlungStartenCommand cmd) {
        // Idempotenz: schon vorhanden?
        if (repository.existsByVertragReferenz(cmd.vertragReferenz())) {
            log.info("Vermittlung für Vertrag {} existiert bereits",
                cmd.vertragReferenz());
            return;
        }
        var vorgang = Vermittlungsvorgang.erstellen(
            VorgangId.generate(), cmd.objektReferenz(), cmd.vertragReferenz());
        repository.save(vorgang);
    }
}
```

---

## Gesamtbild: Event Flow zwischen BCs

```
Akquise BC                        Vermittlung BC
┌──────────────────────┐          ┌────────────────────────────────┐
│ VertragAbschliessen  │          │                                │
│ Service              │          │  adapter.acl                   │
│   │                  │  publish │  ┌─────────────────────────┐   │
│   ├─ vertrag         │─────────►│  │ AkquiseEventListener   │   │
│   │  .abschliessen() │  Event   │  │   ├─ translator         │   │
│   ├─ save()          │          │  │   │  .translate(event)  │   │
│   └─ dispatch events │          │  │   └─ service.starten()  │   │
│                      │          │  └─────────────────────────┘   │
│ MaklervertragAbge-   │          │                                │
│ schlossenEvent       │          │  application                   │
│ (öffentliche API)    │          │  ┌─────────────────────────┐   │
└──────────────────────┘          │  │ VermittlungStarten      │   │
                                  │  │ Service                 │   │
                                  │  │  → idempotenz check     │   │
                                  │  │  → Vorgang.erstellen()  │   │
                                  │  │  → save()               │   │
                                  │  └─────────────────────────┘   │
                                  └────────────────────────────────┘
```

---

## Ausblick: Von In-Process zu Kafka

### Im Modulith (heute)

```java
// In-Process: Spring ApplicationEventPublisher
eventPublisher.publishEvent(new MaklervertragAbgeschlossenEvent(...));
```

### Als Microservices (später)

```java
// Producer Adapter (Akquise-Service)
@Component
public class KafkaEventPublisher {
    private final KafkaTemplate<String, Object> kafka;

    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void on(MaklervertragAbgeschlossenEvent event) {
        kafka.send("akquise.maklervertrag.abgeschlossen",
            event.maklervertragId().toString(), event);
    }
}

// Consumer Adapter (Vermittlung-Service)
@KafkaListener(topics = "akquise.maklervertrag.abgeschlossen",
    groupId = "vermittlung")
public void consume(MaklervertragAbgeschlossenEvent event) {
    var command = translator.translate(event);
    service.starten(command);
}
```

---

## Ausblick: Kafka – Was ändert sich?

| Aspekt | In-Process (Modulith) | Kafka (Microservices) |
|--------|----------------------|----------------------|
| **Transport** | Methodenaufruf | Netzwerk (Topic) |
| **Garantie** | Eventual (AFTER_COMMIT) | At-Least-Once |
| **Serialisierung** | Java Objekt | JSON / Avro |
| **Idempotenz** | Empfohlen | **Pflicht** |
| **ACL-Code** | Identisch | Identisch |
| **Reihenfolge** | Garantiert (single thread) | Nur pro Partition |

- **Gleiche ACL-Logik** — nur der Transport ändert sich
- `@Externalized` (Spring Modulith) bereitet den Übergang vor
- Kafka garantiert **At-Least-Once** → Idempotenz immer beachten

---

## Zusammenfassung

- **Context Integration** verbindet Bounded Contexts über **Events**
- Der **Anti-Corruption Layer** übersetzt fremde Konzepte in die eigene Sprache
- ACL lebt in der **Adapter-Schicht** des konsumierenden BC
- **Idempotenz** ist Pflicht bei At-Least-Once Delivery
- Von In-Process → Kafka: **ACL-Code bleibt gleich**, nur Transport ändert sich
- Öffentliche Event-API: **primitive Typen**, im Root-Package des Moduls

```
Event-Entscheidungsbaum:
├─ Gleicher BC?        → Domain Event (intern, nicht publiziert)
├─ Anderer BC, gleicher Monolith? → ApplicationEventPublisher + ACL
└─ Anderer Service?    → Kafka/RabbitMQ + ACL + Idempotenz
```

---

## 🎯 Hands-on: Lab-10

### Aufgabe

Implementiert Cross-BC-Integration im Immobilien-CRM:

1. Domain Event `MaklervertragAbgeschlossenEvent` im Akquise-Modul erstellen
2. Event über `ApplicationEventPublisher` publizieren (Event Collection Pattern)
3. ACL-Translator im Vermittlung-Modul implementieren
4. `@TransactionalEventListener` registrieren
5. Idempotenz-Check im Application Service einbauen
6. Vermittlungsvorgang automatisch anlegen lassen

> **Dauer:** ca. 45 Minuten
> Details und Aufgabenstellung im **Lab-10**

---

## 💬 Diskussion

> Wie würdet ihr die Integration zwischen euren Bounded Contexts gestalten?

- Welche Events würdet ihr als **synchron** vs. **asynchron** modellieren?
- Wo seht ihr die Grenze zwischen In-Process Events und Kafka?
- Habt ihr Erfahrungen mit dem **Outbox Pattern**?
- Wie geht ihr mit **Eventual Consistency** zwischen BCs um?
- Wo braucht ihr einen **ACL** und wo reicht ein **Conformist**?
