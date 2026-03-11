---
marp: true
theme: default
paginate: true
header: "DDD & Clean Architecture mit Spring Boot 4"
footer: "CC BY-NC-SA 4.0, Alexander Erben"
---

# Exkurs: Von MDBs zu Spring Modulith

## Das Legacy-Messaging-Problem mit DDD-Brille lösen — nicht mit neuem Broker

Geschätzte Dauer: ca. 90 Minuten (inkl. Event Storming und Diskussion)

### Warum dieser Exkurs?

- Zahlreiche `@MessageDriven`-Beans sind kein "technisches Chaos"
- Sie sind ein **implizites event-getriebenes System** — ohne die DDD-Elemente, die es explizit machen
- Die Lösung ist **kein Broker-Wechsel** (JMS → Kafka), sondern **DDD-Muster nachrüsten**
- Spring Modulith + Postgres gibt uns alles, was wir brauchen — **ohne zusätzliche Infrastruktur**

---

## Agenda

1. **Das MDB-Problem verstehen** — Was haben wir wirklich?
2. **Event Storming auf dem Ist-Zustand** — MDBs als implizite Events sichtbar machen
3. **DDD-Diagnose** — Was fehlt? (ACL, Published Language, Idempotenz)
4. **Die falsche Lösung** — Warum "einfach Kafka" das Problem verlagert
5. **Die richtige Architektur** — Spring Modulith + Postgres Outbox
6. **Schritt-für-Schritt Transformation** — Von MDB zu Modulith
7. **Kubernetes-Readiness** — Warum brokerlos besser skaliert
8. **Hands-on: Eine MDB transformieren**

---
<style scoped>section { font-size: 1.55em; }</style>

## Teil 1: Was haben wir wirklich?

### Anatomie einer typischen MDB

```java
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destination",
        propertyValue = "topic/AntragGeaendert"),
    @ActivationConfigProperty(propertyName = "messageSelector",
        propertyValue = "messageObjectClass= 'de.legacy.antrag.basis.AntragsmappeAenderung'")
})
public class MonitoringSynchronizerMDB implements MessageListener {
    @Override
    public void onMessage(Message message) {
        AntragsmappeAenderung aend =
            (AntragsmappeAenderung) ((ObjectMessage) message).getObject();
        if (aend.getAenderungsArt() == UPDATED)
            optimusPrime.synchronisiere(aend.getRegistrationNumber());
    }
}
```

> Sieht chaotisch aus. Ist es aber nicht — es sind **fünf DDD-Konzepte**, die nur nie explizit gemacht wurden.

---
<style scoped>section { font-size: 1.1em; }</style>

## Reale MDB-Patterns in gewachsenen Systemen

### Pattern 1: Generische Basisklasse mit Template-Methode

```java
// Verbreitetes Muster: Abstrakte Basis-MDB mit generischem Event-Typ
public abstract class AbstractAenderungsListenerMDB<A extends AenderungAnRegisterable>
        implements MessageListener {

    @Override
    public void onMessage(Message message) {
        ObjectMessage oMsg = (ObjectMessage) message;
        A aenderung = (A) oMsg.getObject();           // ← generischer Cast
        verarbeiteAenderung(aenderung);
    }

    protected void verarbeiteAenderung(A aenderung) {
        if (isAenderungRelevant(aenderung))
            for (RegistryEntry kandidat : ermittleKandidaten(aenderung))
                if (isAenderungRelevant(aenderung, kandidat))
                    passAenderungToRegisterable(aenderung, kandidat);
    }

    protected abstract List<DokumentArt> getDokumentArtenFuerKandidatenSuche();
    protected boolean isAenderungRelevant(A aenderung) { return true; }
}
```

**DDD-Diagnose:**
- ✅ Template-Method-Pattern → impliziter Application Service
- ❌ `AenderungAnRegisterable` ist Shared Kernel über BC-Grenzen hinweg
- ❌ Kein Translator: `aenderung.getNummer()` wird direkt verwendet
- ❌ Keine Published Language: `AenderungAnRegisterable` ist ein internes API-Objekt
- ❌ Dutzende Subklassen erben die gleiche Conformist-Kopplung

---
<style scoped>section { font-size: 1.05em; }</style>

## Reale MDB-Patterns (Forts.)

### Pattern 2: instanceof-Kette als Event-Router

```java
// Reales Muster: Eine MDB verarbeitet viele Event-Typen per instanceof
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destination",
        propertyValue = "topic/VWKPMonitorAktualisierung"),
    @ActivationConfigProperty(propertyName = "maxSession", propertyValue = "16") })
public class VWKPMonitorAktualisierungMDB implements MessageListener {

    @Override
    public void onMessage(Message message) {
        EntityChangedEvent event = extractEventFromMessage(message);
        AbstractStrategie<?> strategie = ermittleStrategieFuerEvent(event);
        if (strategie != null) strategie.ausfuehren();
    }

    private AbstractStrategie<?> ermittleStrategieFuerEvent(EntityChangedEvent event) {
        if (event instanceof ProtokollChangedEvent)       return new UpdateProtokollStrategie(...);
        if (event instanceof Protokoll2ChangedEvent)      return new UpdateProtokoll2Strategie(...);
        if (event instanceof MappenZustandChangedEvent)    return new UpdateMappenZustandStrategie(...);
        if (event instanceof VorgangZustandChangedEvent)   return new UpdateVorgangZustandStrategie(...);
        // ... 9 weitere instanceof-Checks
        return null;
    }
}
```

**DDD-Diagnose:**
- ✅ Strategy-Pattern existiert → implizite Command-Handler
- ✅ `maxSession = 16` → bewusste Concurrency-Entscheidung
- ❌ Alle 9+ Event-Typen aus **demselben** externen Package → Shared Kernel
- ❌ Kein Idempotenz-Check in den Strategien
- ❌ `instanceof`-Kette statt typbasiertem Event-Routing

