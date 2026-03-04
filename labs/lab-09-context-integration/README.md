# Lab 09: Context-Integration -- Bounded Contexts verbinden

## Lernziel

Event-basierte Kommunikation zwischen Bounded Contexts implementieren.

## Dauer

60 Minuten

## Voraussetzungen

- Lab 08 abgeschlossen
- Slides Module 11 und 12

## Aufgabe

Erstelle einen zweiten Bounded Context "Akquise" und verbinde ihn ueber Domain Events mit dem bestehenden Vermittlungs-BC.

### Schritt 1: Minimalen Akquise-BC erstellen

Erstelle die Entity `Maklerauftrag` im Package `de.immobiliencrm.akquise.domain.model`:

- Felder: `id` (UUID), `eigentuemerId` (UUID), `immobilieId` (UUID), `abgeschlossenAm` (LocalDateTime)
- Methode: `abschliessen()` setzt `abgeschlossenAm` auf den aktuellen Zeitpunkt

```java
public class Maklerauftrag {

    private final UUID id;
    private final UUID eigentuemerId;
    private final UUID immobilieId;
    private LocalDateTime abgeschlossenAm;

    // Constructor, Factory-Methode, Getter
    // abschliessen() setzt abgeschlossenAm = LocalDateTime.now()
}
```

### Schritt 2: Integration Event erstellen

Erstelle das Integration Event `MaklervertragAbgeschlossen` als Record im Package `de.immobiliencrm.akquise.domain.event`:

```java
public record MaklervertragAbgeschlossen(
    UUID maklerauftragId,
    UUID immobilieId,
    LocalDateTime abgeschlossenAm
) {}
```

### Schritt 3: Application Service im Akquise-BC

Erstelle den Service `MaklerauftragAbschliessenUseCase` im Package `de.immobiliencrm.akquise.application.service`:

- Injiziere `MaklerauftragRepository` und `ApplicationEventPublisher`
- Methode `abschliessen(UUID maklerauftragId)`:
  1. Maklerauftrag laden
  2. `abschliessen()` aufrufen
  3. Speichern
  4. Event `MaklervertragAbgeschlossen` ueber `ApplicationEventPublisher` publizieren

```java
@Service
public class MaklerauftragAbschliessenUseCase {

    private final MaklerauftragRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    // Constructor Injection

    @Transactional
    public void abschliessen(UUID maklerauftragId) {
        // 1. Laden
        // 2. abschliessen()
        // 3. Speichern
        // 4. Event publizieren
    }
}
```

### Schritt 4: Event Listener im Vermittlung-BC

Erstelle den Listener `MaklervertragAbgeschlossenListener` im Package `de.immobiliencrm.vermittlung.application.listener`:

```java
@Component
public class MaklervertragAbgeschlossenListener {

    private final VermittlungsvorgangRepository repository;

    // Constructor Injection

    @EventListener
    public void handle(MaklervertragAbgeschlossen event) {
        // Neuen Vermittlungsvorgang erstellen
        // mit der immobilieId aus dem Event
        // Speichern
    }
}
```

### Schritt 5: Test

Schreibe einen Integrationstest, der den gesamten Flow prueft:

1. Erstelle einen `Maklerauftrag`
2. Schliesse ihn ab (ueber den UseCase)
3. Pruefe, dass ein `Vermittlungsvorgang` automatisch erstellt wurde

### Bonus: TransactionalEventListener

Ersetze `@EventListener` durch `@TransactionalEventListener(phase = AFTER_COMMIT)`, um sicherzustellen, dass das Event erst nach dem erfolgreichen Commit der Transaktion verarbeitet wird.

## Verifikation

Fuehre den Integrationstest aus:

```bash
cd solution
mvn test
```

Der Test muss bestaetigen, dass nach dem Abschliessen eines Maklerauftrags automatisch ein Vermittlungsvorgang erstellt wird.

## Tipps

- Spring's `ApplicationEventPublisher` eignet sich gut fuer die Kommunikation zwischen Bounded Contexts innerhalb eines Monolithen.
- Das Event gehoert zum publizierenden BC (Akquise) -- der konsumierende BC (Vermittlung) importiert es.
- Achte darauf, dass der Listener im Vermittlungs-BC keine direkte Abhaengigkeit zum Akquise-Domain-Model hat -- nur zum Event.
- `@TransactionalEventListener(phase = AFTER_COMMIT)` stellt sicher, dass das Event erst verarbeitet wird, wenn die Transaktion erfolgreich war.
