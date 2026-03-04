# Lab 09: Context-Integration - Bounded Contexts verbinden

## Lernziel

Event-basierte Kommunikation zwischen Bounded Contexts implementieren.

## Dauer

60 Minuten

## Voraussetzungen

- Lab 08 abgeschlossen
- Slides Module 11 und 12

## Aufgabe

Erstelle einen zweiten Bounded Context "Akquise" und verbinde ihn über Domain Events mit dem bestehenden Vermittlungs-BC.

### Schritt 1: Minimalen Akquise-BC erstellen

Erstelle die Entity `Maklerauftrag` im Package `de.immobiliencrm.akquise.domain.model`:

- Felder: `id` (UUID), `eigentümerId` (UUID), `immobilieId` (UUID), `abgeschlossenAm` (LocalDateTime)
- Methode: `abschließen()` setzt `abgeschlossenAm` auf den aktuellen Zeitpunkt

```java
public class Maklerauftrag {

    private final UUID id;
    private final UUID eigentümerId;
    private final UUID immobilieId;
    private LocalDateTime abgeschlossenAm;

    // Constructor, Factory-Methode, Getter
    // abschließen() setzt abgeschlossenAm = LocalDateTime.now()
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

Erstelle den Service `MaklerauftragAbschließenUseCase` im Package `de.immobiliencrm.akquise.application.service`:

- Injiziere `MaklerauftragRepository` und `ApplicationEventPublisher`
- Methode `abschließen(UUID maklerauftragId)`:
  1. Maklerauftrag laden
  2. `abschließen()` aufrufen
  3. Speichern
  4. Event `MaklervertragAbgeschlossen` über `ApplicationEventPublisher` publizieren

```java
@Service
public class MaklerauftragAbschließenUseCase {

    private final MaklerauftragRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    // Constructor Injection

    @Transactional
    public void abschließen(UUID maklerauftragId) {
        // 1. Laden
        // 2. abschließen()
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

Schreibe einen Integrationstest, der den gesamten Flow prüft:

1. Erstelle einen `Maklerauftrag`
2. Schließe ihn ab (über den UseCase)
3. Prüfe, dass ein `Vermittlungsvorgang` automatisch erstellt wurde

### Bonus: TransactionalEventListener

Ersetze `@EventListener` durch `@TransactionalEventListener(phase = AFTER_COMMIT)`, um sicherzustellen, dass das Event erst nach dem erfolgreichen Commit der Transaktion verarbeitet wird.

## Verifikation

Führe den Integrationstest aus:

```bash
cd solution
mvn test
```

Der Test muss bestätigen, dass nach dem Abschließen eines Maklerauftrags automatisch ein Vermittlungsvorgang erstellt wird.

## Tipps

- Spring's `ApplicationEventPublisher` eignet sich gut für die Kommunikation zwischen Bounded Contexts innerhalb eines Monolithen.
- Das Event gehört zum publizierenden BC (Akquise) - der konsumierende BC (Vermittlung) importiert es.
- Achte darauf, dass der Listener im Vermittlungs-BC keine direkte Abhängigkeit zum Akquise-Domain-Model hat - nur zum Event.
- `@TransactionalEventListener(phase = AFTER_COMMIT)` stellt sicher, dass das Event erst verarbeitet wird, wenn die Transaktion erfolgreich war.