---
<style scoped>section { font-size: 1.05em; }</style>

## Reale MDB-Patterns (Forts.)

### Pattern 3: MDB mit direkten JPA-Operationen

```java
// Reales Muster: MDB als Bescheid-Ereignis-Listener mit EntityManager
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destination",
        propertyValue = BescheidEreignisse.TOPIC),
    @ActivationConfigProperty(propertyName = "maxSession", propertyValue = "1") })
public class BescheidEreignisListenerMDB implements MessageListener {

    @PersistenceContext(unitName = PersistenceUnitInfo.PU)
    private EntityManager em;                          // ← direkter DB-Zugriff!

    @Override
    public void onMessage(Message message) {
        AbstractBescheidEreignis ereignis = getBescheidEreignis(message);
        BescheidVorgangsErgebnis ergebnis = ermittleErgebnis(ereignis);
        verarbeiteBescheidEreignis(ereignis, ergebnis);
    }

    private void verarbeiteBescheidEreignis(AbstractBescheidEreignis ereignis,
            BescheidVorgangsErgebnis ergebnis) {
        if (ereignis instanceof BescheidGedruckt)
            vermerkeDruckDaten((BescheidGedruckt) ereignis, ergebnis);
        else if (ereignis instanceof NeueBescheidVersionErstellt)
            entferneDruckDaten(ergebnis);
        else if (ereignis instanceof BescheidGesendet)
            vermerkeVersandDaten((BescheidGesendet) ereignis, ergebnis);
    }
}
```

**DDD-Diagnose:**
- ✅ `BescheidEreignisse.TOPIC` als Konstante → rudimentäre Published Language
- ✅ Fachlich klare Event-Typen (`BescheidGedruckt`, `BescheidGesendet`)
- ❌ `EntityManager` direkt in MDB → Adapter + Domain + Persistence vermischt
- ❌ `instanceof`-Kette statt separater Handler
- ❌ Kein Idempotenz-Check (`vermerkeDruckDaten` prüft nur auf `gedrucktAm == null`)

---
<style scoped>section { font-size: 1.1em; }</style>

## Reale MDB-Patterns (Forts.)

### Pattern 4: MDB als Webhook-Trigger (Ceres-Module)

```java
// Reales Muster: MDB filtert per messageSelector und triggert Webhook
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destination",
        propertyValue = "topic/AenderungAnRegisterable"),
    @ActivationConfigProperty(propertyName = "messageSelector",
        propertyValue = "messageObjectClass = "
            + "'...AenderungAnTieranlagen4StarteWebhook'"
            + " AND antragsjahr >= 2024"),
    @ActivationConfigProperty(propertyName = "maxSession", propertyValue = "5") })
public class Anlage4AenderungListenerMDB implements MessageListener {

    @Override
    public void onMessage(Message message) {
        AenderungAnTieranlagen4StarteWebhook aenderung =
            (AenderungAnTieranlagen4StarteWebhook) ((ObjectMessage) message).getObject();
        verarbeiteAenderung(aenderung);
    }

    private void verarbeiteAenderung(AenderungAnTieranlagen4StarteWebhook aenderung) {
        if (aenderung.getJahr().compareTo(GUELTIG_AB) >= 0) {
            TieranlageMappe mappe = tieranlagenAuskunft
                .holeTiereangabenfuerWebhook(aenderung.getNummer(), aenderung.getJahr());
            new WebhookDatenBereitsteller(mappe)
                .stelleDatenfuerWebhookVerfahrenBereit();
        }
    }
}
```

**DDD-Diagnose:**
- ✅ `messageSelector` mit Jahresfilter → fachliche Filterung am Event-Bus
- ✅ Webhook als Outbound-Adapter → rudimentäre Service-Extraktion
- ❌ Kein Translator: `aenderung.getNummer()` direkt verwendet
- ❌ Webhook-Logik in der MDB selbst statt in separatem Service
- ❌ Error-Handling per `catch (DegRTException)` + Logging statt Retry/Dead-Letter

---
<style scoped>section { font-size: 1.35em; }</style>

## Die fünf versteckten DDD-Konzepte in jeder MDB

| MDB-Element | DDD-Konzept | Was fehlt? |
|-------------|-------------|------------|
| `topic/AntragGeaendert` | **Domain Event** | Published Language / Event Contract |
| `messageSelector` auf `messageObjectClass` | **Event-Routing** | Typ-basiertes Routing (Java-Typen statt Strings) |
| `onMessage()` + Cast auf `AntragsmappeAenderung` | **Event Listener** | Anti-Corruption Layer (Translator) |
| `optimusPrime.synchronisiere(...)` | **Application Service** | Eigener Command, Idempotenz-Check |
| Die MDB-Klasse selbst | **Adapter** (Hexagonal) | Gehört in `adapter.acl`, nicht in `mdb` |

> **Erkenntnis:** Jede MDB ist ein unvollständiger Anti-Corruption Layer.
> Es fehlen genau zwei Dinge: der **Translator** und das **eigene Domänenmodell**.

---
<style scoped>section { font-size: 1.3em; }</style>

## Das Gesamtbild: Implizite Event-Architektur

```
                    ┌─────────────────────────────────┐
                    │   topic/AntragGeaendert          │
                    │   (JMS Topic = Event-Bus)        │
                    └──────────┬──────────────────────┘
                               │
          ┌────────────────────┼────────────────────────────┐
          │                    │                             │
          ▼                    ▼                             ▼
  ┌───────────────┐   ┌───────────────┐            ┌───────────────┐
  │ Monitoring-   │   │ Auszahlungs-  │     ...    │ Bescheid-     │
  │ Synchronizer  │   │ Listener      │            │ Versand       │
  │ MDB           │   │ MDB           │            │ MDB           │
  │               │   │               │            │               │
  │ ❌ Kein ACL   │   │ ❌ Kein ACL   │            │ ❌ Kein ACL   │
  │ ❌ Kein VO    │   │ ❌ Kein VO    │            │ ❌ Kein VO    │
  │ ❌ Kein Idem. │   │ ❌ Kein Idem. │            │ ❌ Kein Idem. │
  └───────────────┘   └───────────────┘            └───────────────┘
   BC: Auswertung      BC: Auszahlung              BC: Bescheid
```

**Was DDD draus macht:** Das sind **Bounded Contexts**, die über ein **Domain Event** kommunizieren.
Nur: Es gibt keine **Published Language**, keinen **Translator**, und keine **Idempotenz**.

---
<style scoped>section { font-size: 1.4em; }</style>

## Event Storming: MDBs sichtbar machen

### Übung: MDB-Inventur als Event Storming (20 Min)

**Schritt 1:** Jede MDB auf eine **orange Karte** (Domain Event) schreiben

```
┌──────────────────────┐    ┌──────────────────────┐    ┌──────────────────────┐
│ 🟠 Antragsmappe      │    │ 🟠 Zahlung           │    │ 🟠 Zahlung           │
│    Geaendert         │    │    Freigegeben        │    │    Vermerkt           │
└──────────────────────┘    └──────────────────────┘    └──────────────────────┘
```

**Schritt 2:** Jede MDB auf eine **lila Karte** (Policy/Listener) darunter

```
┌──────────────────────┐    ┌──────────────────────┐    ┌──────────────────────┐
│ 🟣 Monitoring        │    │ 🟣 Auszahlung        │    │ 🟣 Bescheid          │
│    synchronisieren   │    │    durchfuehren       │    │    erstellen          │
└──────────────────────┘    └──────────────────────┘    └──────────────────────┘
```

**Schritt 3:** Bounded-Context-Grenzen einzeichnen

> Ergebnis: Eine explizite Event-Landkarte eures Legacy-Systems.

---
<style scoped>section { font-size: 1.3em; }</style>

## Event Storming: Fehlende Elemente identifizieren

### Schritt 4: Für jede MDB die DDD-Checkliste durchgehen

| Frage | ✅ Vorhanden? | Was fehlt? |
|-------|:---:|------------|
| Gibt es ein **typisiertes Event** (Record/DTO) als Published Language? | ❌ | `ObjectMessage` + String-basierter `messageSelector` |
| Gibt es einen **Translator** (ACL), der fremde Events in eigene Commands übersetzt? | ❌ | Direkte Cast auf `AntragsmappeAenderung` |
| Hat der konsumierende BC ein **eigenes Domänenmodell** (Value Objects, Aggregates)? | ❌ | Arbeitet direkt mit `RegistrationNumber` des fremden BC |
| Gibt es einen **Idempotenz-Check** bei Mehrfachzustellung? | ❌ | Event erneut geliefert → Auszahlung doppelt angelegt |
| Ist der **Bounded Context** als Modul erkennbar? | ❌ | Alles in einem Monolith-Package ohne Modulgrenze |

> **Kernfrage an die Teilnehmer:**
> Wenn vier von fünf Elementen fehlen — ist dann das Problem der JMS-Broker?
> Oder das fehlende DDD?

---
<style scoped>section { font-size: 1.3em; }</style>

## Teil 2: Die falsche Lösung — "Einfach Kafka"

### Der Infrastruktur-Reflex

```
"Wir haben ein Messaging-Problem mit JMS"
    → "Dann ersetzen wir JMS durch Kafka!"
```

### Was wirklich passiert

```
VORHER:                                    NACHHER:
┌──────────────────────┐                   ┌──────────────────────┐
│ JMS Topic            │                   │ Kafka Topic          │
│ topic/AntragGeaendert│                   │ antrag.geaendert.v1  │
└──────────┬───────────┘                   └──────────┬───────────┘
           │                                          │
    ┌──────▼──────┐                            ┌──────▼──────┐
    │ MDB         │                            │ Consumer    │
    │ Cast auf    │                            │ Cast auf    │
    │ fremdes Obj │                            │ fremdes JSON│
    │ Kein ACL    │                            │ Kein ACL    │
    │ Kein Idem.  │                            │ Kein Idem.  │
    └─────────────┘                            └─────────────┘

Gleiche Architektur-Probleme. Nur der Transport ist neu.
+ Kafka-Cluster betreiben (ZooKeeper/KRaft, Broker, Topic-Management)
+ Schema Registry betreiben
+ Monitoring für Consumer Lag
+ Ops-Team für Kafka-Expertise
```

---
<style scoped>section { font-size: 1.55em; }</style>

## Warum Kafka das Problem nicht löst

### Die DDD-Perspektive

| Problem | Löst JMS → Kafka das? | Was es wirklich braucht |
|---------|:---:|------------|
| Kein Translator (ACL) | ❌ | Translator-Klasse im konsumierenden BC |
| Kein eigenes Domänenmodell | ❌ | Value Objects + Aggregates im konsumierenden BC |
| Keine Published Language | ❌ | Typisiertes Event-Record als öffentliche API |
| Keine Idempotenz | ❌ | Idempotenz-Check im Application Service |
| Keine Modulgrenze | ❌ | Package-Struktur mit `internal/` |
| Kopplung an fremde Klassen | ❌ | ACL übersetzt an der Grenze |

### Was Kafka löst (und was Spring Modulith auch löst)

| Kafka löst... | Spring Modulith + Postgres löst... |
|--------------|----------------------------------|
| At-Least-Once Delivery | ✅ EventPublicationRegistry (JDBC) |
| Durability (Crash-Sicherheit) | ✅ Transactional Outbox in Postgres |
| Replay / Audit Trail | ✅ Event-Log-Tabelle in Postgres |
| Service-Entkopplung | ✅ Modul-Isolation + `@Externalized` |

> **Kafka ist die richtige Antwort — auf eine andere Frage.**
> Nämlich: "Wie verteile ich Events zwischen unabhängig deployten Services?"
> Aber nicht: "Wie bringe ich DDD-Muster in meinen Monolithen?"

---
<style scoped>section { font-size: 1.15em; }</style>

## Teil 3: Die richtige Lösung — Spring Modulith + Postgres

### Zielarchitektur: Brokerlos, Kubernetes-ready

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        Spring Modulith Monolith                        │
│                                                                        │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐    │
│  │ Antragstellung   │    │ Fachliche        │    │ Auswertung       │    │
│  │ (Core BC)        │    │ Prüfung (Core)   │    │ (Supporting BC)  │    │
│  │                  │    │                  │    │                  │    │
│  │ ┌──────────────┐ │    │ ┌──────────────┐ │    │ ┌──────────────┐ │    │
│  │ │ Domain Model │ │    │ │ Domain Model │ │    │ │ Domain Model │ │    │
│  │ │ AntragsMappe │ │    │ │ Pruefvorgang │ │    │ │ Monitoring   │ │    │
│  │ └──────┬───────┘ │    │ └──────────────┘ │    │ │ Uebersicht   │ │    │
│  │        │         │    │        ▲         │    │ └──────────────┘ │    │
│  │   publishEvent() │    │   ┌────┴───────┐ │    │        ▲         │    │
│  │        │         │    │   │ ACL:       │ │    │   ┌────┴───────┐ │    │
│  │        ▼         │    │   │ Translator │ │    │   │ ACL:       │ │    │
│  │ ┌──────────────┐ │    │   │ + Listener │ │    │   │ Translator │ │    │
│  │ │ Antragsmappe │ │    │   └────────────┘ │    │   │ + Listener │ │    │
│  │ │ Eingereicht  │─┼──▶─┼─ Spring Event ──┼──▶─┼───┘            │ │    │
│  │ │ (Public API) │ │    │                  │    │                  │    │
│  │ └──────────────┘ │    └─────────────────┘    └─────────────────┘    │
│  └─────────────────┘                                                   │
│                                                                        │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │  Postgres: event_publication (Spring Modulith Outbox)           │   │
│  │  ┌────────┬───────────────────────────┬──────────┬───────────┐  │   │
│  │  │ id     │ event_type                │ status   │ published │  │   │
│  │  │ uuid-1 │ AntragsmappeEingereicht   │ COMPLETED│ 2026-03   │  │   │
│  │  │ uuid-2 │ AntragsmappeGeaendert     │ INCOMPLETE│ 2026-03  │  │   │
│  │  └────────┴───────────────────────────┴──────────┴───────────┘  │   │
│  └─────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────┘
```

---
<style scoped>section { font-size: 1.55em; }</style>

## Warum Spring Modulith + Postgres statt Kafka?

### Architektur-Entscheidung im Kontext

| Kriterium | Kafka | Spring Modulith + Postgres |
|-----------|-------|---------------------------|
| **Infrastruktur** | Kafka-Cluster + Schema Registry | Postgres (haben wir schon) |
| **Ops-Aufwand** | Hoch (Broker, Partitionen, Consumer Groups) | Null (JDBC-Tabelle) |
| **Kubernetes** | StatefulSet + PVCs + Monitoring | Stateless Pods + Managed DB |
| **Delivery-Garantie** | At-Least-Once | At-Least-Once |
| **Transaktionssicherheit** | Separate Transaktion (2PC oder Outbox) | Gleiche DB-Transaktion |
| **Idempotenz nötig?** | Ja | Ja |
| **Replay möglich?** | Ja (Consumer Offset Reset) | Ja (Event-Log-Tabelle) |
| **Zukunft: Microservices** | Dann ja — per `@Externalized` | Event-API bleibt gleich |

### Die Schlüsselfrage

> Brauchen wir **verteilte Services**, die unabhängig deployt werden?
> → **Nein?** Dann brauchen wir keinen Broker. Spring Modulith reicht.
> → **Ja, irgendwann?** Spring Modulith ist die Vorstufe. `@Externalized` macht den Übergang trivial.

---
<style scoped>section { font-size: 1.1em; }</style>

## Der richtige Evolutionspfad

### Alt (falsch): Infrastruktur-getrieben

```
JMS/MDB  →  Spring Events  →  Webhooks  →  Kafka
                                              ↑
                                   "Dafür brauchen wir einen Broker!"
```

### Neu (richtig): DDD-getrieben

```
Stufe 1 — Ist-Zustand: JMS/MDB (Conformist, kein ACL)
  │  Problem: Das fehlende DDD, nicht der JMS-Broker
  │
  ▼
Stufe 2 — DDD nachrüsten (gleicher Transport!)
  │  ✅ Published Language (typisierte Events als Records)
  │  ✅ ACL + Translator im konsumierenden BC
  │  ✅ Eigenes Domänenmodell pro BC
  │  ✅ Idempotenz-Check in Application Services
  │  ✅ Package-Struktur mit Modulgrenzen
  │  → Transport bleibt JMS — das ist OK!
  │
  ▼
Stufe 3 — Spring Modulith + Postgres Outbox
  │  ✅ JMS-Broker entfällt (In-Process Events)
  │  ✅ At-Least-Once per EventPublicationRegistry
  │  ✅ Keine zusätzliche Infrastruktur
  │  ✅ Kubernetes-ready (stateless + managed Postgres)
  │
  ▼
Stufe 4 — Optional: Service-Extraktion mit @Externalized
     Nur wenn tatsächlich unabhängig deployte Services nötig sind
     @Externalized → Kafka/RabbitMQ/SNS — ACL-Code bleibt gleich!
```

---
<style scoped>section { font-size: 1.4em; }</style>

## Stufe 2 im Detail: DDD nachrüsten — bei gleichem Transport

### Das Entscheidende: Die DDD-Muster sind transport-agnostisch!

**Schritt 1: Published Language definieren**

```java
// Öffentliche API des Antragstellung-BC (Root-Package, NICHT internal/)
// Das ist die Published Language — der Vertrag zwischen BCs
package de.foerderung.antragstellung;

public record AntragsmappeGeaendert(
    String registrierungsNummer,    // nur primitive Typen!
    AenderungsArt aenderungsArt,
    Instant geaendertAm
) {}

public enum AenderungsArt { AKTUALISIERT, REAKTIVIERT, ENTFERNT, ARCHIVIERT }
```

> Diese Published Language kann über JMS, Spring Events oder Kafka transportiert werden.
> Der Vertrag bleibt gleich — nur die Serialisierung ändert sich.

---
<style scoped>section { font-size: 1.25em; }</style>

## Stufe 2: ACL + Translator nachrüsten

### Die MDB bleibt — aber bekommt einen Translator

```java
// NACHHER: ACL — die MDB delegiert an Translator + eigenen Service
@Override
public void onMessage(Message message) {
    AntragsmappeAenderung aend =
        (AntragsmappeAenderung) ((ObjectMessage) message).getObject();
    var command = translator.translate(aend);        // Schritt 1: Translate
    monitoringService.synchronisiere(command);       // Schritt 2: Eigener Service
}
```

```java
// Der Translator — das fehlende Puzzlestück
@Component
class AntragsmappeEventTranslator {
    MonitoringSynchronisierenCommand translate(AntragsmappeAenderung aend) {
        return new MonitoringSynchronisierenCommand(
            new AntragsReferenz(aend.getRegistrationNumber()),  // eigenes VO!
            MonitoringsStatus.from(aend.getAenderungsArt())     // eigene Enum!
        );
    }
}
```

> **Wichtig:** Wir ändern noch NICHTS am Transport. Nur die DDD-Muster werden ergänzt.

---
<style scoped>section { font-size: 1.25em; }</style>

## Stufe 2: Eigenes Domänenmodell im konsumierenden BC

### Der Auswertungs-BC braucht eigene Begriffe

```java
// VORHER: Verwendet RegistrationNumber aus dem Antragstellungs-BC
optimusPrime.synchronisiere(aend.getRegistrationNumber());
//                         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^
//                         Typ aus einem ANDEREN Bounded Context!

// NACHHER: Eigene Value Objects
package de.foerderung.auswertung.internal.domain.model;

public record AntragsReferenz(String wert) {             // eigenes VO
    public AntragsReferenz {
        Objects.requireNonNull(wert, "AntragsReferenz darf nicht null sein");
    }
}

public enum MonitoringsStatus {                          // eigene Enum
    AKTIV, INAKTIV, ARCHIVIERT;

    public static MonitoringsStatus from(AenderungsArt art) {
        return switch (art) {
            case AKTUALISIERT, REAKTIVIERT -> AKTIV;
            case ENTFERNT -> INAKTIV;
            case ARCHIVIERT -> ARCHIVIERT;
        };
    }
}
```

> Jetzt kann sich `AntragsmappeAenderung` ändern — der Translator fängt es ab,
> das Auswertungs-Domänenmodell bleibt stabil.

---
<style scoped>section { font-size: 1.2em; }</style>

## Stufe 2: Idempotenz nachrüsten

### Warum Idempotenz JETZT einbauen — nicht erst bei Kafka?

```java
// PROBLEM: JMS-Redelivery bei Transaction Rollback
// → MDB wird erneut aufgerufen → Monitoring-Eintrag doppelt

// VORHER: Kein Idempotenz-Check
public void synchronisiere(String registrationNumber) {
    monitoringRepository.save(new MonitoringEintrag(registrationNumber));
    // Bei Redelivery: Duplikat!
}

// NACHHER: Idempotenz-Check im Application Service
@Service
public class MonitoringService {
    private final MonitoringRepository repository;

    @Transactional
    public void synchronisiere(MonitoringSynchronisierenCommand cmd) {
        // Idempotenz: bereits verarbeitet?
        if (repository.existsByAntragsReferenz(cmd.antragsReferenz())) {
            repository.aktualisiere(
                cmd.antragsReferenz(), cmd.status());
            return;  // Update statt Insert → sicher bei Mehrfachzustellung
        }
        repository.save(MonitoringEintrag.erstellen(
            cmd.antragsReferenz(), cmd.status()));
    }
}
```

> Idempotenz ist kein Kafka-Feature. Es ist eine **DDD-Verantwortung des Application Service**.
> Wer sie erst bei Kafka einbaut, hat sie bei JMS-Redelivery schon nicht.

---
<style scoped>section { font-size: 1.3em; }</style>

## Stufe 2: ArchUnit-Absicherung

### Neue MDBs ohne Translator verhindern

```java
@ArchTest
static final ArchRule jede_mdb_nutzt_translator =
    FreezingArchRule.freeze(
        classes()
            .that().areAnnotatedWith(MessageDriven.class)
            .should().dependOnClassesThat()
                .resideInAPackage("..translator..")
            .as("Jede MDB muss einen Translator verwenden")
    );
```

### Direkte Imports fremder Domain-Klassen unterbinden

```java
@ArchTest
static final ArchRule keine_fremden_domain_imports =
    FreezingArchRule.freeze(
        noClasses()
            .that().resideInAPackage("..auswertung..")
            .should().dependOnClassesThat()
                .resideInAPackage("..antragstellung.internal..")
            .as("Auswertung darf nicht auf Antragstellung-interne Klassen zugreifen")
    );
```

> `FreezingArchRule`: Bestehende Verstöße werden "eingefroren", nur **neue** brechen den Build.
> So kann das Team schrittweise migrieren, ohne alles auf einmal ändern zu müssen.

---
<style scoped>section { font-size: 1.25em; }</style>

## Stufe 3: JMS durch Spring Modulith ersetzen

### Die MDB verschwindet — der Translator bleibt

```java
// VORHER: MDB + JMS
@MessageDriven(...)
public class MonitoringSynchronizerMDB implements MessageListener {
    @Override public void onMessage(Message message) {
        AntragsmappeAenderung aend = /* cast */;
        monitoringService.synchronisiere(translator.translate(aend));
    }
}

// NACHHER: Spring Modulith EventListener
@Component @RequiredArgsConstructor
class AntragsmappeEventListener {
    private final AntragsmappeEventTranslator translator;  // gleicher Translator!
    private final MonitoringService service;               // gleicher Service!

    @TransactionalEventListener(phase = AFTER_COMMIT)
    void on(AntragsmappeGeaendert event) {
        service.synchronisiere(translator.translate(event));
    }
}
```

| Element | Änderung von Stufe 2 → 3? |
|---------|:---:|
| **Translator** | Gleich (nur Eingabetyp: Record statt ObjectMessage) |
| **Application Service** | Gleich |
| **Idempotenz-Check** | Gleich |
| **Domain Model** | Gleich |
| **Transport** | JMS → Spring Event (In-Process) |

---
<style scoped>section { font-size: 1.3em; }</style>

## Stufe 3: Postgres Outbox — At-Least-Once ohne Broker

### Spring Modulith EventPublicationRegistry

```yaml
# application.yml — das ist alles an Konfiguration!
spring:
  modulith:
    events:
      republish-outstanding-events-on-restart: true
      jdbc:
        schema-initialization:
          enabled: true
```

```sql
-- Spring Modulith erstellt automatisch:
CREATE TABLE event_publication (
    id               UUID PRIMARY KEY,
    listener_id      TEXT NOT NULL,
    event_type       TEXT NOT NULL,
    serialized_event TEXT NOT NULL,
    publication_date TIMESTAMP NOT NULL,
    completion_date  TIMESTAMP          -- NULL = noch nicht verarbeitet
);
```

### Der Mechanismus

1. `publishEvent()` → Event + Outbox-Eintrag in **gleicher DB-Transaktion**
2. Listener verarbeitet Event → `completion_date` wird gesetzt
3. Bei Crash: `completion_date` bleibt `NULL` → Redelivery beim nächsten Start
4. **Kein Broker**, kein Poller, keine zusätzliche Infrastruktur

---
<style scoped>section { font-size: 1.4em; }</style>

## Stufe 3: Kubernetes-Readiness

### Warum brokerlos besser skaliert

```
MIT Kafka:                              OHNE Kafka (Spring Modulith):
┌──────────────┐                        ┌──────────────┐
│ App Pod 1    │                        │ App Pod 1    │
│ App Pod 2    │                        │ App Pod 2    │
│ App Pod 3    │                        │ App Pod 3    │
└──────┬───────┘                        └──────┬───────┘
       │                                       │
┌──────▼───────┐                        ┌──────▼───────┐
│ Kafka Broker │ StatefulSet            │ Postgres     │ Managed DB
│ (3 Replicas) │ + PVCs                 │ (Managed)    │ (RDS/CloudSQL)
│ ZooKeeper/   │ + Monitoring           └──────────────┘
│ KRaft        │ + Ops-Expertise
│ Schema Reg.  │
└──────────────┘

Infrastruktur-Overhead:                 Infrastruktur-Overhead:
  5+ zusätzliche Pods                     0 zusätzliche Pods
  PersistentVolumeClaims                  Postgres (schon vorhanden)
  Kafka-Monitoring                        Standard DB-Monitoring
  Ops-Wissen für Kafka                    Standard DB-Wissen
```

> In Kubernetes mit Managed Postgres: **Zero zusätzliche Infrastruktur.**
> Der Modulith ist ein simples Deployment + Horizontal Pod Autoscaler.

---
<style scoped>section { font-size: 1.2em; }</style>

## Stufe 4 (optional): Service-Extraktion mit @Externalized

### Wenn tatsächlich unabhängige Services nötig werden

```java
// Im publizierenden BC: Event für externe Konsumenten markieren
@Externalized("antragstellung.antragsmappe.geaendert::#{registrierungsNummer()}")
public record AntragsmappeGeaendert(
    String registrierungsNummer,
    AenderungsArt aenderungsArt,
    Instant geaendertAm
) {}
```

```yaml
# application.yml — jetzt mit Kafka (oder RabbitMQ, oder SNS...)
spring:
  modulith:
    events:
      externalization:
        enabled: true
```

### Was sich ändert

| Element | Änderung? |
|---------|:---:|
| Published Language (Event Record) | `@Externalized` Annotation hinzufügen |
| Translator im konsumierenden BC | Gleich |
| Application Service | Gleich |
| Idempotenz-Check | Gleich |
| Domain Model | Gleich |

> **Der gesamte DDD-Code bleibt identisch.** Nur eine Annotation + Konfiguration.
> Deshalb ist Stufe 2 (DDD nachrüsten) die wichtigste Stufe — nicht Stufe 4.

---
<style scoped>section { font-size: 1.55em; }</style>

## Zusammenfassung: Was sich bei jeder Stufe ändert

| Element | Stufe 1 (JMS) | Stufe 2 (+DDD) | Stufe 3 (Modulith) | Stufe 4 (Extern) |
|---------|:---:|:---:|:---:|:---:|
| Transport | JMS | JMS | Spring Event | Kafka/RMQ |
| Published Language | ❌ | ✅ | ✅ | ✅ |
| ACL/Translator | ❌ | ✅ | ✅ | ✅ |
| Eigenes Domain Model | ❌ | ✅ | ✅ | ✅ |
| Idempotenz | ❌ | ✅ | ✅ | ✅ |
| At-Least-Once | ❌ | ❌ | ✅ (Outbox) | ✅ (Broker) |
| Broker nötig | JMS | JMS | **Nein** | Ja |
| Kubernetes-ready | ❌ | ❌ | ✅ | ✅ |

> **Die DDD-Muster (Stufe 2) liefern 80% des Werts.**
> Stufe 3 ersetzt die Infrastruktur. Stufe 4 ist nur nötig bei echten Microservices.

---
<style scoped>section { font-size: 1.05em; }</style>

## Teil 4: Schritt-für-Schritt — Eine MDB transformieren

### Komplettes Vorher/Nachher am Beispiel MonitoringSynchronizer

**Paketstruktur VORHER (Legacy)**

```
de.legacy
├── antrag
│   └── basis
│       └── AntragsmappeAenderung.java        ← Shared Kernel (Anti-Pattern)
├── mdb
│   └── MonitoringSynchronizerMDB.java        ← Conformist
└── monitoring
    └── OptimusPrime.java                     ← Kein eigenes Domänenmodell
```

**Paketstruktur NACHHER (Spring Modulith)**

```
de.foerderung
├── antragstellung
│   ├── AntragsmappeGeaendert.java            ← Published Language (public API)
│   └── internal
│       └── application
│           └── AntragAendernService.java     ← publiziert Event
└── auswertung
    └── internal
        ├── domain
        │   └── model
        │       ├── MonitoringEintrag.java    ← eigenes Aggregate
        │       ├── AntragsReferenz.java      ← eigenes VO
        │       └── MonitoringsStatus.java    ← eigene Enum
        ├── application
        │   └── MonitoringService.java        ← eigener Service + Idempotenz
        └── adapter
            └── acl
                ├── AntragsmappeEventTranslator.java   ← ACL
                └── AntragsmappeEventListener.java     ← Spring Event Listener
```

---
<style scoped>section { font-size: 1.2em; }</style>

## Komplettes Code-Beispiel: Published Language

### Der Vertrag zwischen Antragstellung und allen Konsumenten

```java
// Paket: de.foerderung.antragstellung (Root = öffentlich)
@Externalized("antragstellung.antragsmappe.geaendert::#{registrierungsNummer()}")
public record AntragsmappeGeaendert(
    String registrierungsNummer,
    AenderungsArt aenderungsArt,
    Instant geaendertAm
) {
    public AntragsmappeGeaendert {
        Objects.requireNonNull(registrierungsNummer);
        Objects.requireNonNull(aenderungsArt);
        Objects.requireNonNull(geaendertAm);
    }
}
```

```java
@Service @Transactional @RequiredArgsConstructor
class AntragAendernService implements AntragAendern {
    private final AntragsMappeRepository repository;
    private final ApplicationEventPublisher events;

    @Override
    public void execute(AntragAendernCommand cmd) {
        var mappe = repository.findById(cmd.antragId()).orElseThrow();
        mappe.aendern(cmd.aenderung());
        repository.save(mappe);
        events.publishEvent(new AntragsmappeGeaendert(
            mappe.getRegistrierungsNummer().wert(), cmd.aenderungsArt(), Instant.now()));
    }
}
```

---
<style scoped>section { font-size: 1.4em; }</style>

## ACL im Auswertungs-BC: Translator + Listener

```java
@Component
class AntragsmappeEventTranslator {
    MonitoringSynchronisierenCommand translate(AntragsmappeGeaendert event) {
        return new MonitoringSynchronisierenCommand(
            new AntragsReferenz(event.registrierungsNummer()),
            MonitoringsStatus.from(event.aenderungsArt()),
            event.geaendertAm()
        );
    }
}
```

```java
@Component @RequiredArgsConstructor
class AntragsmappeEventListener {
    private final AntragsmappeEventTranslator translator;
    private final MonitoringService service;

    @TransactionalEventListener(phase = AFTER_COMMIT)
    void on(AntragsmappeGeaendert event) {
        service.synchronisiere(translator.translate(event));
    }
}
```

---
<style scoped>section { font-size: 1.4em; }</style>

## ACL im Auswertungs-BC: Application Service mit Idempotenz

```java
@Service @Transactional @RequiredArgsConstructor
class MonitoringService {
    private final MonitoringRepository repository;

    void synchronisiere(MonitoringSynchronisierenCommand cmd) {
        repository.findByAntragsReferenz(cmd.antragsReferenz())
            .ifPresentOrElse(
                eintrag -> eintrag.aktualisiere(cmd.status(), cmd.geaendertAm()),
                () -> repository.save(MonitoringEintrag.erstellen(
                    cmd.antragsReferenz(), cmd.status(), cmd.geaendertAm()))
            );
    }
}
```

> Translator, Listener und Service — drei kleine Klassen ersetzen eine MDB.
> Jede hat genau eine Verantwortung. Zusammen bilden sie den ACL.

---
<style scoped>section { font-size: 1.35em; }</style>

## Modulith-Verifikation: Spring Modulith testet die Grenzen

```java
@ModulithTest
class ModulithVerificationTest {

    @Test
    void verifyModulithStructure() {
        // Prüft: Kein BC greift auf internal/ eines anderen BC zu
        ApplicationModules.of(FoerderantragApplication.class).verify();
    }

    @Test
    void documentModules() {
        // Erzeugt Modul-Dokumentation als PlantUML/Asciidoc
        new Documenter(ApplicationModules.of(FoerderantragApplication.class))
            .writeModulesAsPlantUml()
            .writeIndividualModulesAsPlantUml();
    }
}
```

### Was `verify()` prüft

- Keine zyklischen Abhängigkeiten zwischen BCs
- Kein Zugriff auf `internal/` Packages anderer BCs
- Event-Listener importieren nur öffentliche API (Published Language)

> Spring Modulith macht zur **Build-Zeit**, was ArchUnit auch kann —
> aber mit Verständnis für Event-basierte Integration.

---
<style scoped>section { font-size: 1.3em; }</style>

## Resilience: Was passiert bei Fehlern?

### Vergleich der Fehlerszenarien

| Szenario | JMS/MDB (Legacy) | Spring Modulith + Postgres |
|----------|-------------------|---------------------------|
| **Listener wirft Exception** | JMS-Redelivery (unkontrolliert, oft Endlosschleife) | Event bleibt INCOMPLETE → kontrolliertes Retry beim Restart |
| **Pod crasht während Verarbeitung** | Event verloren (At-Most-Once) | Outbox-Eintrag bleibt → Redelivery beim nächsten Start |
| **DB nicht erreichbar** | MDB verarbeitet, aber Schreiben schlägt fehl → inkonsistenter Zustand | Transaktion rollt zurück → Event + Zustand konsistent |
| **Doppelte Zustellung** | Duplikate (kein Idempotenz-Check) | Idempotenz im Service fängt es ab |
| **Rückstau** | JMS-Broker füllt sich, Backpressure schwer steuerbar | Outbox-Tabelle in Postgres, Verarbeitung in Batches |
| **Monitoring** | JMS-Console, proprietäre Tools | SQL-Query auf `event_publication`-Tabelle |

### Monitoring per SQL

```sql
-- Welche Events sind noch nicht verarbeitet?
SELECT event_type, COUNT(*), MIN(publication_date)
FROM event_publication
WHERE completion_date IS NULL
GROUP BY event_type
ORDER BY MIN(publication_date);

-- Dashboard-Alert: Events älter als 5 Minuten
SELECT * FROM event_publication
WHERE completion_date IS NULL
AND publication_date < NOW() - INTERVAL '5 minutes';
```

---
<style scoped>section { font-size: 1.5em; }</style>

## MDB-Inventur: Das Werkzeug für die Migration

### Vorlage: Jede MDB dokumentieren und priorisieren

| MDB-Klasse | JMS Topic | Ziel-BC | Geschäftswert | Risiko ohne Idempotenz | Priorität |
|---|---|---|---|---|---|
| MonitoringSynchronizer | AntragGeaendert | Auswertung | Hoch (Dashboard) | Mittel (Duplikate) | **1** |
| AuszahlungListener | ZahlungFreigegeben | Auszahlung | Sehr hoch | **Hoch (Doppelzahlung!)** | **1** |
| BescheidVersandListener | ZahlungVermerkt | Bescheid | Mittel | Niedrig | 2 |
| ... | ... | ... | ... | ... | ... |

### Priorisierungs-Kriterien

1. **Geschäftswert**: Welche MDB betrifft den kritischsten Geschäftsprozess?
2. **Risiko ohne Idempotenz**: Wo ist eine Doppelausführung am teuersten?
3. **Kopplung**: Welche MDB importiert die meisten fremden Klassen?
4. **Änderungshäufigkeit**: Welche MDB wird am häufigsten wegen Änderungen im publizierenden BC angepasst?

> **Tipp:** Beginnt mit der MDB, die den höchsten Geschäftswert **und** das höchste Risiko hat.

---
<style scoped>section { font-size: 1.55em; }</style>

## Diskussion: Euer System durch die DDD-Brille

### Runde 1: Bestandsaufnahme (10 Min)

- Wie viele MDBs gibt es in eurem System?
- Welche JMS-Topics transportieren die meisten Events?
- Wie viele MDBs casten direkt auf `AntragsmappeAenderung` (oder äquivalent)?

### Runde 2: DDD-Diagnose (10 Min)

- Wo haben wir **Conformist** statt **ACL**?
- Welche MDBs haben kein eigenes Domänenmodell im konsumierenden BC?
- Wo fehlt ein Idempotenz-Check am dringendsten?

### Runde 3: Migration planen (10 Min)

- Welche MDB wäre der beste Kandidat für die erste Transformation?
- Was passiert, wenn wir `AntragsmappeAenderung` umbenennen?
  *→ Alle abhängigen MDBs kompilieren nicht mehr — weil keine Published Language existiert*
- Wo gibt es `@Scheduled`-Polling als Ersatz für echte Events?

### Runde 4: Architektur-Entscheidung (10 Min)

- Brauchen wir in den nächsten 2 Jahren unabhängig deployte Services?
- Wenn nein: Was spricht dagegen, direkt auf Spring Modulith + Postgres zu gehen?
- Welche Webhook-Lösung (Ceres) könnte durch `@Externalized` ersetzt werden?

---
<style scoped>section { font-size: 1.55em; }</style>

## Zusammenfassung

### Die drei Kernerkenntnisse

**1. Das Problem ist nicht der Broker — es ist das fehlende DDD**

MDBs sind implizite Event-Listener ohne ACL, Translator, eigenes Domänenmodell und Idempotenz.
Ein Broker-Wechsel (JMS → Kafka) ändert daran nichts.

**2. DDD-Muster sind transport-agnostisch**

Published Language, ACL, Translator und Idempotenz funktionieren gleich —
egal ob JMS, Spring Events oder Kafka darunter liegt.

**3. Spring Modulith + Postgres ist der richtige Zielpunkt**

- Kein Broker-Overhead in Kubernetes
- At-Least-Once per Postgres Outbox
- Modulverifikation zur Build-Zeit
- `@Externalized` als Escape Hatch für echte Microservices

### Der Merksatz

> **Erst DDD, dann Infrastruktur.**
> Wer ACL und Translator hat, kann den Transport jederzeit austauschen.
> Wer nur den Transport austauscht, hat die gleichen Probleme auf neuem Stack.

---

## Literatur und Quellen

- Evans, "Domain-Driven Design" (2003), Kapitel 14: Anti-Corruption Layer
- Khononov, "Learning Domain-Driven Design" (2022), Kapitel 9: Communication Patterns
- Stenberg & de Jong, "Spring Modulith Reference Documentation" (2026)
- Santana, "Domain-Driven Design with Java" (2026), Kapitel 10: Reliable Event Publishing
- Fowler, "Transactional Outbox" (martinfowler.com)
- Spring Modulith Docs: Event Publication Registry
